package wily.legacy125.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LegacyCarriedStackPosition125Test {
    @Test
    void movesControllerCarriedStackOneItemDownAndRightAtTopLeft() {
        assertEquals(16, LegacyCarriedStackPosition125.offset(0, 0, true));
        // Vanilla centers the 16px item around the pointer, so (16 - 8)
        // leaves the complete icon and its count inside the display.
        assertEquals(8, 0 - 8 + LegacyCarriedStackPosition125.offset(0, 0, true));
    }

    @Test
    void leavesNormalMouseAndEmptyPointerPositionsUntouched() {
        assertEquals(0, LegacyCarriedStackPosition125.offset(32, 32, true));
        assertEquals(0, LegacyCarriedStackPosition125.offset(0, 0, false));
    }
}
