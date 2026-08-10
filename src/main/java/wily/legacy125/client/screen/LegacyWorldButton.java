package wily.legacy125.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Gui;

/** SD SaveRenderableList row: compact icon followed by a left-aligned world name. */
final class LegacyWorldButton extends LegacyButton {
    LegacyWorldButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!drawButton) return;
        boolean highlighted = isHighlighted(mouseX, mouseY);
        LegacyTheme.button(minecraft, xPosition, yPosition,
                xPosition + buttonWidth, yPosition + buttonHeight, highlighted);

        int iconSize = 11;
        int iconX = xPosition + (buttonHeight - iconSize) / 2;
        int iconY = yPosition + (buttonHeight - iconSize) / 2;
        // Minecraft 1.2.5 predates per-save icon.png files. Keep the exact
        // upstream icon box and provide a deterministic grass-world fallback.
        Gui.drawRect(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xFF78A7D1);
        Gui.drawRect(iconX, iconY + 6, iconX + iconSize, iconY + iconSize, 0xFF80613A);
        Gui.drawRect(iconX, iconY + 5, iconX + iconSize, iconY + 7, 0xFF5D8F36);
        Gui.drawRect(iconX, iconY, iconX + iconSize, iconY + 1, 0xFF303030);
        Gui.drawRect(iconX, iconY + iconSize - 1, iconX + iconSize, iconY + iconSize, 0xFF303030);
        Gui.drawRect(iconX, iconY, iconX + 1, iconY + iconSize, 0xFF303030);
        Gui.drawRect(iconX + iconSize - 1, iconY, iconX + iconSize, iconY + iconSize, 0xFF303030);

        int textColor = enabled ? (highlighted ? 0xFFFF55 : 0xFFFFFF) : 0xA0A0A0;
        int textY = yPosition + (buttonHeight - 8) / 2;
        minecraft.fontRenderer.drawStringWithShadow(displayString, xPosition + 22, textY, textColor);
    }
}
