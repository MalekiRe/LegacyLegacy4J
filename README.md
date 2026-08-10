# Legacy4J for Minecraft 1.2.5

This is a source-level compatibility port of Legacy4J for Minecraft Java 1.2.5. It targets Risugami's ModLoader and MCP 6.2. Upstream-facing APIs preserve Legacy4J concepts while small adapters translate them to 1.2.5 rendering, containers and input.

## Implemented

- LWJGL2/JInput controller discovery with Steam virtual gamepad preference
- Steam Controller support through Steam Input's **Gamepad** output
- Dead-zone-aware left-stick movement and analog right-stick/trackpad camera control
- Jump, sneak, mine/attack, use/place, drop, hotbar, inventory, camera and pause bindings
- Controller-driven GUI cursor, select and back actions
- Legacy-styled title, save selection, world creation, multiplayer, pause, options, profile, help and confirmation screens
- Upstream-style recipe crafting with seven category tabs, grouped variants, a 1.2.5 type rail, availability filtering and real container clicks
- Compact inventory plus dedicated Legacy chest and furnace layouts using upstream Legacy4J panel, slot, focus and progress sprites
- Safe controller slot navigation for unknown modded `GuiContainer` screens without replacing their renderers
- Public container presentation and crafting-category registries for mod compatibility
- Legacy Console-style colored action prompts, selected-item names, flight state and autosave notice
- Save creation/loading/deletion, server add/edit/delete/direct-connect and a controller on-screen keyboard
- Controller-accessible gamma, GUI scale, HUD safe area, opacity, mappings, dead zones, sensitivity and inversion
- Player preview on the survival inventory/crafting screen
- Reproducible MCP 6.2 remapping and a Java 8-compatible output JAR

Modern-only systems such as resource albums, online skin services, post-processing shaders, World Host, and interfaces for blocks added after 1.2.5 are not claimed. The exact compatibility matrix is in [docs/FEATURE_MATRIX.md](docs/FEATURE_MATRIX.md).

## Build

Use JDK 17 or newer to run Gradle. The produced mod remains compatible with a Java 8 Minecraft runtime.

```sh
./gradlew clean test build
```

Install `build/libs/Legacy4J-1.2.5-0.4.0.jar` as a ModLoader 1.2.5 mod. Do not install the `-dev` or `-sources` JAR.

## Steam Controller setup

Minecraft is not a Steamworks title, so the mod reads Steam Input's virtual gamepad rather than linking against a game-specific Steamworks App ID.

1. Add your 1.2.5 launcher (Prism Launcher is suitable) to Steam as a non-Steam game.
2. Open that shortcut's **Controller Layout**.
3. Start with the **Gamepad** template.
4. Map the left pad/stick to **Left Joystick**, right pad to **Right Joystick**, triggers to **Left/Right Trigger**, and the face/grip buttons to the desired gamepad buttons.
5. Launch the Minecraft instance from that Steam shortcut. Open **Help & Options → Controller Settings → Controller Mapping & Diagnostics** to verify its axes and buttons.

Steam's Keyboard/Mouse template also works as a fallback, but those events are ordinary keyboard/mouse events and will not show controller prompts.

### Default layout

| Steam/Gamepad input | Action |
| --- | --- |
| Left stick/pad | Move |
| Right stick/pad | Look / menu cursor |
| A | Jump / select |
| B | Sneak / back |
| X | Recipe crafting |
| Y | Inventory |
| Left trigger | Use/place |
| Right trigger | Attack/mine |
| LB / RB | Previous/next hotbar slot |
| D-pad left/right | Previous/next hotbar slot |
| D-pad up | Drop item |
| Back | Change camera |
| Start | Pause |

Inside crafting, LB/RB changes category, LT/RT addresses the recipe-type rail, D-pad/stick left/right changes recipe group, up/down changes its variant, A prepares/crafts, Y filters to available recipes, and left-stick click switches to inventory slots. X clears the grid or quick-moves the focused inventory slot. Vanilla creative inventory remains available with the controller cursor.

## Configuration and troubleshooting

The first launch creates `.minecraft/config/legacy4j-1.2.5.properties`. Values of `-1` enable axis auto-detection. LWJGL2 controller names differ by operating system, so explicit axis indices are available when a driver reports unusual names.

If a trigger appears held at rest, change `combined_trigger_left_positive`, then set `axis_combined_triggers` explicitly if needed. If the wrong device is chosen, set `controller_index` to its zero-based LWJGL controller index. Restart Minecraft after editing the file.

On Linux, launch through Steam so Steam Input creates the virtual controller before Minecraft starts. Direct access to a physical Steam Controller without Steam Input depends on the installed JInput/udev stack and is not portable across old LWJGL2 distributions. See [docs/STEAM_CONTROLLER.md](docs/STEAM_CONTROLLER.md) for diagnostics and permission troubleshooting.

## Provenance

The compatibility design is based on the MIT-licensed [Legacy4J project](https://github.com/Wilyicaro/Legacy-Minecraft). Selected upstream UI primitives are included under its MIT license; platform integration is rewritten against MCP 6.2 and ModLoader. See [docs/REFERENCE_COMPARISON.md](docs/REFERENCE_COMPARISON.md) and [docs/COMPATIBILITY_API.md](docs/COMPATIBILITY_API.md).

## License

Original backport contributions are available under your choice of
`MIT OR Apache-2.0`. Upstream-derived Legacy4J portions remain under
the upstream MIT license. See [LICENSE](LICENSE), [LICENSES](LICENSES), and
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
