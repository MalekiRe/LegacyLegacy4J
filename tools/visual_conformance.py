#!/usr/bin/env python3
"""Deterministic, region-aware visual conformance reports for Legacy4J.

The manifest keeps scenario setup and meaningful UI regions reviewable.  Each
run compares fixed upstream references with backport captures, localizes pixel
differences into connected components, and emits machine- and human-readable
artifacts.  Pillow and numpy are the only non-stdlib dependencies.
"""

from __future__ import annotations

import argparse
from collections import deque
import json
import os
from pathlib import Path
import subprocess
import sys
import time
from typing import Any, Iterable

import numpy as np
from PIL import Image


SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_MANIFEST = SCRIPT_DIR / "visual_scenarios.json"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Capture and compare deterministic Legacy4J UI scenarios."
    )
    parser.add_argument(
        "scenarios",
        nargs="*",
        help="Scenario names (default: every scenario in the manifest).",
    )
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("build/visual-conformance"),
        help="Report/artifact directory, relative to the project root.",
    )
    parser.add_argument(
        "--candidate-dir",
        type=Path,
        help="Use <candidate-dir>/<scenario>.png instead of manifest candidates.",
    )
    parser.add_argument(
        "--capture",
        action="store_true",
        help="Run the deterministic in-game driver before comparison.",
    )
    parser.add_argument(
        "--capture-dir",
        type=Path,
        default=Path("build/visual-captures"),
        help="Destination used by --capture.",
    )
    parser.add_argument(
        "--capture-timeout",
        type=int,
        default=240,
        help="Maximum runClient time in seconds.",
    )
    parser.add_argument("--zoom", type=int, default=4)
    parser.add_argument(
        "--strict-missing",
        action="store_true",
        help="Exit unsuccessfully when a reference or capture is missing.",
    )
    parser.add_argument(
        "--fail-on-difference",
        action="store_true",
        help="Exit unsuccessfully if any thresholded pixel differs.",
    )
    return parser.parse_args()


def load_manifest(path: Path) -> tuple[dict[str, Any], Path]:
    path = path.resolve()
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("version") != 1:
        raise SystemExit(f"Unsupported manifest version in {path}")
    root = (path.parent / data.get("root", ".")).resolve()
    if not isinstance(data.get("scenarios"), dict):
        raise SystemExit(f"Manifest has no scenarios object: {path}")
    return data, root


def resolve_from_root(root: Path, value: Path | str) -> Path:
    path = Path(value)
    return path.resolve() if path.is_absolute() else (root / path).resolve()


def selected_scenarios(manifest: dict[str, Any], requested: list[str]) -> list[str]:
    available = list(manifest["scenarios"])
    if not requested:
        return available
    flattened: list[str] = []
    for value in requested:
        flattened.extend(name.strip() for name in value.split(",") if name.strip())
    unknown = sorted(set(flattened) - set(available))
    if unknown:
        raise SystemExit(
            "Unknown scenario(s): %s; choices: %s"
            % (", ".join(unknown), ", ".join(available))
        )
    return flattened


def run_capture(root: Path, scenarios: list[str], capture_dir: Path, timeout: int) -> None:
    capture_dir.mkdir(parents=True, exist_ok=True)
    world_name = f"__legacy4j_visual_tests_{os.getpid()}_{int(time.time())}"
    properties = [
        f"-Dlegacy4j.visualScenarios={','.join(scenarios)}",
        f"-Dlegacy4j.visualOutput={capture_dir}",
        f"-Dlegacy4j.visualWorld={world_name}",
        "-Dlegacy4j.visualExit=true",
    ]
    command = [str(root / "gradlew"), "--no-daemon", *properties, "runClient"]
    env = os.environ.copy()
    # JAVA_TOOL_OPTIONS reaches both Gradle and the client JVM even when a
    # Gradle plugin does not explicitly forward system properties to JavaExec.
    existing = env.get("JAVA_TOOL_OPTIONS", "").strip()
    env["JAVA_TOOL_OPTIONS"] = " ".join(([existing] if existing else []) + properties)
    env.setdefault("ALSOFT_DRIVERS", "null")
    print("Capturing scenarios: " + ", ".join(scenarios), flush=True)
    try:
        completed = subprocess.run(
            command,
            cwd=root,
            env=env,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            timeout=timeout,
            check=False,
        )
    except subprocess.TimeoutExpired as exception:
        tail = (exception.stdout or "")[-4000:]
        raise SystemExit(f"runClient timed out after {timeout}s\n{tail}") from exception
    log_path = capture_dir / "capture.log"
    log_path.write_text(completed.stdout, encoding="utf-8")
    if completed.returncode:
        raise SystemExit(
            f"runClient exited with {completed.returncode}; see {log_path}"
        )
    missing = [str(capture_dir / f"{name}.png") for name in scenarios
               if not (capture_dir / f"{name}.png").is_file()]
    if missing:
        raise SystemExit("runClient finished without captures: " + ", ".join(missing))


