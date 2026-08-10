package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import wily.legacy125.LegacyConfig;
import wily.legacy125.input.ControllerDescriptor;
import wily.legacy125.input.LwjglControllerInput;
import wily.legacy125.input.PadButton;

import java.util.Collections;
import java.util.List;

public final class LegacyControllerMappingScreen extends LegacyPanelScreen {
    private static final String[] LABELS = {
            "Left Stick X", "Left Stick Y", "Right Stick X", "Right Stick Y", "Combined Triggers",
            "Left Trigger Axis", "Right Trigger Axis", "A Button", "B Button", "X Button", "Y Button",
            "Left Bumper", "Right Bumper", "Back", "Start", "Left Stick Click", "Right Stick Click",
            "Left Trigger Button", "Right Trigger Button", "D-pad Up", "D-pad Down", "D-pad Left", "D-pad Right"
    };
    private static final int ROWS = 6;
    private final LegacyConfig config = LegacyConfig.get();
    private List<ControllerDescriptor> controllers = Collections.emptyList();
    private ControllerDescriptor descriptor;
    private int page;

    public LegacyControllerMappingScreen(GuiScreen parent) {
        super(parent, "Controller Mapping & Diagnostics");
    }

    @Override
    protected void addLegacyControls() {
        controllers = new LwjglControllerInput(config).descriptors();
        descriptor = chooseDescriptor();
        rebuild();
    }

    private void rebuild() {
        controlList.clear();
        int start = page * ROWS;
        int x = panelLeft + 16;
        int y = panelTop + 42;
        for (int row = 0; row < ROWS && start + row < LABELS.length; row++) {
            addButton(100 + start + row, x, y + row * 22, panelRight - panelLeft - 32, "");
        }
        addButton(1, panelLeft + 16, panelBottom - 29, 48, "<").enabled = page > 0;
        addButton(2, panelLeft + 69, panelBottom - 29, 48, ">").enabled = (page + 1) * ROWS < LABELS.length;
        addButton(0, panelRight - 108, panelBottom - 29, 92, "Done");
        updateLabels();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) goBack();
        if (button.id == 1 && page > 0) { page--; rebuild(); }
        if (button.id == 2 && (page + 1) * ROWS < LABELS.length) { page++; rebuild(); }
        if (button.id >= 100) adjust(button.id - 100, 1);
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.DPAD_LEFT || button == PadButton.DPAD_RIGHT) {
            GuiButton focused = getFocusedButton();
            if (focused != null && focused.id >= 100) {
                adjust(focused.id - 100, button == PadButton.DPAD_LEFT ? -1 : 1);
            } else {
                super.onControllerButton(button);
            }
        } else if (button == PadButton.LEFT_BUMPER && page > 0) {
            page--;
            rebuild();
        } else if (button == PadButton.RIGHT_BUMPER && (page + 1) * ROWS < LABELS.length) {
            page++;
            rebuild();
        } else if (button == PadButton.Y) {
            LwjglControllerInput.refreshControllers();
            config.save();
            controllers = new LwjglControllerInput(config).descriptors();
            descriptor = chooseDescriptor();
            rebuild();
        } else {
            super.onControllerButton(button);
        }
    }

    private void adjust(int mapping, int direction) {
        boolean axis = mapping <= 6;
        int max = descriptor == null ? 0 : (axis ? descriptor.axes.size() : descriptor.buttons.size());
        set(mapping, cycle(get(mapping), direction, max));
        config.save();
        updateLabels();
    }

    private int get(int i) {
        switch (i) {
            case 0: return config.leftXAxis;
            case 1: return config.leftYAxis;
            case 2: return config.rightXAxis;
            case 3: return config.rightYAxis;
            case 4: return config.combinedTriggerAxis;
            case 5: return config.leftTriggerAxis;
            case 6: return config.rightTriggerAxis;
            case 7: return config.buttonA;
            case 8: return config.buttonB;
            case 9: return config.buttonX;
            case 10: return config.buttonY;
            case 11: return config.buttonLeftBumper;
            case 12: return config.buttonRightBumper;
            case 13: return config.buttonBack;
            case 14: return config.buttonStart;
            case 15: return config.buttonLeftStick;
            case 16: return config.buttonRightStick;
            case 17: return config.buttonLeftTrigger;
            case 18: return config.buttonRightTrigger;
            case 19: return config.buttonDpadUp;
            case 20: return config.buttonDpadDown;
            case 21: return config.buttonDpadLeft;
            default: return config.buttonDpadRight;
        }
    }

    private void set(int i, int value) {
        switch (i) {
            case 0: config.leftXAxis = value; break;
            case 1: config.leftYAxis = value; break;
            case 2: config.rightXAxis = value; break;
            case 3: config.rightYAxis = value; break;
            case 4: config.combinedTriggerAxis = value; break;
            case 5: config.leftTriggerAxis = value; break;
            case 6: config.rightTriggerAxis = value; break;
            case 7: config.buttonA = value; break;
            case 8: config.buttonB = value; break;
            case 9: config.buttonX = value; break;
            case 10: config.buttonY = value; break;
            case 11: config.buttonLeftBumper = value; break;
            case 12: config.buttonRightBumper = value; break;
            case 13: config.buttonBack = value; break;
            case 14: config.buttonStart = value; break;
            case 15: config.buttonLeftStick = value; break;
            case 16: config.buttonRightStick = value; break;
            case 17: config.buttonLeftTrigger = value; break;
            case 18: config.buttonRightTrigger = value; break;
            case 19: config.buttonDpadUp = value; break;
            case 20: config.buttonDpadDown = value; break;
            case 21: config.buttonDpadLeft = value; break;
            case 22: config.buttonDpadRight = value; break;
            default: break;
        }
    }

    private void updateLabels() {
        for (Object object : controlList) {
            GuiButton button = (GuiButton) object;
            if (button.id >= 100) {
                int mapping = button.id - 100;
                int value = get(mapping);
                String name = mappingName(mapping, value);
                button.displayString = LABELS[mapping] + ": " + name;
            }
        }
    }

    private ControllerDescriptor chooseDescriptor() {
        if (controllers.isEmpty()) return null;
        if (config.controllerIndex >= 0 && config.controllerIndex < controllers.size()) {
            return controllers.get(config.controllerIndex);
        }
        return controllers.get(0);
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        String text = descriptor == null ? "No gamepad detected" : descriptor.index + ": " + descriptor.name
                + "  (" + descriptor.axes.size() + " axes, " + descriptor.buttons.size() + " buttons)";
        drawCenteredString(fontRenderer, text, width / 2, panelTop + 29, descriptor == null ? 0xFFFF7777 : 0xFFD7D7D7);
    }

    @Override
    protected void drawControlHints() {
        LegacyTheme.drawCenteredControlHint(mc,
                "A Change     LB/RB Page     Y Rescan     B Back", width / 2, height - 14);
    }

    private String mappingName(int mapping, int value) {
        if (value < 0) return mapping <= 6 ? "Auto" : "Disabled";
        if (descriptor == null) return String.valueOf(value);
        List<String> values = mapping <= 6 ? descriptor.axes : descriptor.buttons;
        if (value >= values.size()) return value + " (missing)";
        String result = values.get(value);
        return result.length() <= 28 ? result : result.substring(0, 25) + "...";
    }

    private static int cycle(int current, int direction, int count) {
        int values = count + 1;
        int normalized = current + 1;
        normalized = (normalized + direction) % values;
        if (normalized < 0) normalized += values;
        return normalized - 1;
    }
}
