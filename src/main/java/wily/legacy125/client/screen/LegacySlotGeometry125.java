package wily.legacy125.client.screen;

/** Exact geometry used by upstream LegacyIconHolder and LegacySlotDisplay. */
final class LegacySlotGeometry125 {
    private LegacySlotGeometry125() {
    }

    static float cornerInset(int size) {
        return size / 20.0F;
    }

    static float selectableSize(int size) {
        return size - cornerInset(size) * 2.0F;
    }

    static boolean contains(int slotX, int slotY, int size, double localX, double localY) {
        double inset = cornerInset(size);
        return localX >= slotX - inset && localX < slotX - inset + size
                && localY >= slotY - inset && localY < slotY - inset + size;
    }
}
