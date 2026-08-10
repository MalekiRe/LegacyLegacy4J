package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MenuOpeningGate125Test {
    @Test
    void syntheticEscapeIsSuppressedWhileControllerIsConnected() {
        MenuOpeningGate125 gate = new MenuOpeningGate125();

        assertFalse(gate.allowVanillaPauseScreen(true));
    }

    @Test
    void realControllerStartAllowsExactlyOneVanillaPauseScreen() {
        MenuOpeningGate125 gate = new MenuOpeningGate125();
        gate.onControllerRequest();

        assertTrue(gate.allowVanillaPauseScreen(true));
        assertFalse(gate.allowVanillaPauseScreen(true));
    }

    @Test
    void keyboardEscapeStillWorksWithoutAController() {
        MenuOpeningGate125 gate = new MenuOpeningGate125();

        assertTrue(gate.allowVanillaPauseScreen(false));
    }
}
