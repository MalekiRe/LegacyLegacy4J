package wily.legacy125;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Gui;
import net.minecraft.src.ScaledResolution;
import wily.legacy125.client.screen.LegacyTheme;

/** Lightweight Legacy Console-style action strip and virtual controller cursor. */
final class LegacyHud {
    private static final int PROMPT_GAP = 5;

    private LegacyHud() {
    }

    static void renderGameplay(Minecraft minecraft, String selectedItem, boolean flying, boolean autosaving) {
        if (minecraft.gameSettings.hideGUI) return;
        LegacyConfig config = LegacyConfig.get();
        ScaledResolution scaled = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth,
                minecraft.displayHeight);
        int y = scaled.getScaledHeight() - 11 - config.hudMargin;
        int right = scaled.getScaledWidth();
        int x = PromptStripLayout125.rightAlignedStart(right, PROMPT_GAP,
                promptWidth("LT", "Use"), promptWidth("RT", "Mine"),
                promptWidth("X", "Craft"), promptWidth("Y", "Inventory"));
        Gui.drawRect(x - 3, y - 3, right,
                scaled.getScaledHeight() - config.hudMargin, background(config));
        x = prompt(minecraft, x, y, "LT", "Use", 0xFFEEEEEE);
        x = prompt(minecraft, x, y, "RT", "Mine", 0xFFEEEEEE);
        x = prompt(minecraft, x, y, "X", "Craft", 0xFF44A8E8);
        prompt(minecraft, x, y, "Y", "Inventory", 0xFFFFD339, false);

        if (!selectedItem.isEmpty()) {
            int itemWidth = minecraft.fontRenderer.getStringWidth(selectedItem) + 12;
            int itemX = (scaled.getScaledWidth() - itemWidth) / 2;
            int itemY = scaled.getScaledHeight() - 42 - config.hudMargin;
            Gui.drawRect(itemX, itemY - 3, itemX + itemWidth, itemY + 11, background(config));
            minecraft.fontRenderer.drawStringWithShadow(selectedItem, itemX + 6, itemY, 0xFFFFFFFF);
        }

        if (flying) {
            String flyingLabel = "Flying";
            int flyingX = (scaled.getScaledWidth() - minecraft.fontRenderer.getStringWidth(flyingLabel)) / 2;
            minecraft.fontRenderer.drawStringWithShadow(flyingLabel, flyingX,
                    scaled.getScaledHeight() - 54 - config.hudMargin, 0xFF80D8FF);
        }


        if (autosaving) {
            String savingLabel = "Autosaved";
            int savingX = scaled.getScaledWidth() - minecraft.fontRenderer.getStringWidth(savingLabel)
                    - config.hudMargin;
            minecraft.fontRenderer.drawStringWithShadow(savingLabel, savingX, config.hudMargin, 0xFFFFFFFF);
        }

    }

    static void renderMenu(Minecraft minecraft, float cursorX, float cursorY) {
        LegacyConfig config = LegacyConfig.get();
        ScaledResolution scaled = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth,
                minecraft.displayHeight);
        int y = scaled.getScaledHeight() - 11 - config.hudMargin;
        Gui.drawRect(config.hudMargin, y - 3, scaled.getScaledWidth() - config.hudMargin,
                scaled.getScaledHeight() - config.hudMargin, background(config));
        int x = 8 + config.hudMargin;
        x = prompt(minecraft, x, y, "A", "Select", 0xFF57C84D);
        prompt(minecraft, x, y, "B", "Back", 0xFFE8564A);

        int cx = Math.round(cursorX);
        int cy = Math.round(cursorY);
        Gui.drawRect(cx - 5, cy - 1, cx + 6, cy + 2, 0xFFFFFFFF);
        Gui.drawRect(cx - 1, cy - 5, cx + 2, cy + 6, 0xFFFFFFFF);
        Gui.drawRect(cx - 3, cy - 3, cx + 4, cy + 4, 0xFF202020);
        Gui.drawRect(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);
    }

    static void renderContainerFocus(Minecraft minecraft, int slotX, int slotY) {
        // Upstream treats ordinary, non-Legacy slots as 18px holders. Their
        // selectable area begins at the slot/item origin and is approximately
        // 16px square. Use the same translucent sprite as custom inventories;
        // the old fallback was an opaque 20px rectangle shifted up and left.
        LegacyTheme.focus(minecraft, slotX, slotY, 18);
        LegacyConfig config = LegacyConfig.get();
        ScaledResolution scaled = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth,
                minecraft.displayHeight);
        int y = scaled.getScaledHeight() - 11 - config.hudMargin;
        Gui.drawRect(config.hudMargin, y - 3, scaled.getScaledWidth() - config.hudMargin,
                scaled.getScaledHeight() - config.hudMargin, background(config));
        int x = 8 + config.hudMargin;
        x = prompt(minecraft, x, y, "A", "Pick Up", 0xFF57C84D);
        x = prompt(minecraft, x, y, "X", "Quick Move", 0xFF44A8E8);
        prompt(minecraft, x, y, "B", "Exit", 0xFFE8564A);
    }

    private static int prompt(Minecraft minecraft, int x, int y, String badge, String text, int color) {
        return prompt(minecraft, x, y, badge, text, color, true);
    }

    private static int prompt(Minecraft minecraft, int x, int y, String badge, String text, int color,
                              boolean gapAfter) {
        int badgeWidth = Math.max(9, LegacyTheme.controlHintWidth(badge) + 4);
        Gui.drawRect(x, y - 1, x + badgeWidth, y + 7, 0x80303030);
        Gui.drawRect(x + 1, y, x + badgeWidth - 1, y + 6, withAlpha(color, 0xA0));
        LegacyTheme.drawControlHint(minecraft, badge, x + 2, y, 0xB0101010, false);
        LegacyTheme.drawControlHint(minecraft, text, x + badgeWidth + 2, y);
        return x + promptWidth(badge, text) + (gapAfter ? PROMPT_GAP : 0);
    }

    private static int promptWidth(String badge, String text) {
        int badgeWidth = Math.max(9, LegacyTheme.controlHintWidth(badge) + 4);
        return PromptStripLayout125.promptWidth(badgeWidth, LegacyTheme.controlHintWidth(text));
    }

    private static int background(LegacyConfig config) {
        int alpha = Math.max(0, Math.min(255, Math.round(config.hudOpacity * 255.0F)));
        return alpha << 24;
    }

    private static int withAlpha(int color, int alpha) {
        return color & 0x00FFFFFF | alpha << 24;
    }
}
