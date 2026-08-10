package wily.legacy125.input;

import net.minecraft.src.MovementInput;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyMovementInput125Test {
    @Test
    void composesAnalogMovementWithKeyboardInsteadOfCancellingIt() {
        StubKeyboard keyboard = new StubKeyboard();
        keyboard.forward = 0.4F;
        LegacyMovementInput125 input = new LegacyMovementInput125(keyboard);
        input.updateController(frame(-0.5F, -0.35F), false);

        input.func_52013_a();

        assertEquals(0.5F, input.moveStrafe, 0.0001F);
        assertEquals(0.75F, input.moveForward, 0.0001F);
    }

    @Test
    void usesUpstreamJumpCrouchAndFlightBindings() {
        LegacyMovementInput125 input = new LegacyMovementInput125(new StubKeyboard());
        input.updateController(frame(PadButton.A, PadButton.RIGHT_STICK), false);
        input.func_52013_a();
        assertTrue(input.jump);
        assertTrue(input.sneak);

        input.updateController(frame(PadButton.DPAD_UP, PadButton.DPAD_LEFT), true);
        input.func_52013_a();
        assertTrue(input.jump);
        assertEquals(1.0F, input.moveStrafe, 0.0001F);

        input.updateController(frame(PadButton.DPAD_UP, PadButton.DPAD_LEFT), false);
        input.func_52013_a();
        assertFalse(input.jump);
        assertEquals(0.0F, input.moveStrafe, 0.0001F);
    }

    @Test
    void disconnectedControllerLeavesKeyboardAuthoritative() {
        StubKeyboard keyboard = new StubKeyboard();
        keyboard.strafe = -1.0F;
        keyboard.jumpDown = true;
        LegacyMovementInput125 input = new LegacyMovementInput125(keyboard);
        input.updateController(ControllerFrame.DISCONNECTED, false);
        input.func_52013_a();
        assertEquals(-1.0F, input.moveStrafe, 0.0001F);
        assertTrue(input.jump);
    }

    private static ControllerFrame frame(float x, float y) {
        return new ControllerFrame(true, "test", x, y, 0, 0, EnumSet.noneOf(PadButton.class));
    }

    private static ControllerFrame frame(PadButton... buttons) {
        EnumSet<PadButton> down = EnumSet.noneOf(PadButton.class);
        for (PadButton button : buttons) down.add(button);
        return new ControllerFrame(true, "test", 0, 0, 0, 0, down);
    }

    private static final class StubKeyboard extends MovementInput {
        float strafe;
        float forward;
        boolean jumpDown;

        @Override
        public void func_52013_a() {
            moveStrafe = strafe;
            moveForward = forward;
            jump = jumpDown;
            sneak = false;
        }
    }
}
