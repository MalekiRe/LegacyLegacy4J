package wily.legacy125.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;

public class LegacyButton extends GuiButton {
    protected final int buttonWidth;
    protected final int buttonHeight;
    protected boolean controllerFocused;

    public LegacyButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
        this.buttonWidth = width;
        this.buttonHeight = height;
    }

    public void setControllerFocused(boolean focused) {
        controllerFocused = focused;
    }

    public boolean isInside(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + buttonWidth && mouseY < yPosition + buttonHeight;
    }

    protected boolean isHighlighted(int mouseX, int mouseY) {
        return enabled && (controllerFocused || isInside(mouseX, mouseY));
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!drawButton) return;
        boolean highlighted = isHighlighted(mouseX, mouseY);
        LegacyTheme.button(minecraft, xPosition, yPosition,
                xPosition + buttonWidth, yPosition + buttonHeight, highlighted);
        int textColor = enabled ? (highlighted ? 0xFFFF55 : 0xFFFFFF) : 0xA0A0A0;
        int textX = xPosition + (buttonWidth - minecraft.fontRenderer.getStringWidth(displayString)) / 2;
        int textY = yPosition + (buttonHeight - 8) / 2;
        minecraft.fontRenderer.drawStringWithShadow(displayString, textX, textY, textColor);
    }
}
