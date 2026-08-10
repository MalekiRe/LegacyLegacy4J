package wily.legacy125.input;

/** Radial dead-zone processing shared by movement, camera and menu navigation. */
public final class DeadZone {
    private DeadZone() {
    }

    public static float apply(float value, float deadZone) {
        deadZone = clampDeadZone(deadZone);
        float magnitude = Math.abs(value);
        if (magnitude <= deadZone) {
            return 0.0F;
        }
        float scaled = (magnitude - deadZone) / (1.0F - deadZone);
        return Math.copySign(Math.min(scaled, 1.0F), value);
    }

    public static float[] radial(float x, float y, float deadZone) {
        deadZone = clampDeadZone(deadZone);
        float magnitude = (float) Math.sqrt(x * x + y * y);
        if (magnitude <= deadZone) {
            return new float[] {0.0F, 0.0F};
        }
        float scaledMagnitude = Math.min((magnitude - deadZone) / (1.0F - deadZone), 1.0F);
        float scale = scaledMagnitude / magnitude;
        return new float[] {x * scale, y * scale};
    }

    private static float clampDeadZone(float deadZone) {
        return Math.max(0.0F, Math.min(0.95F, deadZone));
    }
}
