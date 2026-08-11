package wily.legacy125.input;

import net.minecraft.src.KeyBinding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ControllerKeyBindingState125Test {
    @Test
    void pressHoldAndReleaseMatchVanillaInputState() {
        ControllerKeyBindingState125 state = new ControllerKeyBindingState125();
        KeyBinding binding = new KeyBinding("test.controller.action", 12345);

        assertFalse(state.update(binding, true));
        assertTrue(binding.pressed);
        assertEquals(1, binding.pressTime);

        assertFalse(state.update(binding, true));
        assertTrue(binding.pressed);
        assertEquals(1, binding.pressTime, "holding must not enqueue repeated clicks");

        assertTrue(state.update(binding, false));
        assertFalse(binding.pressed);
    }

    @Test
    void releasingAnIdleControllerDoesNotOverwriteOtherInput() {
        ControllerKeyBindingState125 state = new ControllerKeyBindingState125();
        KeyBinding binding = new KeyBinding("test.other.input", 12346);
        binding.pressed = true;

        assertFalse(state.release(binding));
        assertTrue(binding.pressed);
    }

    @Test
    void immediateEntityAttackCanSuppressQueuedDuplicateClick() {
        ControllerKeyBindingState125 state = new ControllerKeyBindingState125();
        KeyBinding binding = new KeyBinding("test.direct.attack", 12347);

        assertFalse(state.update(binding, true, false));
        assertTrue(binding.pressed);
        assertEquals(0, binding.pressTime);
        assertTrue(state.update(binding, false, false));
        assertFalse(binding.pressed);
    }
}
