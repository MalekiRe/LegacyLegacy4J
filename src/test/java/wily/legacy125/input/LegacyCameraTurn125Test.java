package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LegacyCameraTurn125Test {
    @Test
    void usesLegacy4JSignedSquareCameraCurve() {
        assertEquals(0.25F, LegacyCameraTurn125.curve(0.5F), 0.0001F);
        assertEquals(-0.25F, LegacyCameraTurn125.curve(-0.5F), 0.0001F);
        assertEquals(1.0F, LegacyCameraTurn125.curve(1.0F), 0.0001F);
    }

    @Test
    void scalesRotationByElapsedPollingTime() {
        // Default sensitivity produces turn inputs of 15 and 7.5; Minecraft's
        // built-in 0.15 factor converts those to 2.25 and 1.125 degrees.
        assertEquals(15.0F, LegacyCameraTurn125.turnAmount(1.0F, 0.5F, 16.0F), 0.0001F);
        assertEquals(7.5F, LegacyCameraTurn125.turnAmount(1.0F, 0.5F, 8.0F), 0.0001F);
    }

    @Test
    void migratesTheUntunedBackportDefaultToLegacy4JDefault() {
        assertEquals(0.5F, LegacyCameraTurn125.migrateDegreesSensitivity(2.25F), 0.0001F);
    }

    @Test
    void clampsLongFramesLikeModernLegacy4J() {
        assertEquals(1, LegacyCameraTurn125.elapsedMillis(20L, 0L));
        assertEquals(8, LegacyCameraTurn125.elapsedMillis(20L, 12L));
        assertEquals(32, LegacyCameraTurn125.elapsedMillis(100L, 1L));
    }
}
