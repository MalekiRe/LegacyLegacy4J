package wily.legacy125.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacySlotGeometry125Test {
    @Test
    void sdHolderUsesUpstreamSelectableArea() {
        assertEquals(0.65F, LegacySlotGeometry125.cornerInset(13), 0.0001F);
        assertEquals(11.7F, LegacySlotGeometry125.selectableSize(13), 0.0001F);
        assertEquals(9.9F, LegacySlotGeometry125.selectableSize(11), 0.0001F);
    }

    @Test
    void adjacentThirteenPixelSlotsHaveDistinctHitRegions() {
        assertTrue(LegacySlotGeometry125.contains(7, 76, 13, 18.0D, 82.0D));
        assertFalse(LegacySlotGeometry125.contains(20, 76, 13, 18.0D, 82.0D));
        assertTrue(LegacySlotGeometry125.contains(7, 76, 13, 19.0D, 82.0D));
        assertFalse(LegacySlotGeometry125.contains(20, 76, 13, 19.0D, 82.0D));
        assertFalse(LegacySlotGeometry125.contains(7, 76, 13, 20.0D, 82.0D));
        assertTrue(LegacySlotGeometry125.contains(20, 76, 13, 20.0D, 82.0D));
    }
}