def grayscale(rgb: np.ndarray) -> np.ndarray:
    values = rgb.astype(np.float32)
    return values[..., 0] * 0.299 + values[..., 1] * 0.587 + values[..., 2] * 0.114


def edge_mask(rgb: np.ndarray, threshold: float = 28.0) -> np.ndarray:
    gray = grayscale(rgb)
    dx = np.zeros_like(gray)
    dy = np.zeros_like(gray)
    dx[:, 1:] = np.abs(gray[:, 1:] - gray[:, :-1])
    dy[1:, :] = np.abs(gray[1:, :] - gray[:-1, :])
    return np.maximum(dx, dy) >= threshold


def dilate(mask: np.ndarray, radius: int = 1) -> np.ndarray:
    height, width = mask.shape
    padded = np.pad(mask, radius, mode="constant")
    result = np.zeros_like(mask)
    for offset_y in range(radius * 2 + 1):
        for offset_x in range(radius * 2 + 1):
            result |= padded[offset_y:offset_y + height, offset_x:offset_x + width]
    return result


def image_metrics(reference: np.ndarray, candidate: np.ndarray,
                  difference_threshold: int) -> dict[str, Any]:
    signed = candidate.astype(np.float32) - reference.astype(np.float32)
    absolute = np.abs(signed)
    maximum = np.max(absolute, axis=2)
    reference_edges = edge_mask(reference)
    candidate_edges = edge_mask(candidate)
    matched_reference = np.count_nonzero(reference_edges & dilate(candidate_edges))
    matched_candidate = np.count_nonzero(candidate_edges & dilate(reference_edges))
    recall = matched_reference / max(1, np.count_nonzero(reference_edges))
    precision = matched_candidate / max(1, np.count_nonzero(candidate_edges))
    f1 = 2 * precision * recall / max(1e-12, precision + recall)
    changed = maximum > difference_threshold
    return {
        "mae_rgb": round(float(absolute.mean()), 3),
        "rmse_rgb": round(float(np.sqrt(np.mean(signed * signed))), 3),
        "maximum_channel_delta": int(maximum.max(initial=0)),
        "changed_pixels": int(np.count_nonzero(changed)),
        "changed_fraction": round(float(np.mean(changed)), 6),
        "edge_precision_1px": round(float(precision), 5),
        "edge_recall_1px": round(float(recall), 5),
        "edge_f1_1px": round(float(f1), 5),
        "reference_edge_pixels": int(np.count_nonzero(reference_edges)),
        "candidate_edge_pixels": int(np.count_nonzero(candidate_edges)),
    }


def validate_box(box: Iterable[int], width: int, height: int, label: str) -> tuple[int, int, int, int]:
    values = tuple(int(value) for value in box)
    if len(values) != 4:
        raise ValueError(f"{label}: expected [left, top, right, bottom]")
    left, top, right, bottom = values
    if not (0 <= left < right <= width and 0 <= top < bottom <= height):
        raise ValueError(f"{label}: box {values} is outside {width}x{height}")
    return values


def difference_mask(reference: np.ndarray, candidate: np.ndarray,
                    threshold: int) -> tuple[np.ndarray, np.ndarray]:
    maximum = np.max(
        np.abs(candidate.astype(np.int16) - reference.astype(np.int16)), axis=2
    )
    return maximum > threshold, maximum


