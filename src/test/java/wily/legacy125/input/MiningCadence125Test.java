package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MiningCadence125Test {
    @Test
    void survivalMiningRunsAtTwentyHertz() {
        MiningCadence125 cadence = new MiningCadence125();
        assertTrue(cadence.shouldDamage(true, false, 1000L));
        assertFalse(cadence.shouldDamage(true, false, 1049L));
        assertTrue(cadence.shouldDamage(true, false, 1050L));
    }

    @Test
    void creativeMiningCanRunEveryFrame() {
        MiningCadence125 cadence = new MiningCadence125();
        assertTrue(cadence.shouldDamage(true, true, 1000L));
        assertTrue(cadence.shouldDamage(true, true, 1001L));
        assertFalse(cadence.shouldDamage(false, true, 1002L));
    }
}
