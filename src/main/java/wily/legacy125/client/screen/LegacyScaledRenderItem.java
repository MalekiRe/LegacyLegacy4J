package wily.legacy125.client.screen;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemBow;
import net.minecraft.src.ItemFishingRod;
import net.minecraft.src.ItemFlintAndSteel;
import net.minecraft.src.ItemHoe;
import net.minecraft.src.ItemShears;
import net.minecraft.src.ItemSword;
import net.minecraft.src.ItemTool;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.RenderItem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** Scales 1.2.5's fixed 16px item renderer to Legacy4J's compact SD slots. */
final class LegacyScaledRenderItem extends RenderItem {
    static final LegacyScaledRenderItem INSTANCE = new LegacyScaledRenderItem();
    private static LegacySlotSizeProvider provider;

    private LegacyScaledRenderItem() {
    }

    static void begin(LegacySlotSizeProvider value) {
        provider = value;
    }

    static void end() {
        provider = null;
    }

    @Override
    public void renderItemIntoGUI(FontRenderer font, RenderEngine textures, ItemStack stack, int x, int y) {
        int size = sizeAt(x, y);
        renderItemInHolder(font, textures, stack, x, y, size);
    }

    static void renderInHolder(FontRenderer font, RenderEngine textures, ItemStack stack,
                               int x, int y, int holderSize) {
        renderInHolder(font, textures, stack, x, y, holderSize, 1.0F, true);
    }

    static void renderInHolder(FontRenderer font, RenderEngine textures, ItemStack stack,
                               int x, int y, int holderSize, float opacity, boolean decorations) {
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        if (opacity < 1.0F) {
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendColor(1.0F, 1.0F, 1.0F, opacity);
            GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        }
        INSTANCE.renderItemInHolder(font, textures, stack, x, y, holderSize);
        if (decorations) INSTANCE.renderOverlayInHolder(font, textures, stack, x, y, holderSize);
        if (opacity < 1.0F) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (!blend) GL11.glDisable(GL11.GL_BLEND);
        }
    }

    private void renderItemInHolder(FontRenderer font, RenderEngine textures, ItemStack stack,
                                    int x, int y, int holderSize) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        float holderScale = holderSize / 18.0F;
        GL11.glScalef(holderScale, holderScale, 1.0F);
        if (usesPadding(stack)) {
            GL11.glTranslatef(1.0F, 1.0F, 0.0F);
            GL11.glScalef(14.0F / 16.0F, 14.0F / 16.0F, 1.0F);
        }
        super.renderItemIntoGUI(font, textures, stack, 0, 0);
        GL11.glPopMatrix();
    }

    @Override
    public void renderItemOverlayIntoGUI(FontRenderer font, RenderEngine textures, ItemStack stack, int x, int y) {
        int size = sizeAt(x, y);
        renderOverlayInHolder(font, textures, stack, x, y, size);
    }

    private void renderOverlayInHolder(FontRenderer font, RenderEngine textures, ItemStack stack,
                                       int x, int y, int holderSize) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        float scale = holderSize / 18.0F;
        GL11.glScalef(scale, scale, 1.0F);
        super.renderItemOverlayIntoGUI(font, textures, stack, 0, 0);
        GL11.glPopMatrix();
    }

    private static boolean usesPadding(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        return !(stack.getItem() instanceof ItemTool)
                && !(stack.getItem() instanceof ItemSword)
                && !(stack.getItem() instanceof ItemHoe)
                && !(stack.getItem() instanceof ItemBow)
                && !(stack.getItem() instanceof ItemShears)
                && !(stack.getItem() instanceof ItemFishingRod)
                && !(stack.getItem() instanceof ItemFlintAndSteel);
    }

    private static int sizeAt(int x, int y) {
        int size = provider == null ? 16 : provider.legacySlotSize(x, y);
        return size > 0 ? size : 16;
    }
}
