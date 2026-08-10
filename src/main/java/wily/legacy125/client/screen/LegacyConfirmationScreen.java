package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

public final class LegacyConfirmationScreen extends LegacyPanelScreen {
    public interface Callback {
        void answer(boolean confirmed);
    }

    private final String message;
    private final Callback callback;

    public LegacyConfirmationScreen(GuiScreen parent, String title, String message, Callback callback) {
        super(parent, title);
        this.message = message;
        this.callback = callback;
    }

    @Override
    protected void addLegacyControls() {
        int y = panelBottom - 50;
        addButton(1, width / 2 - 124, y, 120, "Yes");
        addButton(0, width / 2 + 4, y, 120, "No");
        setFocusedIndex(1);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        callback.answer(button.id == 1);
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        fontRenderer.drawSplitString(message, panelLeft + 18, panelTop + 42,
                panelRight - panelLeft - 36, 0xFFFFFFFF);
    }
}