def connected_components(mask: np.ndarray, maximum_delta: np.ndarray,
                         minimum_pixels: int) -> list[dict[str, Any]]:
    """Return 8-connected thresholded-difference components, largest first."""
    height, width = mask.shape
    seen = np.zeros(mask.shape, dtype=bool)
    components: list[dict[str, Any]] = []
    for start_y, start_x in np.argwhere(mask):
        if seen[start_y, start_x]:
            continue
        queue: deque[tuple[int, int]] = deque([(int(start_y), int(start_x))])
        seen[start_y, start_x] = True
        count = 0
        total_delta = 0
        peak_delta = 0
        left = right = int(start_x)
        top = bottom = int(start_y)
        while queue:
            y, x = queue.popleft()
            count += 1
            value = int(maximum_delta[y, x])
            total_delta += value
            peak_delta = max(peak_delta, value)
            left, right = min(left, x), max(right, x)
            top, bottom = min(top, y), max(bottom, y)
            for next_y in range(max(0, y - 1), min(height, y + 2)):
                for next_x in range(max(0, x - 1), min(width, x + 2)):
                    if mask[next_y, next_x] and not seen[next_y, next_x]:
                        seen[next_y, next_x] = True
                        queue.append((next_y, next_x))
        if count >= minimum_pixels:
            components.append({
                "pixel_count": count,
                "box": [left, top, right + 1, bottom + 1],
                "mean_max_channel_delta": round(total_delta / count, 2),
                "peak_channel_delta": peak_delta,
            })
    components.sort(
        key=lambda component: (
            component["pixel_count"], component["peak_channel_delta"]
        ),
        reverse=True,
    )
    return components


def heatmap(reference: np.ndarray, candidate: np.ndarray) -> np.ndarray:
    maximum = np.max(
        np.abs(candidate.astype(np.int16) - reference.astype(np.int16)), axis=2
    ).astype(np.uint8)
    result = np.zeros((*maximum.shape, 3), dtype=np.uint8)
    result[..., 0] = np.clip(maximum.astype(np.int16) * 4, 0, 255)
    result[..., 1] = np.clip((maximum.astype(np.int16) - 32) * 2, 0, 255)
    result[..., 2] = maximum // 5
    return result


def save_image(array: np.ndarray, path: Path, zoom: int | None = None) -> None:
    image = Image.fromarray(array.astype(np.uint8), "RGB")
    if zoom and zoom > 1:
        image = image.resize(
            (image.width * zoom, image.height * zoom), Image.Resampling.NEAREST
        )
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def save_region_artifacts(reference: np.ndarray, candidate: np.ndarray,
                          target: Path, zoom: int) -> dict[str, str]:
    side_by_side = np.concatenate((reference, candidate), axis=1)
    paths = {
        "side_by_side": str(target.with_name(target.name + "-side-by-side.png")),
        "side_by_side_zoom": str(target.with_name(target.name + f"-side-by-side-zoom{zoom}.png")),
        "heatmap": str(target.with_name(target.name + "-heatmap.png")),
        "heatmap_zoom": str(target.with_name(target.name + f"-heatmap-zoom{zoom}.png")),
    }
    save_image(side_by_side, Path(paths["side_by_side"]))
    save_image(side_by_side, Path(paths["side_by_side_zoom"]), zoom)
    difference = heatmap(reference, candidate)
    save_image(difference, Path(paths["heatmap"]))
    save_image(difference, Path(paths["heatmap_zoom"]), zoom)
    return paths


def relative_artifacts(artifacts: dict[str, str], output_dir: Path) -> dict[str, str]:
    return {
        key: Path(value).resolve().relative_to(output_dir.resolve()).as_posix()
        for key, value in artifacts.items()
    }


