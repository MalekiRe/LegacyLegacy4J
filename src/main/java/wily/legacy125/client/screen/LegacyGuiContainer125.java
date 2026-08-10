package wily.legacy125.client.screen;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.Slot;
import org.lwjgl.input.Keyboard;

/**
 * Shared 1.2.5 adapter for Legacy4J's variable-size slot widgets.
 *
 * Vanilla GuiContainer hard-codes both hit testing and its hover rectangle to
 * 18/16 pixels. Legacy4J SD holders are commonly 13 or 11 pixels, so retaining
 * those constants makes adjacent slots overlap and draws focus up-left and too
 * large. This class routes every custom container through the display size.
 */
public abstract class LegacyGuiContainer125 extends GuiContainer implements LegacySlotSizeProvider {
    private static final int VANILLA_HOVER = 0x80FFFFFF;
    private int legacyMouseX;
    private int legacyMouseY;
    private boolean legacyVisualMouseOverride;

    protected LegacyGuiContainer125(Container container) {
        super(container);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        if (!legacyVisualMouseOverride) {
            legacyMouseX = mouseX;
            legacyMouseY = mouseY;
        } else {
            mouseX = legacyMouseX;
            mouseY = legacyMouseY;
        }
        boolean carrying = mc != null && mc.thePlayer != null
                && mc.thePlayer.inventory.getItemStack() != null;
        int carriedOffset = LegacyCarriedStackPosition125.offset(mouseX, mouseY, carrying);
        super.drawScreen(mouseX + carriedOffset, mouseY + carriedOffset, partialTick);
    }

    @Override
    protected void drawGradientRect(int left, int top, int right, int bottom,
                                    int topColor, int bottomColor) {
        // GuiContainer's hard-coded 16x16 translucent hover is replaced in
        // drawLegacySlotFocus with the upstream LegacySlotWidget geometry.
        if (right - left == 16 && bottom - top == 16
                && topColor == VANILLA_HOVER && bottomColor == VANILLA_HOVER) return;
        super.drawGradientRect(left, top, right, bottom, topColor, bottomColor);
    }

    protected final void drawLegacySlotFocus(Slot controllerSlot, boolean controllerVisible) {
        Slot focused = controllerVisible ? controllerSlot : legacySlotAt(legacyMouseX, legacyMouseY);
        if (!legacyVisible(focused)) return;
        LegacyTheme.focus(mc, focused.xDisplayPosition, focused.yDisplayPosition,
                legacySlotSize(focused.xDisplayPosition, focused.yDisplayPosition));
    }

    protected final Slot legacySlotAt(int mouseX, int mouseY) {
        double localX = mouseX - guiLeft;
        double localY = mouseY - guiTop;
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (!legacyVisible(slot)) continue;
            int size = legacySlotSize(slot.xDisplayPosition, slot.yDisplayPosition);
            if (LegacySlotGeometry125.contains(slot.xDisplayPosition, slot.yDisplayPosition,
                    size, localX, localY)) return slot;
        }
        return null;
    }

    /**
     * Deterministic visual-test target using the same corner and extent as
     * upstream LegacyIconHolder#isHovered. This deliberately returns a GUI
     * coordinate rather than a display pixel; the capture driver performs the
     * active GUI-scale conversion in exactly the same way as the controller.
     */
    public final int[] legacyVisualFirstSlotCenter() {
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (!legacyVisible(slot)) continue;
            int size = legacySlotSize(slot.xDisplayPosition, slot.yDisplayPosition);
            float inset = LegacySlotGeometry125.cornerInset(size);
            return new int[]{
                    Math.round(guiLeft + slot.xDisplayPosition - inset + size / 2.0F),
                    Math.round(guiTop + slot.yDisplayPosition - inset + size / 2.0F)
            };
        }
        return null;
    }

    public final void legacyVisualHoverFirstSlot() {
        int[] point = legacyVisualFirstSlotCenter();
        if (point == null) return;
        legacyMouseX = point[0];
        legacyMouseY = point[1];
        legacyVisualMouseOverride = true;
    }

    public final void legacyVisualPointerTopLeft() {
        legacyMouseX = 0;
        legacyMouseY = 0;
        legacyVisualMouseOverride = true;
    }

    private boolean legacyVisible(Slot slot) {
        return slot != null && slot.xDisplayPosition > -100 && slot.yDisplayPosition > -100;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 && button != 1) return;
        Slot slot = legacySlotAt(mouseX, mouseY);
        boolean outside = mouseX < guiLeft || mouseY < guiTop
                || mouseX >= guiLeft + xSize || mouseY >= guiTop + ySize;
        int slotNumber = slot == null ? -1 : slot.slotNumber;
        if (outside) slotNumber = -999;
        if (slotNumber == -1) return;
        boolean quickMove = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        handleMouseClick(slot, slotNumber, button, quickMove);
    }
}
