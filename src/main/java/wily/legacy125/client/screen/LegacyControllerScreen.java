package wily.legacy125.client.screen;

import wily.legacy125.input.PadButton;

/** A screen that consumes semantic controller buttons instead of emulated mouse clicks. */
public interface LegacyControllerScreen {
    void onControllerButton(PadButton button);
}
