package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import wily.legacy125.LegacyConfig;
import wily.legacy125.input.ControllerDescriptor;
import wily.legacy125.input.LwjglControllerInput;
import wily.legacy125.input.PadButton;

import java.util.List;

public final class LegacyControllerSettingsScreen extends LegacyPanelScreen {
    private final LegacyConfig config = LegacyConfig.get();
    private List<ControllerDescriptor> controllers;
    private int page;

    public LegacyControllerSettingsScreen(GuiScreen parent) {
        super(parent, "Controller Settings");
    }

    @Override
    protected void addLegacyControls() {
        controllers = new LwjglControllerInput(config).descriptors();
        rebuildPage();
    }

    private void rebuildPage() {
        controlList.clear();
        title = "Controller Settings  (" + (page + 1) + "/2)";
        int x = panelLeft + 16;
        int width = panelRight - panelLeft - 32;
        int y = panelTop + 34;
        if (page == 0) {
            for (int id = 1; id <= 5; id++) addButton(id, x, y + (id - 1) * 22, width, "");
        } else {
            addButton(6, x, y, width, "");
            addButton(7, x, y + 22, width, "");
            addButton(9, x, y + 44, width, "");
            addButton(10, x, y + 66, width, "");
            addButton(8, x, y + 88, width, "Controller Mapping & Diagnostics");
            addButton(12, x, y + 110, width, "Rescan Controllers");
        }
        addButton(90, panelLeft + 16, panelBottom - 29, 48, "<").enabled = page > 0;
        addButton(91, panelLeft + 69, panelBottom - 29, 48, ">").enabled = page < 1;
        updateLabels();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if ((button.id >= 1 && button.id <= 7) || button.id == 9 || button.id == 10) adjust(button.id, 1);
        if (button.id == 8) mc.displayGuiScreen(new LegacyControllerMappingScreen(this));
        if (button.id == 12) {
            LwjglControllerInput.refreshControllers();
            config.save();
            controllers = new LwjglControllerInput(config).descriptors();
            rebuildPage();
        }
        if (button.id == 90 && page > 0) { page--; rebuildPage(); }
        if (button.id == 91 && page < 1) { page++; rebuildPage(); }
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.DPAD_LEFT || button == PadButton.DPAD_RIGHT) {
            GuiButton focused = getFocusedButton();
            if (focused != null && ((focused.id >= 1 && focused.id <= 7) || focused.id == 9 || focused.id == 10)) {
                adjust(focused.id, button == PadButton.DPAD_LEFT ? -1 : 1);
            } else {
                super.onControllerButton(button);
            }
        } else if (button == PadButton.LEFT_BUMPER && page > 0) {
            page--;
            rebuildPage();
        } else if (button == PadButton.RIGHT_BUMPER && page < 1) {
            page++;
            rebuildPage();
        } else {
            super.onControllerButton(button);
        }
    }

    private void adjust(int id, int direction) {
        if (id == 1) config.controllerIndex = cycle(config.controllerIndex, direction, controllers.size());
        if (id == 2) config.moveDeadZone = clamp(config.moveDeadZone + direction * 0.05F, 0.0F, 0.9F);
        if (id == 3) config.lookDeadZone = clamp(config.lookDeadZone + direction * 0.05F, 0.0F, 0.9F);
        if (id == 4) config.lookSensitivity = clamp(config.lookSensitivity + direction * 0.25F, 0.5F, 6.0F);
        if (id == 5) config.invertLookY = !config.invertLookY;
        if (id == 6) config.hudOpacity = clamp(config.hudOpacity + direction * 0.1F, 0.1F, 1.0F);
        if (id == 7) config.hudMargin = Math.max(0, Math.min(40, config.hudMargin + direction * 2));
        if (id == 9) config.showTooltips = !config.showTooltips;
        if (id == 10) config.combinedTriggerLeftPositive = !config.combinedTriggerLeftPositive;
        config.save();
        updateLabels();
    }

    private void updateLabels() {
        String controller = "Auto";
        if (config.controllerIndex >= 0 && config.controllerIndex < controllers.size()) {
            controller = config.controllerIndex + ": " + controllers.get(config.controllerIndex).name;
        }
        setLabel(1, "Controller: " + shorten(controller, 44));
        setLabel(2, "Movement Dead Zone: " + percent(config.moveDeadZone));
        setLabel(3, "Camera Dead Zone: " + percent(config.lookDeadZone));
        setLabel(4, "Camera Sensitivity: " + oneDecimal(config.lookSensitivity));
        setLabel(5, "Invert Camera Y: " + yesNo(config.invertLookY));
        setLabel(6, "HUD Opacity: " + percent(config.hudOpacity));
        setLabel(7, "HUD Safe Margin: " + config.hudMargin + " px");
        setLabel(9, "Controller Tooltips: " + yesNo(config.showTooltips));
        setLabel(10, "Combined Trigger Polarity: " + (config.combinedTriggerLeftPositive ? "LT+ / RT-" : "LT- / RT+"));
    }

    @Override
    protected void drawControlHints() {
        LegacyTheme.drawCenteredControlHint(mc, "A Change     LB/RB Page     B Back",
                width / 2, height - 14);
    }

    private void setLabel(int id, String value) {
        for (Object object : controlList) {
            GuiButton button = (GuiButton) object;
            if (button.id == id) button.displayString = value;
        }
    }

    private static int cycle(int current, int direction, int count) {
        int values = count + 1;
        int normalized = current + 1;
        normalized = (normalized + direction) % values;
        if (normalized < 0) normalized += values;
        return normalized - 1;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String percent(float value) {
        return Math.round(value * 100.0F) + "%";
    }

    private static String oneDecimal(float value) {
        return String.valueOf(Math.round(value * 10.0F) / 10.0F);
    }

    private static String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private static String shorten(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
