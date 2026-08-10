package wily.legacy125.input;

import java.util.EnumSet;

/** One-to-one gameplay edges from Legacy4J's default ControllerBinding map. */
public final class LegacyGameplayBindings125 {
    public enum Action {
        DROP, CRAFTING, INVENTORY, HOST_OPTIONS, PAUSE, TOGGLE_PERSPECTIVE,
        HOTBAR_LEFT, HOTBAR_RIGHT
    }

    private LegacyGameplayBindings125() {
    }

    public static EnumSet<Action> pressed(ControllerFrame current, ControllerFrame previous) {
        EnumSet<Action> result = EnumSet.noneOf(Action.class);
        add(result, Action.DROP, current.pressed(PadButton.B, previous));
        add(result, Action.CRAFTING, current.pressed(PadButton.X, previous));
        add(result, Action.INVENTORY, current.pressed(PadButton.Y, previous));
        add(result, Action.HOST_OPTIONS, current.pressed(PadButton.BACK, previous));
        add(result, Action.PAUSE, current.pressed(PadButton.START, previous));
        add(result, Action.TOGGLE_PERSPECTIVE, current.pressed(PadButton.LEFT_STICK, previous));
        add(result, Action.HOTBAR_LEFT, current.pressed(PadButton.LEFT_BUMPER, previous));
        add(result, Action.HOTBAR_RIGHT, current.pressed(PadButton.RIGHT_BUMPER, previous));
        return result;
    }

    public static boolean attacking(ControllerFrame frame) {
        return frame.down(PadButton.RIGHT_TRIGGER);
    }

    public static boolean using(ControllerFrame frame) {
        return frame.down(PadButton.LEFT_TRIGGER);
    }

    private static void add(EnumSet<Action> actions, Action action, boolean active) {
        if (active) actions.add(action);
    }
}
