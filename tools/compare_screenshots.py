#!/usr/bin/env python3
"""Pixel/edge comparison for Legacy4J reference and 1.2.5 captures."""

import argparse
import json
from pathlib import Path

import numpy as np
from PIL import Image


PRESETS = {
    "inventory": {
        "panel": (296, 100, 556, 380),
        "equipment": (308, 116, 512, 222),
        "label": (308, 228, 390, 248),
        "main_slots": (308, 250, 544, 330),
        "hotbar": (308, 338, 544, 358),
    },
    "crafting": {
        "whole_ui": (180, 94, 722, 382),
        "tabs": (216, 94, 722, 160),
        "tab_panel_seam": (210, 132, 722, 154),
        "outer_panel": (216, 140, 722, 382),
        "recipe_strip": (230, 176, 708, 216),
        "left_panel": (230, 228, 464, 370),
        "right_panel": (472, 228, 706, 370),
    },
    "chest": {
        "whole_ui": (296, 112, 556, 370),
        "title_and_storage": (308, 122, 544, 220),
        "inventory": (308, 226, 544, 334),
        "hotbar": (308, 340, 544, 362),
    },
    "furnace": {
        "whole_ui": (296, 94, 556, 384),
        "process": (344, 120, 542, 228),
        "inventory": (308, 236, 544, 336),
        "hotbar": (308, 342, 544, 372),
    },
    "pause": {
        "logo": (280, 6, 574, 68),
        "button_stack": (290, 108, 564, 324),
        "bottom_hint": (62, 420, 166, 446),
    },
}


def grayscale(rgb):
    return rgb[..., 0] * 0.299 + rgb[..., 1] * 0.587 + rgb[..., 2] * 0.114


def edge_mask(rgb, threshold=28.0):
    gray = grayscale(rgb.astype(np.float32))
    dx = np.zeros_like(gray)
    dy = np.zeros_like(gray)
    dx[:, 1:] = np.abs(gray[:, 1:] - gray[:, :-1])
    dy[1:, :] = np.abs(gray[1:, :] - gray[:-1, :])
    return np.maximum(dx, dy) >= threshold


def dilate(mask, radius=1):
    result = mask.copy()
    for y in range(-radius, radius + 1):
        for x in range(-radius, radius + 1):
            shifted = np.roll(np.roll(mask, y, axis=0), x, axis=1)
            if y < 0:
                shifted[y:, :] = False
            elif y > 0:
                shifted[:y, :] = False
            if x < 0:
                shifted[:, x:] = False
            elif x > 0:
                shifted[:, :x] = False
            result |= shifted
    return result


def metrics(reference, candidate):
    delta = candidate.astype(np.float32) - reference.astype(np.float32)
    abs_delta = np.abs(delta)
    ref_edges = edge_mask(reference)
    candidate_edges = edge_mask(candidate)
    candidate_near = dilate(candidate_edges)
    ref_near = dilate(ref_edges)
    matched_ref = np.count_nonzero(ref_edges & candidate_near)
    matched_candidate = np.count_nonzero(candidate_edges & ref_near)
    recall = matched_ref / max(1, np.count_nonzero(ref_edges))
    precision = matched_candidate / max(1, np.count_nonzero(candidate_edges))
    f1 = 2 * precision * recall / max(1e-12, precision + recall)
    return {
        "mae_rgb": round(float(abs_delta.mean()), 3),
        "rmse_rgb": round(float(np.sqrt(np.mean(delta * delta))), 3),
        "pixels_with_delta_gt_24": round(float(np.mean(np.max(abs_delta, axis=2) > 24)), 5),
        "edge_precision_1px": round(float(precision), 5),
        "edge_recall_1px": round(float(recall), 5),
        "edge_f1_1px": round(float(f1), 5),
        "reference_edge_pixels": int(np.count_nonzero(ref_edges)),
        "candidate_edge_pixels": int(np.count_nonzero(candidate_edges)),
    }


def save_visuals(reference, candidate, output_prefix):
    output_prefix.parent.mkdir(parents=True, exist_ok=True)
    side = np.concatenate((reference, candidate), axis=1)
    side_image = Image.fromarray(side)
    side_image.save(str(output_prefix) + "-side-by-side.png")
    side_image.resize((side_image.width * 3, side_image.height * 3), Image.Resampling.NEAREST).save(
        str(output_prefix) + "-side-by-side-zoom3.png"
    )

    delta = np.abs(candidate.astype(np.int16) - reference.astype(np.int16)).astype(np.uint8)
    heat = np.clip(delta.astype(np.int16) * 4, 0, 255).astype(np.uint8)
    heat_image = Image.fromarray(heat)
    heat_image.save(str(output_prefix) + "-diff-x4.png")
    heat_image.resize((heat_image.width * 3, heat_image.height * 3), Image.Resampling.NEAREST).save(
        str(output_prefix) + "-diff-x4-zoom3.png"
    )

    ref_edges = edge_mask(reference)
    candidate_edges = edge_mask(candidate)
    overlay = np.zeros(reference.shape, dtype=np.uint8)
    overlay[ref_edges] = (255, 50, 50)
    overlay[candidate_edges] = np.maximum(overlay[candidate_edges], (30, 220, 255))
    overlay[ref_edges & candidate_edges] = (255, 255, 255)
    Image.fromarray(overlay).save(str(output_prefix) + "-edges.png")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("preset", choices=sorted(PRESETS))
    parser.add_argument("reference", type=Path)
    parser.add_argument("candidate", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()

    reference_image = np.asarray(Image.open(args.reference).convert("RGB"))
    candidate_image = np.asarray(Image.open(args.candidate).convert("RGB"))
    if reference_image.shape != candidate_image.shape:
        raise SystemExit("image shapes differ: %r vs %r" % (reference_image.shape, candidate_image.shape))

    report = {"preset": args.preset, "regions": {}}
    for name, box in PRESETS[args.preset].items():
        left, top, right, bottom = box
        reference = reference_image[top:bottom, left:right]
        candidate = candidate_image[top:bottom, left:right]
        report["regions"][name] = {"box": box, **metrics(reference, candidate)}
        save_visuals(reference, candidate, args.output.with_name(args.output.name + "-" + name))

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.with_suffix(".json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
