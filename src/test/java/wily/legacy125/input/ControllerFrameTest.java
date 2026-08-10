package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ControllerFrameTest {
    @Test
    void reportsEdgesWithoutRepeatingHeldButtons() {
        ControllerFrame up = ControllerFrame.DISCONNECTED;
        ControllerFrame down = new ControllerFrame(true, "Steam Virtual Gamepad", 0, 0, 0, 0,
                EnumSet.of(PadButton.A));
        assertTrue(down.pressed(PadButton.A, up));
        assertFalse(down.pressed(PadButton.A, down));
        assertTrue(up.released(PadButton.A, down));
    }
}