def analyze_scenario(name: str, config: dict[str, Any], root: Path,
                     candidate_path: Path, output_dir: Path,
                     defaults: dict[str, Any], zoom: int) -> dict[str, Any]:
    reference_path = resolve_from_root(root, config["reference"])
    result: dict[str, Any] = {
        "name": name,
        "reference": str(reference_path),
        "candidate": str(candidate_path),
    }
    if config.get("note"):
        result["note"] = str(config["note"])
    missing = [str(path) for path in (reference_path, candidate_path) if not path.is_file()]
    if missing:
        result.update(status="missing", missing=missing)
        return result

    reference = np.asarray(Image.open(reference_path).convert("RGB"))
    candidate = np.asarray(Image.open(candidate_path).convert("RGB"))
    if reference.shape != candidate.shape:
        result.update(
            status="shape_mismatch",
            reference_shape=list(reference.shape),
            candidate_shape=list(candidate.shape),
        )
        return result

    height, width = reference.shape[:2]
    expected_size = defaults.get("image_size")
    if expected_size and [width, height] != expected_size:
        result["size_warning"] = f"expected {expected_size}, found {[width, height]}"
    threshold = int(config.get("difference_threshold", defaults["difference_threshold"]))
    minimum_pixels = int(config.get("minimum_cluster_pixels", defaults["minimum_cluster_pixels"]))
    padding = int(config.get("cluster_padding", defaults["cluster_padding"]))
    maximum_clusters = int(config.get(
        "maximum_clusters_per_region", defaults["maximum_clusters_per_region"]
    ))
    result.update(status="compared", difference_threshold=threshold, regions={})

    scenario_dir = output_dir / name
    for region_name, configured_box in config["regions"].items():
        box = validate_box(configured_box, width, height, f"{name}.{region_name}")
        left, top, right, bottom = box
        reference_region = reference[top:bottom, left:right]
        candidate_region = candidate[top:bottom, left:right]
        mask, maximum_delta = difference_mask(reference_region, candidate_region, threshold)

        for excluded in config.get("exclude", {}).get(region_name, []):
            ex_left, ex_top, ex_right, ex_bottom = validate_box(
                excluded, right - left, bottom - top, f"{name}.{region_name}.exclude"
            )
            mask[ex_top:ex_bottom, ex_left:ex_right] = False

        prefix = scenario_dir / region_name
        artifacts = relative_artifacts(
            save_region_artifacts(reference_region, candidate_region, prefix, zoom),
            output_dir,
        )
        components = connected_components(mask, maximum_delta, minimum_pixels)
        clusters: list[dict[str, Any]] = []
        for index, component in enumerate(components[:maximum_clusters], start=1):
            local_left, local_top, local_right, local_bottom = component["box"]
            crop_left = max(0, local_left - padding)
            crop_top = max(0, local_top - padding)
            crop_right = min(right - left, local_right + padding)
            crop_bottom = min(bottom - top, local_bottom + padding)
            crop_box = [crop_left, crop_top, crop_right, crop_bottom]
            absolute_box = [
                left + local_left, top + local_top,
                left + local_right, top + local_bottom,
            ]
            absolute_crop_box = [
                left + crop_left, top + crop_top,
                left + crop_right, top + crop_bottom,
            ]
            ref_crop = reference_region[crop_top:crop_bottom, crop_left:crop_right]
            candidate_crop = candidate_region[crop_top:crop_bottom, crop_left:crop_right]
            cluster_prefix = scenario_dir / "clusters" / f"{region_name}-{index:02d}"
            cluster_artifacts = relative_artifacts(
                save_region_artifacts(ref_crop, candidate_crop, cluster_prefix, zoom),
                output_dir,
            )
            clusters.append({
                **component,
                "box": absolute_box,
                "crop_box": absolute_crop_box,
                "local_crop_box": crop_box,
                "artifacts": cluster_artifacts,
            })
        result["regions"][region_name] = {
            "box": list(box),
            "metrics": image_metrics(reference_region, candidate_region, threshold),
            "cluster_count": len(components),
            "reported_cluster_count": len(clusters),
            "clusters": clusters,
            "artifacts": artifacts,
        }
    return result


def markdown_link(path: str, label: str) -> str:
    return f"[{label}]({path})"


