package wily.legacy125.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Tessellator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * 1.2.5 renderer for the measured Legacy4J panel and slot primitives. Modern
 * Legacy4J uses GUI sprites and nine-slices; these methods retain that API shape
 * while expressing the same bevels with primitives supported by LWJGL 2.
 */
public final class LegacyTheme {
    public static final int TEXT = 0xFF464646;
    public static final int MUTED_TEXT = 0xFF464646;
    public static final int FOCUS_TEXT = 0xFFFFFF55;
    public static final int CONTROL_HINT_TEXT = 0x90FFFFFF;
    private static final float CONTROL_HINT_SCALE = 0.40F;

    private LegacyTheme() {
    }

    public static void panel(Minecraft minecraft, int left, int top, int right, int bottom) {
        nineSlice(minecraft, "/legacy4j125/gui/panel.png", left, top, right, bottom);
    }

    public static void recess(Minecraft minecraft, int left, int top, int right, int bottom) {
        nineSlice(minecraft, "/legacy4j125/gui/panel_recess.png", left, top, right, bottom);
    }

    public static void squareRecess(Minecraft minecraft, int left, int top, int right, int bottom) {
        nineSlice(minecraft, "/legacy4j125/gui/square_recessed_panel.png", left, top, right, bottom,
                8, 24, 24);
    }

    public static void entityPanel(Minecraft minecraft, int left, int top, int right, int bottom) {
        nineSlice(minecraft, "/legacy4j125/gui/square_entity_panel.png", left, top, right, bottom,
                8, 24, 24);
    }

    public static void button(Minecraft minecraft, int left, int top, int right, int bottom,
                              boolean highlighted) {
        nineSlice(minecraft, highlighted ? "/legacy4j125/gui/button_highlighted.png"
                        : "/legacy4j125/gui/button.png", left, top, right, bottom,
                3, 200, 20);
    }

    public static void slot(Minecraft minecraft, int x, int y) {
        slot(minecraft, x, y, 16);
    }

