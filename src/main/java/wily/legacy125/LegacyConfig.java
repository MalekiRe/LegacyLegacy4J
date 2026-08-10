package wily.legacy125;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Small, dependency-free properties config that remains editable on Java 8-era launchers. */
public final class LegacyConfig {
    private static LegacyConfig instance;

    public boolean enabled = true;
    public int controllerIndex = -1;
    public float moveDeadZone = 0.20F;
    public float lookDeadZone = 0.16F;
    public float lookSensitivity = 2.25F;
    public float menuCursorSpeed = 10.0F;
    public boolean invertLookY = false;
    public boolean showTooltips = true;
    public float hudOpacity = 0.45F;
    public int hudMargin = 4;

    public int leftXAxis = -1;
    public int leftYAxis = -1;
    public int rightXAxis = -1;
    public int rightYAxis = -1;
    public int combinedTriggerAxis = -1;
    public int leftTriggerAxis = -1;
    public int rightTriggerAxis = -1;
    public boolean combinedTriggerLeftPositive = true;

    public int buttonA = 0;
    public int buttonB = 1;
    public int buttonX = 2;
    public int buttonY = 3;
    public int buttonLeftBumper = 4;
    public int buttonRightBumper = 5;
    public int buttonBack = 6;
    public int buttonStart = 7;
    public int buttonLeftStick = 8;
    public int buttonRightStick = 9;
    public int buttonLeftTrigger = -1;
    public int buttonRightTrigger = -1;
    public int buttonDpadUp = -1;
    public int buttonDpadDown = -1;
    public int buttonDpadLeft = -1;
    public int buttonDpadRight = -1;

    private final File file;
    private int revision;

    private LegacyConfig(File file) {
        this.file = file;
    }

    public static LegacyConfig load() {
        File directory = new File(Minecraft.getMinecraftDir(), "config");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        LegacyConfig config = new LegacyConfig(new File(directory, "legacy4j-1.2.5.properties"));
        config.read();
        config.writeDefaults();
        instance = config;
        return config;
    }

    public static LegacyConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    public int revision() {
        return revision;
    }

    public void save() {
        revision++;
        writeDefaults();
    }

    private void read() {
        if (!file.isFile()) {
            return;
        }
        Properties values = new Properties();
        try {
            FileInputStream input = new FileInputStream(file);
            try {
                values.load(input);
            } finally {
                input.close();
            }
            enabled = bool(values, "enabled", enabled);
            controllerIndex = integer(values, "controller_index", controllerIndex);
            moveDeadZone = decimal(values, "move_dead_zone", moveDeadZone);
            lookDeadZone = decimal(values, "look_dead_zone", lookDeadZone);
            lookSensitivity = decimal(values, "look_sensitivity", lookSensitivity);
            menuCursorSpeed = decimal(values, "menu_cursor_speed", menuCursorSpeed);
            invertLookY = bool(values, "invert_look_y", invertLookY);
            showTooltips = bool(values, "show_tooltips", showTooltips);
            hudOpacity = decimal(values, "hud_opacity", hudOpacity);
            hudMargin = integer(values, "hud_margin", hudMargin);
            leftXAxis = integer(values, "axis_left_x", leftXAxis);
            leftYAxis = integer(values, "axis_left_y", leftYAxis);
            rightXAxis = integer(values, "axis_right_x", rightXAxis);
            rightYAxis = integer(values, "axis_right_y", rightYAxis);
            combinedTriggerAxis = integer(values, "axis_combined_triggers", combinedTriggerAxis);
            leftTriggerAxis = integer(values, "axis_left_trigger", leftTriggerAxis);
            rightTriggerAxis = integer(values, "axis_right_trigger", rightTriggerAxis);
            combinedTriggerLeftPositive = bool(values, "combined_trigger_left_positive", combinedTriggerLeftPositive);
            buttonA = integer(values, "button_a", buttonA);
            buttonB = integer(values, "button_b", buttonB);
            buttonX = integer(values, "button_x", buttonX);
            buttonY = integer(values, "button_y", buttonY);
            buttonLeftBumper = integer(values, "button_left_bumper", buttonLeftBumper);
            buttonRightBumper = integer(values, "button_right_bumper", buttonRightBumper);
            buttonBack = integer(values, "button_back", buttonBack);
            buttonStart = integer(values, "button_start", buttonStart);
            buttonLeftStick = integer(values, "button_left_stick", buttonLeftStick);
            buttonRightStick = integer(values, "button_right_stick", buttonRightStick);
            buttonLeftTrigger = integer(values, "button_left_trigger", buttonLeftTrigger);
            buttonRightTrigger = integer(values, "button_right_trigger", buttonRightTrigger);
            buttonDpadUp = integer(values, "button_dpad_up", buttonDpadUp);
            buttonDpadDown = integer(values, "button_dpad_down", buttonDpadDown);
            buttonDpadLeft = integer(values, "button_dpad_left", buttonDpadLeft);
            buttonDpadRight = integer(values, "button_dpad_right", buttonDpadRight);
        } catch (IOException ignored) {
        }
    }

