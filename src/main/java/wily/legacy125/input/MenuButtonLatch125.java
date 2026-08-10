package wily.legacy125.input;

/**
 * Prevents the menu-opening buttons from leaking across a GUI-to-game transition.
 * Steam Input can expose the same physical press through both its desktop/UI action
 * and virtual gamepad action; requiring a release mirrors Legacy4J's binding edge.
 */
public final class MenuButtonLatch125 {
    private boolean armed;

    public void onGuiTick() {
        armed = true;
    }

    public boolean suppressMenuOpen(ControllerFrame frame) {
        if (!armed) return false;
        if (!frame.down(PadButton.BACK) && !frame.down(PadButton.START)) {
            armed = false;
        }
        return true;
    }
}
