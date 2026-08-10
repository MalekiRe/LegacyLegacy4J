package wily.legacy125.client.screen;

/** Keeps a controller-carried stack fully visible when Steam parks the cursor at (0, 0). */
final class LegacyCarriedStackPosition125 {
    static final int ITEM_SIZE = 16;
    private static final int VANILLA_CENTER = ITEM_SIZE / 2;

    private LegacyCarriedStackPosition125() {
    }

    static int offset(int mouseX, int mouseY, boolean carrying) {
        return carrying && mouseX < VANILLA_CENTER && mouseY < VANILLA_CENTER ? ITEM_SIZE : 0;
    }
}
