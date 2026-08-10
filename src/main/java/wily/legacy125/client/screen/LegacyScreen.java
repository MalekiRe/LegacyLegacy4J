package wily.legacy125.client.screen;

import net.minecraft.src.Gui;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import org.lwjgl.input.Keyboard;
import wily.legacy125.input.PadButton;

/** Shared panel, focus, keyboard and controller behavior for the 1.2.5 rewrite. */
public abstract class LegacyScreen extends GuiScreen implements LegacyControllerScreen {
    protected final GuiScreen parent;
    protected String title;
    protected int focusedIndex;
    protected int panelLeft;
    protected int panelTop;
    protected int panelRight;
    protected int panelBottom;
    private boolean controllerFocusVisible;

    protected LegacyScreen(GuiScreen parent, String title) {
        this.parent = parent;
        this.title = title;
    }

    @Override
    public final void initGui() {
        controlList.clear();
        focusedIndex = 0;
        controllerFocusVisible = true;
        panelLeft = Math.max(12, width / 2 - 190);
        panelRight = Math.min(width - 12, width / 2 + 190);
        panelTop = Math.max(18, height / 2 - 112);
        panelBottom = Math.min(height - 22, height / 2 + 112);
        addLegacyControls();
        ensureFocusable(1);
        updateButtonFocus();
    }

    protected abstract void addLegacyControls();

    protected LegacyButton addButton(int id, int x, int y, int width, String text) {
        return addButton(id, x, y, width, 20, text);
    }

    protected LegacyButton addButton(int id, int x, int y, int width, int height, String text) {
        LegacyButton button = new LegacyButton(id, x, y, width, height, text);
        controlList.add(button);
        return button;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        drawLegacyBackground();
        drawBehindPanel(mouseX, mouseY, partialTick);
        if (drawPanel()) {
            drawPanelLayer(mouseX, mouseY, partialTick);
        }
        drawAbovePanel(mouseX, mouseY, partialTick);
        updateButtonFocus();
        super.drawScreen(mouseX, mouseY, partialTick);
        drawScreenContents(mouseX, mouseY, partialTick);
        drawControlHints();
    }

    protected void drawLegacyBackground() {
        drawGradientRect(0, 0, width, height, 0xFF1E2933, 0xFF0B0F13);
        for (int y = 0; y < height; y += 16) {
            Gui.drawRect(0, y, width, y + 1, 0x20FFFFFF);
        }
    }

    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
    }

    protected void drawBehindPanel(int mouseX, int mouseY, float partialTick) {
    }

    protected void drawPanelLayer(int mouseX, int mouseY, float partialTick) {
        LegacyTheme.panel(mc, panelLeft, panelTop, panelRight, panelBottom);
        Gui.drawRect(panelLeft + 3, panelTop + 24, panelRight - 3, panelTop + 26, 0xFFAEAEAE);
        drawCenteredString(fontRenderer, title, width / 2, panelTop + 8, LegacyTheme.TEXT);
    }

    protected void drawAbovePanel(int mouseX, int mouseY, float partialTick) {
    }

    protected boolean drawPanel() {
        return false;
    }

    protected void drawControlHints() {
        String hints = parent == null ? "A Select" : "A Select     B Back";
        LegacyTheme.drawCenteredControlHint(mc, hints, width / 2, height - 14);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        controllerFocusVisible = false;
        selectAt(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_LEFT) {
            controllerFocusVisible = true;
            moveFocus(-1);
        } else if (keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_RIGHT) {
            controllerFocusVisible = true;
            moveFocus(1);
        } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_SPACE) {
            controllerFocusVisible = true;
            activateFocused();
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            goBack();
        }
    }

    @Override
    public void onControllerButton(PadButton button) {
        controllerFocusVisible = true;
        if (button == PadButton.DPAD_UP || button == PadButton.DPAD_LEFT) {
            moveFocus(-1);
        } else if (button == PadButton.DPAD_DOWN || button == PadButton.DPAD_RIGHT) {
            moveFocus(1);
        } else if (button == PadButton.A) {
            activateFocused();
        } else if (button == PadButton.B || button == PadButton.BACK || button == PadButton.START) {
            goBack();
        }
    }

    protected void goBack() {
        if (parent != null) mc.displayGuiScreen(parent);
    }

    protected void moveFocus(int amount) {
        if (controlList.isEmpty()) return;
        int next = FocusNavigator.move(focusedIndex, amount, focusableButtons());
        if (next >= 0) focusedIndex = next;
    }

    protected void activateFocused() {
        GuiButton button = getFocusedButton();
        if (button != null && button.enabled && button.drawButton) actionPerformed(button);
    }

    protected GuiButton getFocusedButton() {
        if (focusedIndex < 0 || focusedIndex >= controlList.size()) return null;
        return (GuiButton) controlList.get(focusedIndex);
    }

    protected void setFocusedIndex(int index) {
        focusedIndex = Math.max(0, Math.min(index, controlList.size() - 1));
        ensureFocusable(1);
    }

    private void selectAt(int mouseX, int mouseY) {
        for (int i = 0; i < controlList.size(); i++) {
            Object value = controlList.get(i);
            if (value instanceof LegacyButton && ((LegacyButton) value).isInside(mouseX, mouseY)) {
                focusedIndex = i;
                return;
            }
        }
    }

    private void ensureFocusable(int direction) {
        if (controlList.isEmpty()) return;
        GuiButton current = (GuiButton) controlList.get(focusedIndex);
        if (current.enabled && current.drawButton) return;
        int next = FocusNavigator.move(focusedIndex, direction, focusableButtons());
        if (next >= 0) focusedIndex = next;
    }

    private void updateButtonFocus() {
        for (int i = 0; i < controlList.size(); i++) {
            Object value = controlList.get(i);
            if (value instanceof LegacyButton) {
                ((LegacyButton) value).setControllerFocused(controllerFocusVisible && i == focusedIndex);
            }
        }
    }

    /** Draws the SD Legacy4J Minecraft logo used by title and overlay menus. */
    protected void drawMinecraftLogo() {
        // Modern LogoRenderer is scaled by exactly 321/571 in Legacy4J SD.
        LegacyTheme.image(mc, "/legacy4j125/gui/title_minecraft.png", width / 2 - 72, 5, 144, 36);
        LegacyTheme.image(mc, "/legacy4j125/gui/title_edition.png", width / 2 - 36, 26, 72, 9);
    }

    private boolean[] focusableButtons() {
        boolean[] result = new boolean[controlList.size()];
        for (int i = 0; i < controlList.size(); i++) {
            GuiButton button = (GuiButton) controlList.get(i);
            result[i] = button.enabled && button.drawButton;
        }
        return result;
    }
}
