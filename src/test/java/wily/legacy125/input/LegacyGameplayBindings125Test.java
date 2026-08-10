package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyGameplayBindings125Test {
    @Test
    void defaultHotkeysMatchUpstreamLegacy4j() {
        ControllerFrame previous = frame();
        ControllerFrame current = frame(PadButton.B, PadButton.X, PadButton.Y, PadButton.BACK,
                PadButton.START, PadButton.LEFT_STICK, PadButton.LEFT_BUMPER, PadButton.RIGHT_BUMPER);

        assertEquals(EnumSet.allOf(LegacyGameplayBindings125.Action.class),
                LegacyGameplayBindings125.pressed(current, previous));
        assertTrue(LegacyGameplayBindings125.pressed(current, previous)
                .contains(LegacyGameplayBindings125.Action.DROP));
        assertFalse(LegacyGameplayBindings125.pressed(current, current)
                .contains(LegacyGameplayBindings125.Action.DROP));
    }

    @Test
    void triggersRemainHeldActions() {
        ControllerFrame triggers = frame(PadButton.LEFT_TRIGGER, PadButton.RIGHT_TRIGGER);
        assertTrue(LegacyGameplayBindings125.using(triggers));
        assertTrue(LegacyGameplayBindings125.attacking(triggers));
        assertFalse(LegacyGameplayBindings125.using(frame()));
        assertFalse(LegacyGameplayBindings125.attacking(frame()));
    }

    private static ControllerFrame frame(PadButton... buttons) {
        EnumSet<PadButton> down = EnumSet.noneOf(PadButton.class);
        for (PadButton button : buttons) down.add(button);
        return new ControllerFrame(true, "test", 0, 0, 0, 0, down);
    }
}
