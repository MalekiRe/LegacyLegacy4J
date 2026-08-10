package wily.legacy125.client.screen;

import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

/**
 * Retains every behavior of a container-owned slot while preventing vanilla
 * GuiContainer from painting its fixed 16px empty-slot icon. Legacy4J supplies
 * that icon itself at the holder's variable selectable size.
 */
final class LegacyNoBackgroundSlot125 extends Slot {
    private final Slot delegate;

    LegacyNoBackgroundSlot125(Slot delegate) {
        // Every inventory operation below delegates, so this index is never
        // observed. It only satisfies the 1.2.5 Slot constructor.
        super(delegate.inventory, 0, delegate.xDisplayPosition, delegate.yDisplayPosition);
        this.delegate = delegate;
        slotNumber = delegate.slotNumber;
    }

    @Override
    public void func_48433_a(ItemStack current, ItemStack original) {
        delegate.func_48433_a(current, original);
    }

    @Override
    public void onPickupFromSlot(ItemStack stack) {
        delegate.onPickupFromSlot(stack);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return delegate.isItemValid(stack);
    }

    @Override
    public ItemStack getStack() {
        return delegate.getStack();
    }

    @Override
    public boolean getHasStack() {
        return delegate.getHasStack();
    }

    @Override
    public void putStack(ItemStack stack) {
        delegate.putStack(stack);
    }

    @Override
    public void onSlotChanged() {
        delegate.onSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return delegate.getSlotStackLimit();
    }

    @Override
    public int getBackgroundIconIndex() {
        return -1;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        return delegate.decrStackSize(amount);
    }
}
