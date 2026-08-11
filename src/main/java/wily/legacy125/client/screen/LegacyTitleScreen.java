package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiScreen;
import wily.legacy125.input.ControllerDebugLog125;
import java.lang.reflect.Method;

public final class LegacyTitleScreen extends LegacyRenderableVListScreen {
    private static final long EXIT_GRACE_MILLIS = 250L;
    private static final Method RENDER_SKYBOX = findSkybox();
    private GuiMainMenu panorama;
    public LegacyTitleScreen() {
        super(null, "");
    }

    @Override
    protected void addListControls() {
        addListButton(1, "Play Game");
        addListButton(2, "Leaderboards");
        addListButton(7, "Languages");
        addListButton(3, "Help & Options");
        addListButton(8, "Minecraft Store");
        addListButton(5, "Exit Game");
        panorama = new GuiMainMenu();
        panorama.setWorldAndResolution(mc, width, height);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) mc.displayGuiScreen(new LegacyPlayScreen(this));
        if (button.id == 2) mc.displayGuiScreen(new net.minecraft.src.GuiStats(this, mc.statFileWriter));
        if (button.id == 7) mc.displayGuiScreen(new net.minecraft.src.GuiLanguage(this, mc.gameSettings));
        if (button.id == 3) mc.displayGuiScreen(new LegacyOptionsScreen(this, mc.gameSettings));
        if (button.id == 8) mc.displayGuiScreen(LegacyTextScreen.storeUnavailable(this));
        if (button.id == 5) exitGame();
    }

    @Override
    protected void drawLegacyBackground() {
        if (panorama != null && RENDER_SKYBOX != null) {
            try {
                RENDER_SKYBOX.invoke(panorama, Integer.valueOf(0), Integer.valueOf(0), Float.valueOf(0.0F));
                drawGradientRect(0, 0, width, height, 0x22000000, 0x55000000);
                return;
            } catch (Exception ignored) {
            }
        }
        drawGradientRect(0, 0, width, height, 0xFF546D7C, 0xFF18232C);
        drawGradientRect(0, height / 2, width, height, 0x00222D34, 0xCC071016);
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        super.drawScreenContents(mouseX, mouseY, partialTick);
        fontRenderer.drawStringWithShadow("Minecraft 1.2.5", 5, height - 12, 0xFFBFC6CA);
        String version = "Backport 0.4.0";
        fontRenderer.drawStringWithShadow(version, width - fontRenderer.getStringWidth(version) - 5,
                height - 12, 0xFFBFC6CA);
    }

    @Override
    protected void drawControlHints() {
        LegacyTheme.drawCenteredControlHint(mc, "A Select", width / 2, height - 14);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void exitGame() {
        ControllerDebugLog125.log("Exit Game selected; beginning normal shutdown with watchdog fallback");
        Thread watchdog = new Thread("Legacy4J exit watchdog") {
            @Override
            public void run() {
                try {
                    Thread.sleep(EXIT_GRACE_MILLIS);
                } catch (InterruptedException ignored) {
                    return;
                }
                // Several 1.2.5-era mods leave shutdown cleanup blocked after
                // the display has already been destroyed. At the title screen
                // no world is loaded, so force the process down after normal
                // Minecraft cleanup has had a reasonable chance to finish.
                Runtime.getRuntime().halt(0);
            }
        };
        watchdog.setDaemon(true);
        watchdog.start();
        mc.shutdown();
    }

    private static Method findSkybox() {
        String[] names = {"renderSkybox", "func_73971_c"};
        for (String name : names) {
            try {
                Method method = GuiMainMenu.class.getDeclaredMethod(name,
                        Integer.TYPE, Integer.TYPE, Float.TYPE);
                method.setAccessible(true);
                return method;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
