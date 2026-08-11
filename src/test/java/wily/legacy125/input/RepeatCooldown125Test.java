package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RepeatCooldown125Test {
    @Test
    void repeatsAtConfiguredIntervalWhileHeld() {
        RepeatCooldown125 cooldown = new RepeatCooldown125(200L);
        assertTrue(cooldown.shouldFire(true, 1000L));
        assertFalse(cooldown.shouldFire(true, 1199L));
        assertTrue(cooldown.shouldFire(true, 1200L));
    }

    @Test
    void releasedStateNeverFires() {
        RepeatCooldown125 cooldown = new RepeatCooldown125(200L);
        assertFalse(cooldown.shouldFire(false, 1000L));
    }
}
