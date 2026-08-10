package wily.legacy125.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyScreenInitialFocusTest {
    @Test
    void selectsTheFirstEnabledButtonAsSoonAsTheScreenOpens() {
        TestScreen screen = new TestScreen();
        screen.width = 640;
        screen.height = 360;

        screen.initGui();

        assertFalse(screen.disabled.controllerFocused);
        assertTrue(screen.firstEnabled.controllerFocused);
        assertFalse(screen.secondEnabled.controllerFocused);
    }

    private static final class TestScreen extends LegacyScreen {
        private LegacyButton disabled;
        private LegacyButton firstEnabled;
        private LegacyButton secondEnabled;

        private TestScreen() {
            super(null, "Test");
        }

        @Override
        protected void addLegacyControls() {
            disabled = addButton(0, 0, 0, 100, "Disabled");
            disabled.enabled = false;
            firstEnabled = addButton(1, 0, 20, 100, "First enabled");
            secondEnabled = addButton(2, 0, 40, 100, "Second enabled");
        }
    }
}
