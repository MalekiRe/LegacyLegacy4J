package wily.legacy125.input;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import wily.legacy125.LegacyConfig;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * LWJGL2/JInput backend. Steam Input's Gamepad template appears here as its virtual gamepad,
 * which avoids depending on Steamworks or a particular Steam installation.
 */
public final class LwjglControllerInput implements ControllerInput {
    private final LegacyConfig config;
    private Controller selected;
    private boolean initialized;
    private int rescanTicks;
    private int lx = -1;
    private int ly = -1;
    private int rx = -1;
    private int ry = -1;
    private int triggers = -1;
    private int leftTrigger = -1;
    private int rightTrigger = -1;
    private boolean separateTriggersRestNegative;
    private int seenConfigRevision = -1;
    private EnumSet<PadButton> lastLoggedButtons = EnumSet.noneOf(PadButton.class);
    private String lastLoggedController = "";
    private String lastInitializationError = "";

    public LwjglControllerInput(LegacyConfig config) {
        this.config = config;
    }

    public ControllerFrame poll() {
        if (!config.enabled) {
            return ControllerFrame.DISCONNECTED;
        }
        initialize();
        if (!initialized) {
            return ControllerFrame.DISCONNECTED;
        }
        if (seenConfigRevision != config.revision()) {
            seenConfigRevision = config.revision();
            selected = null;
        }
        if (selected == null || ++rescanTicks >= 100) {
            rescanTicks = 0;
            selectController();
        }
        if (selected == null) return ControllerFrame.DISCONNECTED;
        try {
            selected.poll();
        } catch (RuntimeException disconnected) {
            ControllerDebugLog125.log("poll failed for '" + selected.getName() + "': " + disconnected);
            selected = null;
            return ControllerFrame.DISCONNECTED;
        }

        float[] left = DeadZone.radial(axis(lx), axis(ly), config.moveDeadZone);
        // Legacy4J smooths each camera axis independently. A radial dead zone makes
        // vertical stick motion change the effective horizontal turn rate.
        float rightX = DeadZone.apply(axis(rx), config.lookDeadZone);
        float rightY = DeadZone.apply(axis(ry), config.lookDeadZone);
        EnumSet<PadButton> down = EnumSet.noneOf(PadButton.class);
        addButton(down, PadButton.A, config.buttonA);
        addButton(down, PadButton.B, config.buttonB);
        addButton(down, PadButton.X, config.buttonX);
        addButton(down, PadButton.Y, config.buttonY);
        addButton(down, PadButton.LEFT_BUMPER, config.buttonLeftBumper);
        addButton(down, PadButton.RIGHT_BUMPER, config.buttonRightBumper);
        addButton(down, PadButton.BACK, config.buttonBack);
        addButton(down, PadButton.START, config.buttonStart);
        addButton(down, PadButton.LEFT_STICK, config.buttonLeftStick);
        addButton(down, PadButton.RIGHT_STICK, config.buttonRightStick);
        addButton(down, PadButton.LEFT_TRIGGER, config.buttonLeftTrigger);
        addButton(down, PadButton.RIGHT_TRIGGER, config.buttonRightTrigger);
        addButton(down, PadButton.DPAD_UP, config.buttonDpadUp);
        addButton(down, PadButton.DPAD_DOWN, config.buttonDpadDown);
        addButton(down, PadButton.DPAD_LEFT, config.buttonDpadLeft);
        addButton(down, PadButton.DPAD_RIGHT, config.buttonDpadRight);

        float povX = selected.getPovX();
        float povY = selected.getPovY();
        if (povX < -0.5F) down.add(PadButton.DPAD_LEFT);
        if (povX > 0.5F) down.add(PadButton.DPAD_RIGHT);
        if (povY < -0.5F) down.add(PadButton.DPAD_UP);
        if (povY > 0.5F) down.add(PadButton.DPAD_DOWN);

        float triggerValue = axis(triggers);
        if (leftTrigger >= 0) {
            if (separateTriggerDown(leftTrigger)) down.add(PadButton.LEFT_TRIGGER);
        } else {
            if (config.combinedTriggerLeftPositive ? triggerValue > 0.45F : triggerValue < -0.45F) {
                down.add(PadButton.LEFT_TRIGGER);
            }
        }
        if (rightTrigger >= 0) {
            if (separateTriggerDown(rightTrigger)) down.add(PadButton.RIGHT_TRIGGER);
        } else {
            if (config.combinedTriggerLeftPositive ? triggerValue < -0.45F : triggerValue > 0.45F) {
                down.add(PadButton.RIGHT_TRIGGER);
            }
        }
        if (!down.equals(lastLoggedButtons)) {
            ControllerDebugLog125.log("buttons controller='" + selected.getName() + "' down=" + down);
            lastLoggedButtons = down.clone();
        }
        return new ControllerFrame(true, selected.getName(), left[0], left[1], rightX, rightY, down);
    }

