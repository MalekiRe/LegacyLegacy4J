# Upstream reference comparison

Reference environment: `Wilyicaro/Legacy-Minecraft` commit `e42c86911f0cbf1332e30393c419dcc8db9b0c76`, built and run as the `26.1.2-fabric` development client at 854×480. The comparison was performed on 2026-08-09 against the upstream title, survival inventory, recipe crafting, chest and furnace screens.

## Measured differences and responses

| Surface | Earlier backport | Upstream reference | Backport response |
| --- | --- | --- | --- |
| Title | Flat blue gradient, text logo, dark framed menu | Blurred panorama, Minecraft logo, independent light beveled buttons | Uses Minecraft's real panorama/logo, upstream-like focus colors and unframed title buttons |
| Inventory | Combined inventory/recipe screen | Compact centered light panel; player, armor, inventory and hotbar only | Split into `LegacyInventoryScreen`; crafting is a separate action |
| Crafting table | Dark two-column recipe list | Large light recipe UI with category/type tabs, grouped tiles and controller tooltips | Uses the seven upstream tabs/icons, grouped variants, a truthful 1.2.5 Crafting type rail, relocated inventory, and shared player/workbench recipe logic |
| Chest | Vanilla screen plus virtual cursor | Compact light chest panel with world visible | Dedicated Legacy layout over the original `ContainerChest` |
| Furnace | Vanilla furnace texture plus virtual cursor | Labeled ingredient/fuel/output layout with Legacy arrow | Dedicated layout and real upstream arrow sprites over the original `ContainerFurnace` |
| Modded inventory | Cursor only; replacement safety unclear | Upstream uses container mixins plus specialized screens | Unknown screens keep their renderer and get slot actions; exact replacement is opt-in through the registry |

The UI primitives packaged by this port are derived from the upstream repository's MIT-licensed panels, slots, arrows, category icons, and horizontal/vertical tab sprites. Their license is included as `LEGACY4J-UPSTREAM-LICENSE.txt` in the release JAR.

## Automated interaction coverage

`WorldContainerScenarioTest` uses the actual MCP 6.2 Minecraft classes. It creates an Anvil-backed world for each scenario and verifies:

1. a chest block is placed, a `TileEntityChest` is created, and coal is picked up through `ContainerChest.slotClick`;
2. a furnace block is placed, iron ore and coal are inserted, 201 furnace ticks produce an iron ingot, and the result is picked up through `ContainerFurnace.slotClick`;
3. a crafting table is placed, a real `ContainerWorkbench` crafts four sticks, and its result slot can be taken.

`SlotNavigatorTest` additionally uses irregular and off-screen slot geometry to cover the fallback used by modded inventories.

`LegacyCraftingTabsTest` checks representative 1.2.5 outputs across all seven categories. `LegacyRecipeGroupTest` loads the real `CraftingManager`, verifies every readable vanilla recipe has a category and stable group key, and covers material/armour variants sharing one tile.
