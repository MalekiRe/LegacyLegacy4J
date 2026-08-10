package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DeadZoneTest {
    @Test
    void removesNoiseAndRescalesRemainingRange() {
        assertEquals(0.0F, DeadZone.apply(0.19F, 0.2F));
        assertEquals(0.5F, DeadZone.apply(0.6F, 0.2F), 0.0001F);
        assertEquals(-1.0F, DeadZone.apply(-1.0F, 0.2F));
    }

    @Test
    void radialDeadZonePreservesDirection() {
        float[] result = DeadZone.radial(0.6F, 0.8F, 0.2F);
        assertEquals(0.6F, result[0], 0.0001F);
        assertEquals(0.8F, result[1], 0.0001F);
    }
}
