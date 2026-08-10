package wily.legacy125.input;

import net.minecraft.src.MovementInput;

/**
 * Composes Legacy4J analog movement with Minecraft's live keyboard input.
 * Keeping the vanilla delegate authoritative means attaching a controller can
 * never cancel a key that is physically held.
 */
public final class LegacyMovementInput125 extends MovementInput {
    private final MovementInput keyboard;
    private ControllerFrame controller = ControllerFrame.DISCONNECTED;
    private boolean flying;

    public LegacyMovementInput125(MovementInput keyboard) {
        if (keyboard == null) throw new IllegalArgumentException("keyboard");
        this.keyboard = keyboard;
    }

    public void updateController(ControllerFrame frame, boolean isFlying) {
        controller = frame == null ? ControllerFrame.DISCONNECTED : frame;
        flying = isFlying;
    }

    @Override
    public void func_52013_a() {
        keyboard.func_52013_a();
        moveStrafe = keyboard.moveStrafe;
        moveForward = keyboard.moveForward;
        jump = keyboard.jump;
        sneak = keyboard.sneak;
        if (!controller.connected) return;

        moveStrafe = clamp(moveStrafe - controller.leftX);
        moveForward = clamp(moveForward - controller.leftY);
        if (flying && controller.down(PadButton.DPAD_LEFT)) moveStrafe = clamp(moveStrafe + 1.0F);
        if (flying && controller.down(PadButton.DPAD_RIGHT)) moveStrafe = clamp(moveStrafe - 1.0F);
        jump |= controller.down(PadButton.A) || flying && controller.down(PadButton.DPAD_UP);
        boolean controllerSneak = controller.down(PadButton.RIGHT_STICK)
                || flying && controller.down(PadButton.DPAD_DOWN);
        if (controllerSneak && !sneak) {
            moveStrafe *= 0.3F;
            moveForward *= 0.3F;
        }
        sneak |= controllerSneak;
    }

    private static float clamp(float value) {
        return Math.max(-1.0F, Math.min(1.0F, value));
    }
}
