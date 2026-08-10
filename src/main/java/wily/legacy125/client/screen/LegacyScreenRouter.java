package wily.legacy125.client.screen;

import net.minecraft.src.GuiIngameMenu;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiCreateWorld;
import net.minecraft.src.GuiCrafting;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiMultiplayer;
import net.minecraft.src.GuiScreen;
import wily.legacy125.api.LegacyContainerLayout;
import wily.legacy125.api.LegacyContainers;

public final class LegacyScreenRouter {
    private LegacyScreenRouter() {
    }

    public static GuiScreen replacement(GuiScreen current) {
        if (current == null) return null;
        if (current.getClass() == GuiMainMenu.class) return new LegacyTitleScreen();
        if (current.getClass() == GuiIngameMenu.class) return new LegacyPauseScreen();
        if (current.getClass() == GuiCreateWorld.class) return new LegacyCreateWorldScreen(new LegacyPlayScreen(new LegacyTitleScreen()));
        if (current.getClass() == GuiMultiplayer.class) return new LegacyMultiplayerScreen(new LegacyTitleScreen());
        if (current.getClass() == GuiInventory.class) return new LegacyInventoryScreen(((GuiInventory) current).inventorySlots);
        if (current.getClass() == GuiCrafting.class) return new LegacyCraftingScreen(((GuiCrafting) current).inventorySlots);
        if (current instanceof GuiContainer) {
            LegacyContainerLayout layout = LegacyContainers.describe((GuiContainer) current);
            if (layout != null) return new LegacyContainerScreen(((GuiContainer) current).inventorySlots, layout);
        }
        return current;
    }
}
