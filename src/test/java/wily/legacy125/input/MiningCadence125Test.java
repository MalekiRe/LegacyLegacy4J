package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MiningCadence125Test {
    @Test
    void survivalDamageRunsAtTwentyHertz() {
        MiningCadence125 cadence = new MiningCadence125();

        assertTrue(cadence.shouldDamage(true, false, 1_000L));
        assertFalse(cadence.shouldDamage(true, false, 1_049L));
        assertTrue(cadence.shouldDamage(true, false, 1_050L));
        assertFalse(cadence.shouldDamage(false, false, 1_100L));
    }

    @Test
    void instantMineBypassesSurvivalCadence() {
        MiningCadence125 cadence = new MiningCadence125();

        assertTrue(cadence.shouldDamage(true, true, 2_000L));
        assertTrue(cadence.shouldDamage(true, true, 2_001L));
    }
}
