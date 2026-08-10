package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import org.lwjgl.input.Keyboard;
import wily.legacy125.input.PadButton;

public final class LegacyKeyboardScreen extends LegacyPanelScreen {
    public interface Callback {
        void accept(String value);
    }

    private static final String KEYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_ .";
    private static final int COLUMNS = 13;
    private final Callback callback;
    private final int maximumLength;
    private String value;

    public LegacyKeyboardScreen(GuiScreen parent, String title, String initialValue, int maximumLength,
                                Callback callback) {
        super(parent, title);
        this.value = initialValue == null ? "" : initialValue;
        this.maximumLength = maximumLength;
        this.callback = callback;
    }

    @Override
    protected void addLegacyControls() {
        int buttonWidth = 25;
        int totalWidth = COLUMNS * buttonWidth;
        int left = width / 2 - totalWidth / 2;
        int top = panelTop + 58;
        for (int index = 0; index < KEYS.length(); index++) {
            int row = index / COLUMNS;
            int column = index % COLUMNS;
            addButton(1000 + index, left + column * buttonWidth, top + row * 22, buttonWidth - 1,
                    String.valueOf(KEYS.charAt(index)));
        }
        int y = top + 4 * 22;
        addButton(2, left, y, 105, "Space");
        addButton(3, left + 110, y, 105, "Backspace");
        addButton(1, left + totalWidth - 105, y, 105, "Done");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 1000) append(KEYS.charAt(button.id - 1000));
        if (button.id == 2) append(' ');
        if (button.id == 3) backspace();
        if (button.id == 1) {
            callback.accept(value.trim());
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            goBack();
        } else if (keyCode == Keyboard.KEY_RETURN) {
            callback.accept(value.trim());
            mc.displayGuiScreen(parent);
        } else if (keyCode == Keyboard.KEY_BACK) {
            backspace();
        } else if (!Character.isISOControl(character)) {
            append(character);
        }
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.DPAD_UP) {
            moveFocus(-COLUMNS);
        } else if (button == PadButton.DPAD_DOWN) {
            moveFocus(COLUMNS);
        } else if (button == PadButton.X) {
            backspace();
        } else if (button == PadButton.Y) {
            append(' ');
        } else {
            super.onControllerButton(button);
        }
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        int left = panelLeft + 20;
        int right = panelRight - 20;
        net.minecraft.src.Gui.drawRect(left, panelTop + 34, right, panelTop + 54, 0xFF111111);
        String shown = value + (((System.currentTimeMillis() / 500L) & 1L) == 0L ? "_" : "");
        fontRenderer.drawStringWithShadow(shown, left + 5, panelTop + 40, 0xFFFFFFFF);
    }

    @Override
    protected void drawControlHints() {
        LegacyTheme.drawCenteredControlHint(mc,
                "A Type     X Backspace     Y Space     B Cancel", width / 2, height - 14);
    }

    private void append(char character) {
        if (value.length() < maximumLength) value += character;
    }

    private void backspace() {
        if (value.length() > 0) value = value.substring(0, value.length() - 1);
    }
}