    private void writeDefaults() {
        Properties values = new Properties();
        values.setProperty("enabled", String.valueOf(enabled));
        values.setProperty("controller_index", String.valueOf(controllerIndex));
        values.setProperty("move_dead_zone", String.valueOf(moveDeadZone));
        values.setProperty("look_dead_zone", String.valueOf(lookDeadZone));
        values.setProperty("look_sensitivity", String.valueOf(lookSensitivity));
        values.setProperty("menu_cursor_speed", String.valueOf(menuCursorSpeed));
        values.setProperty("invert_look_y", String.valueOf(invertLookY));
        values.setProperty("show_tooltips", String.valueOf(showTooltips));
        values.setProperty("hud_opacity", String.valueOf(hudOpacity));
        values.setProperty("hud_margin", String.valueOf(hudMargin));
        values.setProperty("axis_left_x", String.valueOf(leftXAxis));
        values.setProperty("axis_left_y", String.valueOf(leftYAxis));
        values.setProperty("axis_right_x", String.valueOf(rightXAxis));
        values.setProperty("axis_right_y", String.valueOf(rightYAxis));
        values.setProperty("axis_combined_triggers", String.valueOf(combinedTriggerAxis));
        values.setProperty("axis_left_trigger", String.valueOf(leftTriggerAxis));
        values.setProperty("axis_right_trigger", String.valueOf(rightTriggerAxis));
        values.setProperty("combined_trigger_left_positive", String.valueOf(combinedTriggerLeftPositive));
        values.setProperty("button_a", String.valueOf(buttonA));
        values.setProperty("button_b", String.valueOf(buttonB));
        values.setProperty("button_x", String.valueOf(buttonX));
        values.setProperty("button_y", String.valueOf(buttonY));
        values.setProperty("button_left_bumper", String.valueOf(buttonLeftBumper));
        values.setProperty("button_right_bumper", String.valueOf(buttonRightBumper));
        values.setProperty("button_back", String.valueOf(buttonBack));
        values.setProperty("button_start", String.valueOf(buttonStart));
        values.setProperty("button_left_stick", String.valueOf(buttonLeftStick));
        values.setProperty("button_right_stick", String.valueOf(buttonRightStick));
        values.setProperty("button_left_trigger", String.valueOf(buttonLeftTrigger));
        values.setProperty("button_right_trigger", String.valueOf(buttonRightTrigger));
        values.setProperty("button_dpad_up", String.valueOf(buttonDpadUp));
        values.setProperty("button_dpad_down", String.valueOf(buttonDpadDown));
        values.setProperty("button_dpad_left", String.valueOf(buttonDpadLeft));
        values.setProperty("button_dpad_right", String.valueOf(buttonDpadRight));
        try {
            FileOutputStream output = new FileOutputStream(file);
            try {
                values.store(output, "Legacy4J 1.2.5 controller settings (-1 means auto-detect)");
            } finally {
                output.close();
            }
        } catch (IOException ignored) {
        }
    }

    private static int integer(Properties p, String key, int fallback) {
        try {
            return Integer.parseInt(p.getProperty(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float decimal(Properties p, String key, float fallback) {
        try {
            return Float.parseFloat(p.getProperty(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        return Boolean.parseBoolean(p.getProperty(key, String.valueOf(fallback)));
    }
}