    public List<ControllerDescriptor> descriptors() {
        initialize();
        if (!initialized) return Collections.emptyList();
        List<ControllerDescriptor> result = new ArrayList<ControllerDescriptor>();
        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            Controller controller = Controllers.getController(i);
            List<String> axes = new ArrayList<String>();
            List<String> buttons = new ArrayList<String>();
            for (int axis = 0; axis < controller.getAxisCount(); axis++) {
                axes.add(axis + ": " + controller.getAxisName(axis));
            }
            for (int button = 0; button < controller.getButtonCount(); button++) {
                buttons.add(button + ": " + controller.getButtonName(button));
            }
            result.add(new ControllerDescriptor(i, controller.getName(), axes, buttons));
        }
        return result;
    }

    public static void refreshControllers() {
        try {
            if (Controllers.isCreated()) Controllers.destroy();
            Controllers.create();
        } catch (Exception ignored) {
        }
    }

    private void initialize() {
        if (initialized) return;
        try {
            if (!Controllers.isCreated()) Controllers.create();
            initialized = true;
            ControllerDebugLog125.log("LWJGL controllers initialized; count=" + Controllers.getControllerCount());
            logDescriptors();
            selectController();
        } catch (Exception failed) {
            String error = failed.toString();
            if (!error.equals(lastInitializationError)) {
                lastInitializationError = error;
                ControllerDebugLog125.log("LWJGL controller initialization failed: " + error);
            }
        }
    }

    private void selectController() {
        selected = null;
        int count = Controllers.getControllerCount();
        if (config.controllerIndex >= 0 && config.controllerIndex < count) {
            selected = Controllers.getController(config.controllerIndex);
        } else {
            int firstUsable = -1;
            for (int i = 0; i < count; i++) {
                Controller candidate = Controllers.getController(i);
                if (candidate.getAxisCount() >= 2 && candidate.getButtonCount() >= 2) {
                    if (firstUsable < 0) firstUsable = i;
                    String name = candidate.getName().toLowerCase(Locale.ENGLISH);
                    if (name.contains("steam") || name.contains("xbox") || name.contains("x-box")
                            || name.contains("gamepad") || name.contains("controller")) {
                        selected = candidate;
                        break;
                    }
                }
            }
            if (selected == null && firstUsable >= 0) selected = Controllers.getController(firstUsable);
        }
        detectAxes();
        String selectedName = selected == null ? "<none>" : selected.getName();
        if (!selectedName.equals(lastLoggedController)) {
            lastLoggedController = selectedName;
            ControllerDebugLog125.log("selected controller=" + selectedName + " configuredIndex="
                    + config.controllerIndex + " axes=[" + lx + "," + ly + "," + rx + "," + ry
                    + "] triggers=[" + triggers + "," + leftTrigger + "," + rightTrigger + "]");
        }
    }

    private void logDescriptors() {
        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            Controller controller = Controllers.getController(i);
            StringBuilder buttons = new StringBuilder();
            StringBuilder axes = new StringBuilder();
            for (int axis = 0; axis < controller.getAxisCount(); axis++) {
                if (axis > 0) axes.append(", ");
                axes.append(axis).append('=').append(controller.getAxisName(axis));
            }
            for (int button = 0; button < controller.getButtonCount(); button++) {
                if (button > 0) buttons.append(", ");
                buttons.append(button).append('=').append(controller.getButtonName(button));
            }
            ControllerDebugLog125.log("device[" + i + "] name='" + controller.getName()
                    + "' axes=" + controller.getAxisCount() + " buttons=" + controller.getButtonCount()
                    + " axisNames=[" + axes + "] buttonNames=[" + buttons + "]");
        }
    }

    private void detectAxes() {
        if (selected == null) return;
        lx = configuredOrFind(config.leftXAxis, "x axis", "left x", "x");
        ly = configuredOrFind(config.leftYAxis, "y axis", "left y", "y");
        rx = configuredOrFind(config.rightXAxis, "x rotation", "right x", "rx axis", "rx");
        ry = configuredOrFind(config.rightYAxis, "y rotation", "right y", "ry axis", "ry");
        leftTrigger = configuredOrFind(config.leftTriggerAxis, "left trigger", "lt axis", "left z");
        rightTrigger = configuredOrFind(config.rightTriggerAxis, "right trigger", "rt axis", "right z");
        triggers = configuredOrFind(config.combinedTriggerAxis, "z axis", "combined trigger", "z");
        separateTriggersRestNegative = false;

        // Linux exposes Steam Input's virtual Xbox triggers as two independent axes:
        // Z Axis and Z Rotation, both resting at -1. Treating Z as a combined axis makes
        // RT appear permanently held. Preserve explicit config, otherwise recognize this
        // standard virtual-XInput layout by device and exact axis names.
        String controllerName = selected.getName().toLowerCase(Locale.ENGLISH);
        boolean virtualXbox = controllerName.contains("xbox") || controllerName.contains("x-box");
        if (virtualXbox && config.leftTriggerAxis < 0 && config.rightTriggerAxis < 0) {
            int z = findExactAxis("z", "z axis");
            int rz = findExactAxis("rz", "z rotation");
            if (z >= 0 && rz >= 0) {
                leftTrigger = z;
                rightTrigger = rz;
                triggers = -1;
                separateTriggersRestNegative = true;
            }
        }

        // JInput names are not standardized. Conventional index fallbacks cover virtual XInput pads.
        if (lx < 0 && selected.getAxisCount() > 0) lx = 0;
        if (ly < 0 && selected.getAxisCount() > 1) ly = 1;
        if (rx < 0 && selected.getAxisCount() > 2) rx = 2;
        if (ry < 0 && selected.getAxisCount() > 3) ry = 3;
        if (triggers < 0 && selected.getAxisCount() > 4) triggers = 4;
    }

    private int configuredOrFind(int configured, String... names) {
        if (configured >= 0 && configured < selected.getAxisCount()) return configured;
        for (String desired : names) {
            for (int i = 0; i < selected.getAxisCount(); i++) {
                String actual = selected.getAxisName(i).toLowerCase(Locale.ENGLISH).trim();
                if (actual.equals(desired) || actual.contains(desired)) return i;
            }
        }
        return -1;
    }

    private int findExactAxis(String... names) {
        for (String name : names) {
            for (int i = 0; i < selected.getAxisCount(); i++) {
                if (selected.getAxisName(i).trim().equalsIgnoreCase(name)) return i;
            }
        }
        return -1;
    }

    private boolean separateTriggerDown(int index) {
        float value = axis(index);
        return separateTriggersRestNegative ? value > -0.45F : value > 0.45F;
    }

    private float axis(int index) {
        return index >= 0 && index < selected.getAxisCount() ? selected.getAxisValue(index) : 0.0F;
    }

    private void addButton(EnumSet<PadButton> down, PadButton button, int index) {
        if (index >= 0 && index < selected.getButtonCount() && selected.isButtonPressed(index)) {
            down.add(button);
        }
    }
}