    public static void slot(Minecraft minecraft, int x, int y, int size) {
        // LegacyIconHolder uses the ordinary holder in SD at every size. Its
        // corner is offset by size / 20 rather than switching to the HD-only
        // sizeable nine-slice.
        int corner = Math.max(1, Math.round(size / 20.0F));
        texture(minecraft, "/legacy4j125/gui/icon_holder.png", x - corner, y - corner, size, size,
                0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void resultSlot(Minecraft minecraft, int x, int y, int size) {
        texture(minecraft, "/legacy4j125/gui/non_interactive_result_slot.png", x, y, size, size,
                0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void warningSlot(Minecraft minecraft, int x, int y, int size) {
        int corner = Math.max(1, Math.round(size / 20.0F));
        texture(minecraft, "/legacy4j125/gui/red_icon_holder.png", x - corner, y - corner, size, size,
                0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void warning(Minecraft minecraft, int x, int y, int size) {
        texture(minecraft, "/legacy4j125/gui/icon_warning.png", x, y, size, size,
                0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void recipeSlot(Minecraft minecraft, int x, int y, boolean available) {
        // Upstream keeps the holder unchanged and makes only an unavailable
        // result translucent (RecipeIconHolder#renderItem).
        texture(minecraft, "/legacy4j125/gui/icon_holder.png", x - 1, y - 1, 20, 20,
                0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void armorSlot(Minecraft minecraft, String icon, int x, int y, int size) {
        slot(minecraft, x, y, size);
        // LegacyIconHolder scales sub-18px placeholder icons to the selectable
        // inner area rather than the full holder bounds.
        float selectable = LegacySlotGeometry125.selectableSize(size);
        texture(minecraft, icon, x, y, selectable, selectable,
                0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void focus(Minecraft minecraft, int x, int y) {
        focus(minecraft, x, y, 16);
    }

    public static void focus(Minecraft minecraft, int x, int y, int size) {
        // LegacyIconHolder.renderHighlight starts at the logical slot origin
        // and scales the 16px sprite to the holder's selectable inner area.
        float selectable = LegacySlotGeometry125.selectableSize(size);
        texture(minecraft, "/legacy4j125/gui/slot_highlight.png",
                x, y, selectable, selectable, 0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void recipeFocus(Minecraft minecraft, int x, int y) {
        texture(minecraft, "/legacy4j125/gui/select_icon_highlight_small.png",
                x - 2, y - 2, 21, 21, 0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void recipeScroll(Minecraft minecraft, int x, int y) {
        texture(minecraft, "/legacy4j125/gui/scroll_up_very_small.png",
                x + 6, y - 6, 7, 4, 0.0D, 0.0D, 1.0D, 1.0D);
        texture(minecraft, "/legacy4j125/gui/scroll_down_very_small.png",
                x + 6, y + 22, 7, 4, 0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void arrow(Minecraft minecraft, int x, int y, int progress) {
        texture(minecraft, "/legacy4j125/gui/arrow.png", x, y, 22, 16, 0.0D, 0.0D, 1.0D, 1.0D);
        int filled = Math.max(0, Math.min(22, progress));
        if (filled > 0) texture(minecraft, "/legacy4j125/gui/full_arrow.png", x, y, filled, 16,
                0.0D, 0.0D, filled / 22.0D, 1.0D);
    }

    public static void smallArrow(Minecraft minecraft, int x, int y, int progress) {
        texture(minecraft, "/legacy4j125/gui/small_arrow.png", x, y, 16, 14,
                0.0D, 0.0D, 1.0D, 1.0D);
        int filled = Math.max(0, Math.min(16, progress));
        if (filled > 0) texture(minecraft, "/legacy4j125/gui/full_small_arrow.png", x, y, filled, 14,
                0.0D, 0.0D, filled / 16.0D, 1.0D);
    }

    public static void flame(Minecraft minecraft, int x, int y, int progress) {
        texture(minecraft, "/legacy4j125/gui/lit.png", x, y, 14, 14,
                0.0D, 0.0D, 1.0D, 1.0D);
        int filled = Math.max(0, Math.min(14, progress));
        if (filled > 0) texture(minecraft, "/legacy4j125/gui/lit_progress.png",
                x, y + 14 - filled, 14, filled,
                0.0D, (14 - filled) / 14.0D, 1.0D, 1.0D);
    }

    public static void horizontalTab(Minecraft minecraft, int x, int y, int width, int height,
                                     boolean selected, int position, int count) {
        String texture = "/legacy4j125/gui/big_low_tab.png";
        if (selected) {
            if (position == 0) texture = "/legacy4j125/gui/big_high_tab_left.png";
            else if (position == count - 1) texture = "/legacy4j125/gui/big_high_tab_right.png";
            else texture = "/legacy4j125/gui/big_high_tab_middle.png";
        }
        nineSlice(minecraft, texture, x, y, x + width, y + height, 8, 24, 24);
    }

    public static void verticalTab(Minecraft minecraft, int x, int y, int width, int height, boolean selected) {
        nineSlice(minecraft, selected ? "/legacy4j125/gui/big_high_vert_tab_up.png"
                        : "/legacy4j125/gui/big_low_vert_tab.png",
                x, y, x + width, y + height, 8, 24, 24);
    }

    public static void icon(Minecraft minecraft, String path, int x, int y, int size) {
        texture(minecraft, path, x, y, size, size, 0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void image(Minecraft minecraft, String path, int x, int y, int width, int height) {
        texture(minecraft, path, x, y, width, height, 0.0D, 0.0D, 1.0D, 1.0D);
    }

    public static void drawString(Minecraft minecraft, String text, int x, int y, int color) {
        drawString(minecraft, text, x, y, color, false);
    }

    public static void drawString(Minecraft minecraft, String text, int x, int y, int color, boolean shadow) {
        if (shadow) drawLegacyString(minecraft, text, x + 0.5F, y + 0.5F, dim(color));
        drawLegacyString(minecraft, text, x, y, color);
    }

    public static void drawCenteredString(Minecraft minecraft, String text, int centerX, int y,
                                          int color, boolean shadow) {
        drawString(minecraft, text, centerX - stringWidth(text) / 2, y, color, shadow);
    }

    /** Regular menu buttons use the larger UI font; compact container labels use the SD font above. */
    public static void drawMenuCenteredString(Minecraft minecraft, String text, int centerX, int y,
                                              int color, boolean shadow) {
        int width = scaledStringWidth(text, 0.75F);
        if (shadow) drawLegacyString(minecraft, text, centerX - width / 2 + 0.5F,
                y + 0.5F, dim(color), 0.75F);
        drawLegacyString(minecraft, text, centerX - width / 2, y, color, 0.75F);
    }

    public static void drawMenuString(Minecraft minecraft, String text, int x, int y,
                                      int color, boolean shadow) {
        if (shadow) drawLegacyString(minecraft, text, x + 0.5F, y + 0.5F, dim(color), 0.75F);
        drawLegacyString(minecraft, text, x, y, color, 0.75F);
    }

    public static void drawControlHint(Minecraft minecraft, String text, int x, int y) {
        drawControlHint(minecraft, text, x, y, CONTROL_HINT_TEXT, true);
    }

    public static void drawControlHint(Minecraft minecraft, String text, int x, int y,
                                       int color, boolean shadow) {
        if (shadow) drawLegacyString(minecraft, text, x + 0.5F, y + 0.5F, dim(color),
                CONTROL_HINT_SCALE);
        drawLegacyString(minecraft, text, x, y, color, CONTROL_HINT_SCALE);
    }

    public static void drawCenteredControlHint(Minecraft minecraft, String text, int centerX, int y) {
        drawControlHint(minecraft, text, centerX - controlHintWidth(text) / 2, y);
    }

    public static int controlHintWidth(String text) {
        return scaledStringWidth(text, CONTROL_HINT_SCALE);
    }

    public static int stringWidth(String text) {
        return scaledStringWidth(text, 0.5F);
    }

    private static void drawLegacyString(Minecraft minecraft, String text, float x, float y, int color) {
        drawLegacyString(minecraft, text, x, y, color, 0.5F);
    }

    private static void drawLegacyString(Minecraft minecraft, String text, float x, float y,
                                         int color, float scale) {
        boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        if (lighting) GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,
                minecraft.renderEngine.getTexture("/legacy4j125/gui/default_11.png"));
        // Modern bitmap-font shaders use the atlas alpha as a mask and the
        // requested text color as RGB. Fixed-function MODULATE would multiply
        // by the atlas' 235-gray RGB and make unselected text visibly muddy.
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE1_ALPHA, GL13.GL_PRIMARY_COLOR);
        GL11.glColor4f(((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F, ((color >>> 24) & 255) / 255.0F);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);
        int cursor = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int packed = glyphCell(character);
            if (packed >= 0) {
                int column = packed & 255;
                int row = packed >>> 8;
                glyphQuad(cursor, column * 12, row * 16);
            }
            cursor += glyphSourceAdvance(character);
        }
        GL11.glPopMatrix();
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        if (lighting) GL11.glEnable(GL11.GL_LIGHTING);
    }

    private static int scaledStringWidth(String text, float scale) {
        int sourcePixels = 0;
        for (int i = 0; i < text.length(); i++) sourcePixels += glyphSourceAdvance(text.charAt(i));
        return Math.round(sourcePixels * scale);
    }

    private static void glyphQuad(int x, int sourceX, int sourceY) {
        double u0 = sourceX / 276.0D;
        double v0 = sourceY / 320.0D;
        double u1 = (sourceX + 12) / 276.0D;
        double v1 = (sourceY + 16) / 320.0D;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, 16, 0.0D, u0, v1);
        tessellator.addVertexWithUV(x + 12, 16, 0.0D, u1, v1);
        tessellator.addVertexWithUV(x + 12, 0, 0.0D, u1, v0);
        tessellator.addVertexWithUV(x, 0, 0.0D, u0, v0);
        tessellator.draw();
    }

    private static int glyphCell(char character) {
        if (character >= 33 && character <= 45) return (1 << 8) | (10 + character - 33);
        if (character >= 46 && character <= 68) return (2 << 8) | (character - 46);
        if (character >= 69 && character <= 91) return (3 << 8) | (character - 69);
        if (character >= 92 && character <= 114) return (4 << 8) | (character - 92);
        if (character >= 115 && character <= 127) return (5 << 8) | (character - 115);
        return -1;
    }

    private static int glyphSourceAdvance(char character) {
        // BitmapProvider advances by the visible source width plus one pixel;
        // the 16px rows are rendered at half scale in SD mode.
        if (character == ' ') return 8;
        if (character >= '0' && character <= '9') return character == '1' ? 7 : 8;
        if (character >= 'A' && character <= 'Z') return character == 'I' ? 4 : 8;
        if (character >= 'a' && character <= 'z') {
            if (character == 'i') return 2;
            if (character == 'l' || character == 't') return 4;
            if (character == 'k') return 6;
            if (character == 'm' || character == 'w' || character == 'z') return 8;
            return 7;
        }
        switch (character) {
            case '!': case '.': case ':': case '|': return 2;
            case '\'': case ',': case ';': case '`': return 3;
            case '"': case '[': return 5;
            case ']': return 4;
            case '(': case ')': case '<': case '>': case '\\': return 6;
            case '$': case '-': case '/': case '{': case '}': return 7;
            case '*': case '+': case '=': case '?': case '@': case '^': case '_': return 8;
            case '#': case '%': case '&': case '~': return 9;
            default: return 8;
        }
    }

    private static int dim(int color) {
        int alpha = color >>> 24;
        int red = ((color >> 16) & 255) / 4;
        int green = ((color >> 8) & 255) / 4;
        int blue = (color & 255) / 4;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static void nineSlice(Minecraft minecraft, String texture, int left, int top, int right, int bottom) {
        nineSlice(minecraft, texture, left, top, right, bottom, 8, 24, 24);
    }

    private static void nineSlice(Minecraft minecraft, String texture, int left, int top, int right, int bottom,
                                  int desiredBorder, int logicalWidth, int logicalHeight) {
        int border = Math.min(desiredBorder, Math.min((right - left) / 2, (bottom - top) / 2));
        double sourceBorderX = desiredBorder / (double) logicalWidth;
        double sourceBorderY = desiredBorder / (double) logicalHeight;
        int innerLeft = left + border;
        int innerRight = right - border;
        int innerTop = top + border;
        int innerBottom = bottom - border;
        texture(minecraft, texture, left, top, border, border, 0, 0, sourceBorderX, sourceBorderY);
        tiledTexture(minecraft, texture, innerLeft, top, innerRight - innerLeft, border,
                sourceBorderX, 0, 1 - sourceBorderX, sourceBorderY,
                logicalWidth - desiredBorder * 2, desiredBorder);
        texture(minecraft, texture, innerRight, top, border, border,
                1 - sourceBorderX, 0, 1, sourceBorderY);
        tiledTexture(minecraft, texture, left, innerTop, border, innerBottom - innerTop,
                0, sourceBorderY, sourceBorderX, 1 - sourceBorderY,
                desiredBorder, logicalHeight - desiredBorder * 2);
        tiledTexture(minecraft, texture, innerLeft, innerTop, innerRight - innerLeft, innerBottom - innerTop,
                sourceBorderX, sourceBorderY, 1 - sourceBorderX, 1 - sourceBorderY,
                logicalWidth - desiredBorder * 2, logicalHeight - desiredBorder * 2);
        tiledTexture(minecraft, texture, innerRight, innerTop, border, innerBottom - innerTop,
                1 - sourceBorderX, sourceBorderY, 1, 1 - sourceBorderY,
                desiredBorder, logicalHeight - desiredBorder * 2);
        texture(minecraft, texture, left, innerBottom, border, border,
                0, 1 - sourceBorderY, sourceBorderX, 1);
        tiledTexture(minecraft, texture, innerLeft, innerBottom, innerRight - innerLeft, border,
                sourceBorderX, 1 - sourceBorderY, 1 - sourceBorderX, 1,
                logicalWidth - desiredBorder * 2, desiredBorder);
        texture(minecraft, texture, innerRight, innerBottom, border, border,
                1 - sourceBorderX, 1 - sourceBorderY, 1, 1);
    }

    private static void tiledTexture(Minecraft minecraft, String texture, int x, int y,
                                     int width, int height, double u0, double v0, double u1, double v1,
                                     int tileWidth, int tileHeight) {
        if (width <= 0 || height <= 0 || tileWidth <= 0 || tileHeight <= 0) return;
        for (int offsetY = 0; offsetY < height; offsetY += tileHeight) {
            int partHeight = Math.min(tileHeight, height - offsetY);
            double partV1 = v0 + (v1 - v0) * partHeight / tileHeight;
            for (int offsetX = 0; offsetX < width; offsetX += tileWidth) {
                int partWidth = Math.min(tileWidth, width - offsetX);
                double partU1 = u0 + (u1 - u0) * partWidth / tileWidth;
                texture(minecraft, texture, x + offsetX, y + offsetY, partWidth, partHeight,
                        u0, v0, partU1, partV1);
            }
        }
    }

    private static void texture(Minecraft minecraft, String path, float x, float y, float width, float height,
                                double u0, double v0, double u1, double v1) {
        if (width <= 0 || height <= 0) return;
        // RenderHelper's item lighting must not tint GUI sprites that happen to
        // be drawn between item calls (notably warning holders and arrows).
        boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        if (lighting) GL11.glDisable(GL11.GL_LIGHTING);
        if (!blend) GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, minecraft.renderEngine.getTexture(path));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0.0D, u0, v1);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, u1, v1);
        tessellator.addVertexWithUV(x + width, y, 0.0D, u1, v0);
        tessellator.addVertexWithUV(x, y, 0.0D, u0, v0);
        tessellator.draw();
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        if (lighting) GL11.glEnable(GL11.GL_LIGHTING);
    }
}
