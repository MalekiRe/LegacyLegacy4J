package wily.legacy125.client.screen;

import net.minecraft.src.Container;
import net.minecraft.src.Slot;
import net.minecraft.src.TileEntityFurnace;
import org.lwjgl.input.Keyboard;
import wily.legacy125.api.LegacyContainerLayout;
import wily.legacy125.input.PadButton;

/** Legacy presentation over an existing live vanilla or mod container. */
public final class LegacyContainerScreen extends LegacyGuiContainer125 implements LegacyControllerScreen {
    private final LegacyContainerLayout layout;
    private final int[] oldX;
    private final int[] oldY;
    private int focusedSlot;
    private boolean controllerFocusVisible;

    public LegacyContainerScreen(Container container, LegacyContainerLayout layout) {
        super(container);
        if (layout == null) throw new IllegalArgumentException("layout");
        this.layout = layout;
        xSize = layout.width();
        ySize = layout.height();
        allowUserInput = true;
        oldX = new int[container.inventorySlots.size()];
        oldY = new int[container.inventorySlots.size()];
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = (Slot) container.inventorySlots.get(i);
            oldX[i] = slot.xDisplayPosition;
            oldY[i] = slot.yDisplayPosition;
        }
        arrangeKnownLayout();
    }

    @Override
    public void initGui() {
        super.initGui();
        focusedSlot = SlotNavigator.firstVisible(inventorySlots.inventorySlots);
        controllerFocusVisible = true;
    }

    @Override
    public void drawDefaultBackground() {
        // Console container screens retain the undimmed world behind the panel.
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        net.minecraft.src.RenderItem previous = itemRenderer;
        itemRenderer = LegacyScaledRenderItem.INSTANCE;
        LegacyScaledRenderItem.begin(this);
        try {
            super.drawScreen(mouseX, mouseY, partialTick);
        } finally {
            LegacyScaledRenderItem.end();
            itemRenderer = previous;
        }
    }

    @Override
    public int legacySlotSize(int x, int y) {
        if (layout.style() == LegacyContainerLayout.Style.FURNACE) {
            for (int i = 0; i < Math.min(3, inventorySlots.inventorySlots.size()); i++) {
                Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
                if (slot.xDisplayPosition == x && slot.yDisplayPosition == y) return i == 2 ? 21 : 17;
            }
        }
        if (layout.style() == LegacyContainerLayout.Style.CHEST
                || layout.style() == LegacyContainerLayout.Style.FURNACE) return 13;
        return 16;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        LegacyTheme.panel(mc, guiLeft, guiTop, guiLeft + xSize, guiTop + ySize);
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.xDisplayPosition > -100 && slot.yDisplayPosition > -100) {
                int size = legacySlotSize(slot.xDisplayPosition, slot.yDisplayPosition);
                LegacyTheme.slot(mc, guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, size);
            }
        }
        if (layout.style() == LegacyContainerLayout.Style.FURNACE) drawFurnaceProgress();
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        LegacyTheme.drawString(mc, layout.title(), 7, 5, LegacyTheme.TEXT);
        int inventoryY = layout.style() == LegacyContainerLayout.Style.FURNACE ? 71
                : layout.style() == LegacyContainerLayout.Style.CHEST ? 56 + chestYDiff() : ySize - 96;
        LegacyTheme.drawString(mc, "Inventory", 7, inventoryY, LegacyTheme.TEXT);
        if (layout.style() == LegacyContainerLayout.Style.FURNACE) {
            int ingredientWidth = LegacyTheme.stringWidth("Ingredient");
            int fuelWidth = LegacyTheme.stringWidth("Fuel");
            LegacyTheme.drawString(mc, "Ingredient", 57 - ingredientWidth, 19, LegacyTheme.TEXT);
            LegacyTheme.drawString(mc, "Fuel", 57 - fuelWidth, 53, LegacyTheme.TEXT);
        }
        Slot controllerSlot = focusedSlot >= 0 && focusedSlot < inventorySlots.inventorySlots.size()
                ? (Slot) inventorySlots.inventorySlots.get(focusedSlot) : null;
        drawLegacySlotFocus(controllerSlot, controllerFocusVisible);
    }

    private void drawFurnaceProgress() {
        TileEntityFurnace furnace = furnace();
        int arrow = furnace == null ? 0 : furnace.getCookProgressScaled(16);
        int burn = furnace == null ? 0 : furnace.getBurnTimeRemainingScaled(14);
        LegacyTheme.smallArrow(mc, guiLeft + 82, guiTop + 33, arrow);
        LegacyTheme.flame(mc, guiLeft + 63, guiTop + 34, burn);
    }

    private void arrangeKnownLayout() {
        if (layout.style() == LegacyContainerLayout.Style.CHEST) arrangeChest();
        else if (layout.style() == LegacyContainerLayout.Style.FURNACE) arrangeFurnace();
    }

    private void arrangeChest() {
        int containerSlots = Math.max(0, inventorySlots.inventorySlots.size() - 36);
        int rows = Math.max(1, containerSlots / 9);
        int yDiff = (rows - 3) * 13;
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
            if (i < containerSlots) {
                slot.xDisplayPosition = 7 + i % 9 * 13;
                slot.yDisplayPosition = 15 + i / 9 * 13;
            } else {
                int player = i - containerSlots;
                slot.xDisplayPosition = 7 + (player < 27 ? player % 9 : player - 27) * 13;
                slot.yDisplayPosition = player < 27 ? 66 + player / 9 * 13 + yDiff : 111 + yDiff;
            }
        }
    }

    private void arrangeFurnace() {
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
            if (i == 0) {
                slot.xDisplayPosition = 62;
                slot.yDisplayPosition = 16;
            } else if (i == 1) {
                slot.xDisplayPosition = 62;
                slot.yDisplayPosition = 50;
            } else if (i == 2) {
                slot.xDisplayPosition = 102;
                slot.yDisplayPosition = 30;
            } else {
                int player = i - 3;
                slot.xDisplayPosition = 7 + (player < 27 ? player % 9 : player - 27) * 13;
                slot.yDisplayPosition = player < 27 ? 81 + player / 9 * 13 : 125;
            }
        }
    }

    private int chestYDiff() {
        int rows = Math.max(1, (inventorySlots.inventorySlots.size() - 36) / 9);
        return (rows - 3) * 13;
    }

    @Override
    public void onGuiClosed() {
        for (int i = 0; i < inventorySlots.inventorySlots.size() && i < oldX.length; i++) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
            slot.xDisplayPosition = oldX[i];
            slot.yDisplayPosition = oldY[i];
        }
        super.onGuiClosed();
    }

    private TileEntityFurnace furnace() {
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.inventory instanceof TileEntityFurnace) return (TileEntityFurnace) slot.inventory;
        }
        return null;
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) close();
        else if (keyCode == Keyboard.KEY_UP) { controllerFocusVisible = true; move(PadButton.DPAD_UP); }
        else if (keyCode == Keyboard.KEY_DOWN) { controllerFocusVisible = true; move(PadButton.DPAD_DOWN); }
        else if (keyCode == Keyboard.KEY_LEFT) { controllerFocusVisible = true; move(PadButton.DPAD_LEFT); }
        else if (keyCode == Keyboard.KEY_RIGHT) { controllerFocusVisible = true; move(PadButton.DPAD_RIGHT); }
        else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_SPACE) {
            controllerFocusVisible = true;
            click(false);
        }
        else super.keyTyped(character, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        controllerFocusVisible = false;
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onControllerButton(PadButton button) {
        controllerFocusVisible = true;
        if (button == PadButton.B || button == PadButton.BACK || button == PadButton.START) close();
        else if (button == PadButton.A) click(0, false);
        else if (button == PadButton.X) click(1, false);
        else if (button == PadButton.Y) click(0, true);
        else if (button == PadButton.DPAD_UP || button == PadButton.DPAD_DOWN
                || button == PadButton.DPAD_LEFT || button == PadButton.DPAD_RIGHT) move(button);
    }

    private void move(PadButton direction) {
        focusedSlot = SlotNavigator.neighbor(inventorySlots.inventorySlots, focusedSlot, direction);
    }

    private void click(boolean quickMove) {
        click(0, quickMove);
    }

    private void click(int button, boolean quickMove) {
        if (focusedSlot < 0 || focusedSlot >= inventorySlots.inventorySlots.size()) return;
        Slot slot = (Slot) inventorySlots.inventorySlots.get(focusedSlot);
        mc.playerController.windowClick(inventorySlots.windowId, slot.slotNumber, button, quickMove, mc.thePlayer);
    }

    private void close() {
        mc.thePlayer.closeScreen();
    }
}
