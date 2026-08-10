package wily.legacy125.client.screen;

import net.minecraft.src.Gui;
import net.minecraft.src.GuiAchievements;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiStats;
import net.minecraft.src.StatList;
import wily.legacy125.input.ControllerDebugLog125;

public final class LegacyPauseScreen extends LegacyRenderableVListScreen {
    public LegacyPauseScreen() {
        super(null, "Game Menu");
    }

    @Override
    protected void addListControls() {
        addListButton(1, "Resume Game");
        addListButton(3, "Help & Options");
        addListButton(5, "Leaderboards");
        addListButton(4, "Advancements");
        addListButton(6, "Exit Game");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) resume();
        if (button.id == 3) mc.displayGuiScreen(new LegacyOptionsScreen(this, mc.gameSettings));
        if (button.id == 4) mc.displayGuiScreen(new GuiAchievements(mc.statFileWriter));
        if (button.id == 5) mc.displayGuiScreen(new GuiStats(this, mc.statFileWriter));
        if (button.id == 6) {
            mc.displayGuiScreen(new LegacyConfirmationScreen(this, "Exit Game",
                    "Save and return to the main menu?", new LegacyConfirmationScreen.Callback() {
                public void answer(boolean confirmed) {
                    if (!confirmed) {
                        mc.displayGuiScreen(LegacyPauseScreen.this);
                        return;
                    }
                    mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
                    if (mc.isMultiplayerWorld()) mc.theWorld.sendQuittingDisconnectingPacket();
                    mc.changeWorld1(null);
                    mc.displayGuiScreen(new LegacyTitleScreen());
                }
            }));
        }
    }

    @Override
    protected void drawLegacyBackground() {
        Gui.drawRect(0, 0, width, height, 0x99000000);
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        super.drawScreenContents(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawControlHints() {
        LegacyTheme.image(mc, "/legacy4j125/gui/esc_key.png", 33, height - 22, 8, 8);
        LegacyTheme.drawControlHint(mc, "Back", 42, height - 20);
    }

    @Override
    protected void goBack() {
        resume();
    }

    private void resume() {
        ControllerDebugLog125.log("resume requested; closing pause GUI");
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }
}
