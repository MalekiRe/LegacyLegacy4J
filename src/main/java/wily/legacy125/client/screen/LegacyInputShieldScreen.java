package wily.legacy125.client.screen;

import net.minecraft.src.GuiScreen;

/**
 * Invisible non-pausing screen that absorbs Steam Input's continuously repeated
 * synthetic Escape events while controller gameplay continues behind it.
 */
public final class LegacyInputShieldScreen extends GuiScreen {
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        // The world and Legacy4J HUD remain the only visible layers.
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        // Intentionally consume Escape and any synthetic keyboard mapping.
    }
}
