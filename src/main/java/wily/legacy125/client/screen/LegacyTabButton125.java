package wily.legacy125.client.screen;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Atomic sprite-and-icon tab matching Legacy4J's LegacyTabButton render unit. */
final class LegacyTabButton125 {
    final int x;
    final int y;
    final int width;
    final int height;
    final int position;
    final int typeCount;
    final boolean vertical;
    final String icon;
    final String label;
    final int iconX;
    final int iconY;
    final float unselectedOffsetX;
    final float unselectedOffsetY;

    LegacyTabButton125(int x, int y, int width, int height, int position, int typeCount,
                       boolean vertical, String icon, int iconX, int iconY,
                       float unselectedOffsetX, float unselectedOffsetY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.position = position;
        this.typeCount = typeCount;
        this.vertical = vertical;
        this.icon = icon;
        this.label = null;
        this.iconX = iconX;
        this.iconY = iconY;
        this.unselectedOffsetX = unselectedOffsetX;
        this.unselectedOffsetY = unselectedOffsetY;
    }

    LegacyTabButton125(int x, int y, int width, int height, int position, int typeCount,
                       String label, float unselectedOffsetY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.position = position;
        this.typeCount = typeCount;
        this.vertical = false;
        this.icon = null;
        this.label = label;
        this.iconX = 0;
        this.iconY = 0;
        this.unselectedOffsetX = 0.0F;
        this.unselectedOffsetY = unselectedOffsetY;
    }

    void render(Minecraft minecraft, boolean selected) {
        GL11.glPushMatrix();
        if (!selected) GL11.glTranslatef(unselectedOffsetX, unselectedOffsetY, 0.0F);
        if (vertical) LegacyTheme.verticalTab(minecraft, x, y, width, height, selected);
        else LegacyTheme.horizontalTab(minecraft, x, y, width, height, selected, position, typeCount);
        // LegacyTabButton moves an inactive icon one logical pixel toward the panel.
        int inactiveIconY = selected ? 0 : -1;
        if (icon != null) LegacyTheme.icon(minecraft, icon, x + iconX, y + iconY + inactiveIconY, 16);
        if (label != null) LegacyTheme.drawCenteredString(minecraft, label, x + width / 2,
                y + (height - 8) / 2 + inactiveIconY, selected ? 0xFFFFFFFF : LegacyTheme.TEXT, true);
        GL11.glPopMatrix();
    }
}