def write_markdown(report: dict[str, Any], output_dir: Path) -> None:
    lines = [
        "# Legacy4J visual conformance",
        "",
        f"Generated: {report['generated_at']}",
        "",
        "Thresholded clusters localize contiguous UI discrepancies; boxes are "
        "`[left, top, right, bottom]` in full-screen pixels.",
        "",
    ]
    for scenario in report["scenarios"]:
        lines.extend([f"## {scenario['name']}", ""])
        if scenario.get("note"):
            lines.extend([f"> Note: {scenario['note']}", ""])
        if scenario["status"] != "compared":
            lines.append(f"Status: **{scenario['status']}**")
            if scenario.get("missing"):
                lines.append("")
                lines.append("Missing: " + ", ".join(f"`{path}`" for path in scenario["missing"]))
            lines.append("")
            continue
        lines.extend([
            "| Region | Changed | MAE | Edge F1 | Largest cluster | Artifacts |",
            "|---|---:|---:|---:|---:|---|",
        ])
        for name, region in scenario["regions"].items():
            metrics = region["metrics"]
            largest = region["clusters"][0]["pixel_count"] if region["clusters"] else 0
            artifacts = region["artifacts"]
            links = " · ".join((
                markdown_link(artifacts["side_by_side_zoom"], "zoom"),
                markdown_link(artifacts["heatmap"], "heatmap"),
            ))
            lines.append(
                f"| {name} | {metrics['changed_pixels']} ({metrics['changed_fraction']:.2%}) "
                f"| {metrics['mae_rgb']:.3f} | {metrics['edge_f1_1px']:.5f} "
                f"| {largest} | {links} |"
            )
        lines.append("")
        clusters = [
            (region_name, cluster)
            for region_name, region in scenario["regions"].items()
            for cluster in region["clusters"]
        ]
        clusters.sort(key=lambda item: item[1]["pixel_count"], reverse=True)
        if clusters:
            lines.extend(["Largest localized differences:", ""])
            for region_name, cluster in clusters[:8]:
                link = markdown_link(cluster["artifacts"]["side_by_side_zoom"], "crop")
                lines.append(
                    f"- `{region_name}` {cluster['box']}: {cluster['pixel_count']} pixels, "
                    f"peak {cluster['peak_channel_delta']} — {link}"
                )
            lines.append("")
    (output_dir / "report.md").write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    args = parse_args()
    manifest, root = load_manifest(args.manifest)
    scenarios = selected_scenarios(manifest, args.scenarios)
    output_dir = resolve_from_root(root, args.output)
    capture_dir = resolve_from_root(root, args.capture_dir)
    candidate_dir = (
        resolve_from_root(root, args.candidate_dir) if args.candidate_dir else None
    )
    if args.capture:
        run_capture(root, scenarios, capture_dir, args.capture_timeout)
        candidate_dir = capture_dir

    output_dir.mkdir(parents=True, exist_ok=True)
    defaults = {
        key: manifest[key]
        for key in (
            "image_size", "difference_threshold", "minimum_cluster_pixels",
            "cluster_padding", "maximum_clusters_per_region",
        )
    }
    report: dict[str, Any] = {
        "manifest": str(args.manifest.resolve()),
        "project_root": str(root),
        "generated_at": time.strftime("%Y-%m-%dT%H:%M:%S%z"),
        "scenarios": [],
    }
    for name in scenarios:
        config = manifest["scenarios"][name]
        candidate = (
            candidate_dir / f"{name}.png"
            if candidate_dir else resolve_from_root(root, config["candidate"])
        )
        print(f"Comparing {name}: {candidate}", flush=True)
        report["scenarios"].append(
            analyze_scenario(name, config, root, candidate, output_dir, defaults, args.zoom)
        )

    report_path = output_dir / "report.json"
    report_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    write_markdown(report, output_dir)
    compared = sum(item["status"] == "compared" for item in report["scenarios"])
    missing = sum(item["status"] != "compared" for item in report["scenarios"])
    changed = sum(
        region["metrics"]["changed_pixels"]
        for item in report["scenarios"] if item["status"] == "compared"
        for region in item["regions"].values()
    )
    print(
        f"Wrote {report_path} and {output_dir / 'report.md'} "
        f"({compared} compared, {missing} unavailable, {changed} region-pixels changed)"
    )
    if args.strict_missing and missing:
        return 2
    if args.fail_on_difference and changed:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
