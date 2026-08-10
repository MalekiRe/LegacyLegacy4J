package wily.legacy125.input;

import java.util.EnumSet;
import java.util.Collections;
import java.util.Set;

public final class ControllerFrame {
    public static final ControllerFrame DISCONNECTED = new ControllerFrame(false, "", 0, 0, 0, 0,
            EnumSet.noneOf(PadButton.class));

    public final boolean connected;
    public final String name;
    public final float leftX;
    public final float leftY;
    public final float rightX;
    public final float rightY;
    private final EnumSet<PadButton> buttons;

    public ControllerFrame(boolean connected, String name, float leftX, float leftY, float rightX, float rightY,
                           Set<PadButton> buttons) {
        this.connected = connected;
        this.name = name;
        this.leftX = leftX;
        this.leftY = leftY;
        this.rightX = rightX;
        this.rightY = rightY;
        this.buttons = buttons.isEmpty() ? EnumSet.noneOf(PadButton.class) : EnumSet.copyOf(buttons);
    }

    public boolean down(PadButton button) {
        return buttons.contains(button);
    }

    public Set<PadButton> buttons() {
        return Collections.unmodifiableSet(buttons);
    }

    public boolean pressed(PadButton button, ControllerFrame previous) {
        return down(button) && !previous.down(button);
    }

    public boolean released(PadButton button, ControllerFrame previous) {
        return !down(button) && previous.down(button);
    }
}
