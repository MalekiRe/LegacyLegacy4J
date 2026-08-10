# Minecraft 1.2.5 Backport Matrix

This matrix translates Legacy4J's current feature list into requirements that make sense on the Minecraft 1.2.5 codebase. A feature is not considered complete merely because the mod starts.

| Area | 1.2.5 requirement | Status |
| --- | --- | --- |
| Controller support | Gamepad discovery, reconnection rescan, analog look, gameplay bindings, menu navigation, rebinding, diagnostics and Steam Input instructions | Implemented; physical Steam hardware verification pending |
| Interfaces | Legacy-styled title, play, pause, options, confirmation, help and update screens; controller focus on every replacement screen | Core flows implemented; title uses the real panorama/logo and UI panels use upstream Legacy4J sprites |
| Containers | Chest, furnace and inventory layouts; safe behavior for modded inventories | Dedicated vanilla layouts plus non-invasive slot focus for every unknown `GuiContainer`; registered mod adapters can opt into exact reskinning |
| Crafting | Recipe-oriented controller screen for player and workbench recipes, ingredient availability and safe inventory/container behavior | Seven upstream category tabs/icons, grouped variants, available filter and 1.2.5 Crafting type rail implemented over live `ContainerPlayer`/`ContainerWorkbench` window clicks; mods can register categories |
| HUD | Console action prompts, configurable opacity/margins, selected-item tooltip, autosave activity and controller type | Implemented |
| Tooltips | Item name/details and contextual controller actions | Selected-item names and contextual actions implemented; extended item lore is unchanged from vanilla |
| Creative flight | Legacy Console double-jump flight toggle and vertical controls in creative | Implemented through the vanilla 1.2.5 jump/sneak flight state, with a HUD indicator |
| Gamma | Controller-accessible gamma option using 1.2.5's existing gamma field | Implemented |
| GUI scale | Controller-accessible GUI scale and safe-area/HUD scaling | Implemented |
| Animated character | Player preview on inventory/crafting and pause surfaces where 1.2.5 rendering permits | Implemented on survival inventory/crafting |
| World management | Controller-first save list, create/load/delete confirmation and game-mode metadata | Implemented |
| How to Play | Offline pages covering controls, survival, crafting and Steam Controller setup | Implemented |
| Update notes | Offline backport version and implemented-feature notes | Implemented |
| Skin selection | 1.2.5-compatible local/username skin choice without modern skin-service assumptions | Username/profile identity implemented; no online skin picker is available in the 1.2.5 protocol |
| Multiplayer | Controller server list, add/edit/delete, direct connect and connection handoff | Implemented |
| On-screen keyboard | Controller-only entry for world names, seeds, profile names and server fields | Implemented |
| Superflat customization | Safe world-type selection using 1.2.5's built-in generator | Default/Superflat selection implemented; layer customization is not |
| Tutorial world | Bundled/generated tutorial only after world creation and save compatibility are verified | Not implemented; no save template is bundled |
| Open LAN / World Host | 1.2.5 has no integrated server or modern World Host API | Not applicable |
| Modern content screens | Loom, smithing, stonecutter, advancements, resource albums and other post-1.2.5 content | Not applicable |

## Release gates

1. `./gradlew clean test build` passes and the installable JAR is remapped to 1.2.5 production names.
2. A real 1.2.5 client reaches the replacement title screen with ModLoader reporting the mod loaded.
3. Focus navigation and input edge behavior pass their simulation tests; custom screens are designed for semantic controller events.
4. Gameplay input transitions release cleanly when pausing, disconnecting or opening a GUI.
5. The Steam Input path is verified with a virtual gamepad trace or explicitly recorded as awaiting physical hardware verification.
6. Real-world tests place chest, furnace and crafting-table blocks; furnace fuel/cook/output and container pickup execute through Minecraft 1.2.5 classes.
