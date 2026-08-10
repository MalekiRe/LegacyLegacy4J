# Legacy4J-shaped compatibility API

The backport keeps upstream concepts above a narrow Minecraft 1.2.5 bridge. Code that decides focus, layout, tooltips, recipe selection or container actions should not depend directly on MCP fields unless it is the adapter responsible for that field.

## Containers

`LegacyContainers` is the public registration point. A registered `LegacyContainerAdapter` recognizes a mod's existing `GuiContainer` and returns a `LegacyContainerLayout`. The replacement `LegacyContainerScreen` reuses the original live `Container`, including its window id, slots, inventories and `windowClick` behavior.

```java
LegacyContainers.registerFirst(new LegacyContainerAdapter() {
    public boolean supports(GuiContainer screen) {
        return screen instanceof GuiMyMachine;
    }

    public LegacyContainerLayout describe(GuiContainer screen) {
        return new LegacyContainerLayout(
                LegacyContainerLayout.Style.GENERIC,
                "My Machine", 176, 166);
    }
});
```

Do not register an adapter merely to make a modded inventory usable. Unknown `GuiContainer` screens already receive non-invasive controller slot navigation:

- D-pad or left stick selects the nearest slot geometrically.
- A performs a normal primary slot click.
- X performs a quick-move click.
- B or Start closes the screen.
- The original mod screen continues drawing its background, progress bars and custom fields.

Register only when the native screen can safely be replaced by the shared Legacy presentation. Future adapters can add specialized progress and button capabilities without changing controller semantics.

## Presentation boundary

`LegacyTheme` implements upstream's panel, recessed panel, chest slot, selection and furnace-arrow primitives on LWJGL 2. The source assets are encoded textually for reproducible patches, decoded during `processResources`, and shipped as ordinary PNG resources in the final JAR.

`LegacyControllerScreen` is the semantic-input boundary for replacement screens. Game code emits `PadButton` actions; screens decide what those actions mean. Foreign container screens stay outside that interface and use the safe slot fallback above.

## Crafting categories

`LegacyCraftingTabs` exposes the seven Legacy Console categories and accepts additional mod categories. A matcher sees the recipe output, so it does not need to know the private MCP fields of shaped or shapeless recipes.

```java
LegacyCraftingTabs.register(new LegacyCraftingTab(
        "my_mod_machines", "My Mod Machines",
        "/my_mod/gui/machine_tab.png",
        new LegacyCraftingTab.Matcher() {
            public boolean matches(ItemStack output) {
                return output.itemID == MyMod.machineItem.shiftedIndex;
            }
        }));
```

Registered categories take precedence over the built-in classifier, including its broad Structures fallback. Recipes remain the original `IRecipe` objects and crafting still flows through the live player or workbench container. The vertical rail contains only Crafting on 1.2.5; upstream's banner, firework and dye-custom recipe types depend on game systems that did not exist yet.

### Recipe families

`LegacyCraftingGroups` groups outputs by semantic family. Vanilla subclasses
automatically place modded pickaxes, axes, shovels, hoes and swords into their
existing dropdowns. Class/name fallbacks cover common ModLoader-era tools such
as sickles, handsaws, drills and wrenches, while material prefixes collapse
families such as RedPower gem tools and BuildCraft pipes.

Pack-specific semantic families are resolved before generic prefix stripping.
The bundled rules group RedPower jacketed wire/cable/bluewire by jacket
material, EE2 collector and anti-matter relay tiers by machine, and BuildCraft
engines by engine type. This ordering prevents similarly named but unrelated
items, such as RedPower's plain Relay, from entering an EE2 tier dropdown.

A mod can override the inferred family without depending on screen internals:

```java
LegacyCraftingGroups.registerFirst(new LegacyCraftingGroups.Resolver() {
    public String resolve(ItemStack output) {
        return output.itemID == MyMod.part.shiftedIndex
                ? "my_mod:machine_parts" : null;
    }
});
```

The bridge also reflectively reads fixed-output `IRecipe` implementations such
as IC2 `AdvRecipe` and `AdvShapelessRecipe`. Dynamic recipes whose output exists
only after inspecting a live crafting grid (for example RedPower cover cutting)
remain available through the normal grid but cannot be enumerated as a finite
recipe-book list.

## Compatibility rule

Port upstream algorithms and state transitions where 1.2.5 has the same invariant. Reimplement the platform edge where modern Minecraft types do not exist. In particular:

- retain the live `Container` instead of reconstructing a menu;
- perform inventory changes through `PlayerController.windowClick`;
- describe screens and progress rather than exposing MCP fields to common logic;
- treat an unregistered mod container as functional before attempting to reskin it.
