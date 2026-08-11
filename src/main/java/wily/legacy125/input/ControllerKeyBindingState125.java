package wily.legacy125.input;

import net.minecraft.src.KeyBinding;

/** Drives a vanilla key binding from a held controller button without losing its edges. */
public final class ControllerKeyBindingState125 {
    private boolean held;

    /** Returns true only when the controller released a binding it previously held. */
    public boolean update(KeyBinding binding, boolean down) {
        return update(binding, down, true);
    }

    /** Allows an action handled immediately to suppress the redundant vanilla click queue. */
    public boolean update(KeyBinding binding, boolean down, boolean enqueuePress) {
        boolean pressed = down && !held;
        boolean released = !down && held;
        if (pressed && enqueuePress) binding.pressTime++;
        if (down || released) binding.pressed = down;
        held = down;
        return released;
    }

    /** Releases only controller-owned state, leaving an untouched binding alone. */
    public boolean release(KeyBinding binding) {
        if (!held) return false;
        held = false;
        binding.pressed = false;
        return true;
    }
}
