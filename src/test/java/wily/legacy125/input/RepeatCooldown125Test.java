package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RepeatCooldown125Test {
    @Test
    void heldActionRepeatsAtRealTimeInterval() {
        RepeatCooldown125 cooldown = new RepeatCooldown125(200L);

        assertTrue(cooldown.shouldFire(true, 1_000L));
        assertFalse(cooldown.shouldFire(true, 1_199L));
        assertTrue(cooldown.shouldFire(true, 1_200L));
    }

    @Test
    void rapidReleaseAndRepressCannotBypassCooldown() {
        RepeatCooldown125 cooldown = new RepeatCooldown125(200L);

        assertTrue(cooldown.shouldFire(true, 5_000L));
        assertFalse(cooldown.shouldFire(false, 5_050L));
        assertFalse(cooldown.shouldFire(true, 5_100L));
        assertTrue(cooldown.shouldFire(true, 5_200L));
    }
}
