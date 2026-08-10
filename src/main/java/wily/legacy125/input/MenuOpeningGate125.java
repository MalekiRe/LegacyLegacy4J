package wily.legacy125.input;

/**
 * Distinguishes an intentional controller pause from Steam Input's parallel
 * synthetic Escape event. The latter must not reopen the menu after Resume.
 */
public final class MenuOpeningGate125 {
    private boolean controllerRequest;

    public void onControllerRequest() {
        controllerRequest = true;
    }

    public boolean allowVanillaPauseScreen(boolean controllerConnected) {
        if (controllerRequest) {
            controllerRequest = false;
            return true;
        }
        return !controllerConnected;
    }
}
