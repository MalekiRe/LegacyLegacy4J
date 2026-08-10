package wily.legacy125.api;

import net.minecraft.src.GuiContainer;

/**
 * Extension point for containers supplied by other 1.2.5 mods.
 *
 * Adapters describe presentation only. The compatibility screen keeps the mod's
 * original live {@link net.minecraft.src.Container}, window id, slots and click
 * handling intact.
 */
public interface LegacyContainerAdapter {
    boolean supports(GuiContainer screen);

    LegacyContainerLayout describe(GuiContainer screen);
}
