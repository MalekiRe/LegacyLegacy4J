package wily.legacy125;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Method;

/** Reflection bridge for the few protected/private input entry points in 1.2.5. */
final class MinecraftActions {
    private static final Method CLICK_MOUSE = find(Minecraft.class,
            new String[] {"clickMouse", "func_6243_a", "c"}, Integer.TYPE);
    private static final Method GUI_CLICK = find(GuiScreen.class,
            new String[] {"mouseClicked", "func_565_a", "a"}, Integer.TYPE, Integer.TYPE, Integer.TYPE);
    private static final Method GUI_RELEASE = find(GuiScreen.class,
            new String[] {"mouseMovedOrUp", "func_573_b", "b"}, Integer.TYPE, Integer.TYPE, Integer.TYPE);
    private static final Method GUI_KEY = find(GuiScreen.class,
            new String[] {"keyTyped", "func_580_a", "a"}, Character.TYPE, Integer.TYPE);

    private MinecraftActions() {
    }

    static void click(Minecraft minecraft, int button) {
        invoke(CLICK_MOUSE, minecraft, Integer.valueOf(button));
    }

    static void mine(Minecraft minecraft, boolean held) {
        if (minecraft.playerController == null) return;
        MovingObjectPosition hit = minecraft.objectMouseOver;
        if (!held || hit == null || hit.typeOfHit != EnumMovingObjectType.TILE) {
            minecraft.playerController.resetBlockRemoving();
            return;
        }
        minecraft.playerController.onPlayerDamageBlock(hit.blockX, hit.blockY, hit.blockZ, hit.sideHit);
        if (minecraft.thePlayer != null
                && minecraft.thePlayer.canPlayerEdit(hit.blockX, hit.blockY, hit.blockZ)) {
            if (minecraft.effectRenderer != null) {
                minecraft.effectRenderer.addBlockHitEffects(hit.blockX, hit.blockY, hit.blockZ, hit.sideHit);
            }
            minecraft.thePlayer.swingItem();
        }
    }

    static boolean hasBlockTarget(Minecraft minecraft) {
        MovingObjectPosition hit = minecraft.objectMouseOver;
        return hit != null && hit.typeOfHit == EnumMovingObjectType.TILE;
    }

    static void stopUsingItem(Minecraft minecraft) {
        if (minecraft.playerController != null && minecraft.thePlayer != null
                && minecraft.thePlayer.isUsingItem()) {
            minecraft.playerController.onStoppedUsingItem(minecraft.thePlayer);
        }
    }

    /** Immediately performs the entity branch of Minecraft 1.2.5's clickMouse. */
    static boolean attackEntityUnderCrosshair(Minecraft minecraft) {
        MovingObjectPosition hit = minecraft.objectMouseOver;
        if (minecraft.playerController == null || minecraft.thePlayer == null || hit == null
                || hit.typeOfHit != EnumMovingObjectType.ENTITY || hit.entityHit == null) return false;
        minecraft.thePlayer.swingItem();
        minecraft.playerController.attackEntity(minecraft.thePlayer, hit.entityHit);
        return true;
    }

    static String attackTargetDescription(Minecraft minecraft) {
        MovingObjectPosition hit = minecraft.objectMouseOver;
        if (hit == null) return "none";
        if (hit.typeOfHit == EnumMovingObjectType.ENTITY && hit.entityHit != null) {
            return "entity:" + hit.entityHit.getClass().getName() + "#" + hit.entityHit.entityId;
        }
        if (hit.typeOfHit == EnumMovingObjectType.TILE) {
            return "tile:" + hit.blockX + "," + hit.blockY + "," + hit.blockZ;
        }
        return String.valueOf(hit.typeOfHit);
    }

    static void guiClick(GuiScreen screen, int x, int y) {
        invoke(GUI_CLICK, screen, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(0));
    }

    static void guiRelease(GuiScreen screen, int x, int y) {
        invoke(GUI_RELEASE, screen, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(0));
    }

    static void guiBack(GuiScreen screen) {
        invoke(GUI_KEY, screen, Character.valueOf('\0'), Integer.valueOf(Keyboard.KEY_ESCAPE));
    }

    static boolean guiBridgeAvailable() {
        return GUI_CLICK != null && GUI_RELEASE != null && GUI_KEY != null;
    }

    static boolean gameplayBridgeAvailable() {
        return CLICK_MOUSE != null;
    }

    private static Method find(Class<?> owner, String[] names, Class<?>... parameters) {
        for (String name : names) {
            try {
                Method method = owner.getDeclaredMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static void invoke(Method method, Object target, Object... arguments) {
        if (method == null) return;
        try {
            method.invoke(target, arguments);
        } catch (Exception ignored) {
        }
    }
}
