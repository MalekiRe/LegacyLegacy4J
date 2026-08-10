package wily.legacy125.client.screen;

import net.minecraft.src.Container;
import net.minecraft.src.RenderHelper;
import net.minecraft.src.RenderManager;
import net.minecraft.src.Slot;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import wily.legacy125.input.PadButton;

/** Compact console inventory. Crafting remains on the dedicated recipe screen. */
public final class LegacyInventoryScreen extends LegacyGuiContainer125 implements LegacyControllerScreen {
    private final int[] oldX;
    private final int[] oldY;
    private final Slot[] originalSlots;
    private int focusedSlot;
    private boolean controllerFocusVisible;

    public LegacyInventoryScreen(Container container) {
        super(container);
        xSize = 130;
        ySize = 140;
        oldX = new int[container.inventorySlots.size()];
        oldY = new int[container.inventorySlots.size()];
        originalSlots = new Slot[container.inventorySlots.size()];
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = (Slot) container.inventorySlots.get(i);
            originalSlots[i] = slot;
            oldX[i] = slot.xDisplayPosition;
            oldY[i] = slot.yDisplayPosition;
            if (i >= 5 && i <= 8 && slot.getBackgroundIconIndex() >= 0) {
                container.inventorySlots.set(i, new LegacyNoBackgroundSlot125(slot));
            }
        }
        arrange();
    }

    private void arrange() {
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
            if (i <= 4) {
                slot.xDisplayPosition = -1000;
                slot.yDisplayPosition = -1000;
            } else if (i <= 8) {
                // Upstream EQUIP_SLOT_OFFSET_SD when classic crafting is off.
                slot.xDisplayPosition = 38;
                slot.yDisplayPosition = 9 + (i - 5) * 13;
            } else if (i <= 35) {
                int inventory = i - 9;
                slot.xDisplayPosition = 7 + inventory % 9 * 13;
                slot.yDisplayPosition = 76 + inventory / 9 * 13;
            } else {
                slot.xDisplayPosition = 7 + (i - 36) * 13;
                slot.yDisplayPosition = 120;
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        focusedSlot = inventorySlots.inventorySlots.size() > 9 ? 9 : SlotNavigator.firstVisible(inventorySlots.inventorySlots);
    }

    @Override
    public void drawDefaultBackground() {
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
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.xDisplayPosition > -100 && slot.xDisplayPosition == x && slot.yDisplayPosition == y) return 13;
        }
        return 16;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        LegacyTheme.panel(mc, guiLeft, guiTop, guiLeft + xSize, guiTop + ySize);
        LegacyTheme.entityPanel(mc, guiLeft + 54, guiTop + 8, guiLeft + 93, guiTop + 60);
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.xDisplayPosition <= -100) continue;
            int index = inventorySlots.inventorySlots.indexOf(slot);
            if (index == 5) LegacyTheme.armorSlot(mc, "/legacy4j125/gui/head_slot.png",
                    guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 13);
            else if (index == 6) LegacyTheme.armorSlot(mc, "/legacy4j125/gui/chest_slot.png",
                    guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 13);
            else if (index == 7) LegacyTheme.armorSlot(mc, "/legacy4j125/gui/legs_slot.png",
                    guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 13);
            else if (index == 8) LegacyTheme.armorSlot(mc, "/legacy4j125/gui/feet_slot.png",
                    guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 13);
            else LegacyTheme.slot(mc, guiLeft + slot.xDisplayPosition,
                    guiTop + slot.yDisplayPosition, 13);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        LegacyTheme.drawString(mc, "Inventory", 7, 64, LegacyTheme.TEXT);
        Slot controllerSlot = focusedSlot >= 0 && focusedSlot < inventorySlots.inventorySlots.size()
                ? (Slot) inventorySlots.inventorySlots.get(focusedSlot) : null;
        drawLegacySlotFocus(controllerSlot, controllerFocusVisible);
        drawPlayerPreview(74, 57, 20);
    }

    @Override
    public void onGuiClosed() {
        for (int i = 0; i < inventorySlots.inventorySlots.size() && i < oldX.length; i++) {
            Slot slot = originalSlots[i];
            slot.xDisplayPosition = oldX[i];
            slot.yDisplayPosition = oldY[i];
            inventorySlots.inventorySlots.set(i, slot);
        }
        super.onGuiClosed();
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

    private void drawPlayerPreview(int x, int y, int scale) {
        RenderManager manager = RenderManager.instance;
        // Screens may be opened on the first world tick by tests or another
        // mod, before EntityRenderer has populated RenderManager's context.
        if (manager.renderEngine == null || manager.livingPlayer == null) {
            manager.cacheActiveRenderInfo(mc.theWorld, mc.renderEngine, fontRenderer,
                    mc.thePlayer, mc.gameSettings, 1.0F);
        }
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 50.0F);
        GL11.glScalef(-scale, scale, scale);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float renderYaw = mc.thePlayer.renderYawOffset;
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;
        float headYaw = mc.thePlayer.rotationYawHead;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        mc.thePlayer.renderYawOffset = 0.0F;
        mc.thePlayer.rotationYaw = 0.0F;
        mc.thePlayer.rotationPitch = 8.0F;
        mc.thePlayer.rotationYawHead = 0.0F;
        GL11.glTranslatef(0.0F, mc.thePlayer.yOffset, 0.0F);
        manager.playerViewY = 180.0F;
        try {
            manager.renderEntityWithPosYaw(mc.thePlayer, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        } finally {
            mc.thePlayer.renderYawOffset = renderYaw;
            mc.thePlayer.rotationYaw = yaw;
            mc.thePlayer.rotationPitch = pitch;
            mc.thePlayer.rotationYawHead = headYaw;
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
    }
}
