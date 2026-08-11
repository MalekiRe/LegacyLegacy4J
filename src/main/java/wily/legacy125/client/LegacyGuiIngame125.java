package wily.legacy125.client;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.GuiScreen;
import org.lwjgl.opengl.GL11;
import wily.legacy125.client.screen.LegacyCraftingScreen;

/**
 * Keeps Minecraft 1.2.5's gameplay HUD out of the player crafting view.
 *
 * <p>1.2.5 renders the in-game hotbar and armor/status icons behind every
 * in-world GUI. Modern Legacy4J suppresses that layer for its player crafting
 * screen. Workbench crafting and every other GUI continue through vanilla's
 * renderer unchanged.</p>
 */
public final class LegacyGuiIngame125 extends GuiIngame {
    private final Minecraft minecraft;

    public LegacyGuiIngame125(Minecraft minecraft) {
        super(minecraft);
        this.minecraft = minecraft;
    }

    public static void install(Minecraft minecraft) {
        if (!(minecraft.ingameGUI instanceof LegacyGuiIngame125)) {
            minecraft.ingameGUI = new LegacyGuiIngame125(minecraft);
        }
    }

    @Override
    public void renderGameOverlay(float partialTick, boolean screenOpen, int mouseX, int mouseY) {
        if (!suppressesGameplayHud(minecraft.currentScreen)) {
            super.renderGameOverlay(partialTick, screenOpen, mouseX, mouseY);
            return;
        }
        // Keep the vanilla call alive because ModLoader dispatches its GUI
        // tick hooks from inside this method. Mask only its framebuffer writes;
        // the Legacy crafting screen is composed immediately afterward.
        GL11.glColorMask(false, false, false, false);
        try {
            super.renderGameOverlay(partialTick, screenOpen, mouseX, mouseY);
        } finally {
            GL11.glColorMask(true, true, true, true);
        }
    }

    public static boolean suppressesGameplayHud(GuiScreen screen) {
        return screen instanceof LegacyCraftingScreen
                && ((LegacyCraftingScreen) screen).isPlayerCrafting();
    }
}
