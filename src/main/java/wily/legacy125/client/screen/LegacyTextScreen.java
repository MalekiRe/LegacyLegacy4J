package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import wily.legacy125.input.PadButton;

public final class LegacyTextScreen extends LegacyPanelScreen {
    private final String[][] pages;
    private int page;

    private LegacyTextScreen(GuiScreen parent, String title, String[][] pages) {
        super(parent, title);
        this.pages = pages;
    }

    public static LegacyTextScreen howToPlay(GuiScreen parent) {
        return new LegacyTextScreen(parent, "How To Play", new String[][] {
                {"CONTROLS", "Left stick moves. Right stick or the Steam Controller's right pad looks around. A jumps, right-stick click crouches, LT uses or places, and RT attacks or mines."},
                {"INVENTORY & CRAFTING", "X opens crafting, Y opens inventory, B drops the held item, and LB/RB changes the hotbar slot. Left-stick click changes perspective. While flying, the D-pad adds vertical or strafe movement."},
                {"SURVIVAL", "Gather wood before night, craft tools, build shelter, and keep the heart and food meters filled. Beds skip the night when it is safe to sleep."},
                {"STEAM CONTROLLER", "Launch the Minecraft launcher from Steam. Use Steam Input's Gamepad template: left pad as Left Joystick, right pad as Right Joystick, and triggers as analog gamepad triggers."}
        });
    }

    public static LegacyTextScreen patchNotes(GuiScreen parent) {
        return new LegacyTextScreen(parent, "What's New", new String[][] {
                {"LEGACY4J 1.2.5 BACKPORT", "A source-level Legacy4J compatibility port for Minecraft 1.2.5 and ModLoader."},
                {"CONTROLLER", "Steam Input virtual gamepads, dead zones, analog camera, gameplay actions, focused menu navigation and remappable axes/buttons."},
                {"INTERFACES", "Legacy-styled title, play, pause, settings, confirmation, How To Play and update-note screens are being rebuilt for the 1.2.5 APIs."}
        });
    }

    public static LegacyTextScreen credits(GuiScreen parent) {
        return new LegacyTextScreen(parent, "Credits", new String[][] {
                {"LEGACY4J", "Legacy4J by Wilyicaro and contributors. Minecraft by Mojang Studios. This compatibility port targets the Minecraft 1.2.5 client APIs."}
        });
    }

    public static LegacyTextScreen storeUnavailable(GuiScreen parent) {
        return new LegacyTextScreen(parent, "Minecraft Store", new String[][] {
                {"MINECRAFT STORE", "The modern Legacy4J store has no equivalent service in Minecraft 1.2.5. The menu entry is retained for one-to-one screen composition."}
        });
    }

    @Override
    protected void addLegacyControls() {
        int y = panelBottom - 32;
        addButton(1, panelLeft + 14, y, 50, "<").enabled = page > 0;
        addButton(2, panelRight - 64, y, 50, ">").enabled = page + 1 < pages.length;
        addButton(0, width / 2 - 50, y, 100, "Back");
        setFocusedIndex(2);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) goBack();
        if (button.id == 1 && page > 0) {
            page--;
            initGui();
        }
        if (button.id == 2 && page + 1 < pages.length) {
            page++;
            initGui();
        }
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.LEFT_BUMPER && page > 0) {
            page--;
            initGui();
        } else if (button == PadButton.RIGHT_BUMPER && page + 1 < pages.length) {
            page++;
            initGui();
        } else {
            super.onControllerButton(button);
        }
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        drawCenteredString(fontRenderer, pages[page][0], width / 2, panelTop + 39, 0xFFFFE080);
        fontRenderer.drawSplitString(pages[page][1], panelLeft + 18, panelTop + 58,
                panelRight - panelLeft - 36, 0xFFFFFFFF);
        String count = (page + 1) + " / " + pages.length;
        drawCenteredString(fontRenderer, count, width / 2, panelBottom - 46, 0xFFCCCCCC);
    }
}
