package wily.legacy125.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class FocusNavigatorTest {
    @Test
    void wrapsAndSkipsDisabledControls() {
        boolean[] focusable = {true, false, true};
        assertEquals(2, FocusNavigator.move(0, -1, focusable));
        assertEquals(2, FocusNavigator.move(0, 1, focusable));
        assertEquals(0, FocusNavigator.move(2, 1, focusable));
    }

    @Test
    void reportsNoFocusWhenEveryControlIsDisabled() {
        assertEquals(-1, FocusNavigator.move(0, 1, new boolean[] {false, false}));
    }
}
