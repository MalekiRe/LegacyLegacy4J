package wily.legacy125.input;

/** Legacy4J-compatible right-stick response and polling-time scaling. */
public final class LegacyCameraTurn125 {
    private static final int MAX_UPDATE_MILLIS = 32;

    private LegacyCameraTurn125() {
    }

    public static float curve(float value) {
        return value * value * Math.signum(value);
    }

    public static int elapsedMillis(long nowMillis, long previousMillis) {
        if (previousMillis == 0L || nowMillis <= previousMillis) return 1;
        return Math.max(1, Math.min(MAX_UPDATE_MILLIS, (int) (nowMillis - previousMillis)));
    }

    /** Exact modern Legacy4J sensitivity transform, before Minecraft's 0.15 turn factor. */
    public static float turnAmount(float axis, float sensitivity, float elapsedMillis) {
        float sensitivityFactor = sensitivity * 0.6F + 0.2F;
        return curve(axis) * sensitivityFactor * sensitivityFactor * sensitivityFactor
                * 7.5F * elapsedMillis;
    }

    /** Migrates the old backport's degrees-per-16-ms scale to Legacy4J's 0..1 scale. */
    public static float migrateDegreesSensitivity(float degrees) {
        double sensitivityFactor = Math.cbrt(Math.max(0.0F, degrees) / 18.0F);
        return (float) Math.max(0.0D, Math.min(1.0D, (sensitivityFactor - 0.2D) / 0.6D));
    }
}
