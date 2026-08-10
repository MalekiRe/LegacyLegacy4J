package wily.legacy125;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiIngameMenu;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import net.minecraft.src.ScaledResolution;
import org.lwjgl.input.Mouse;
import wily.legacy125.client.screen.LegacyControllerScreen;
import wily.legacy125.client.screen.LegacyCraftingScreen;
import wily.legacy125.client.screen.LegacyInputShieldScreen;
import wily.legacy125.client.screen.LegacyScreenRouter;
import wily.legacy125.client.screen.SlotNavigator;
import wily.legacy125.input.ControllerFrame;
import wily.legacy125.input.ControllerInput;
import wily.legacy125.input.ControllerDebugLog125;
import wily.legacy125.input.LwjglControllerInput;
import wily.legacy125.input.LegacyGameplayBindings125;
import wily.legacy125.input.LegacyMovementInput125;
import wily.legacy125.input.MenuButtonLatch125;
import wily.legacy125.input.MenuOpeningGate125;
import wily.legacy125.input.MiningCadence125;
import wily.legacy125.input.PadButton;
import wily.legacy125.input.RepeatCooldown125;

import java.io.File;
import java.util.EnumSet;

public final class LegacyController {
    private final LegacyConfig config;
    private final ControllerInput input;
    private ControllerFrame previous = ControllerFrame.DISCONNECTED;
    private ControllerFrame current = ControllerFrame.DISCONNECTED;
    private GuiScreen cursorScreen;
    private float cursorX;
    private float cursorY;
    private final RepeatCooldown125 useCooldown = new RepeatCooldown125(200L);
    private PadButton navigationDirection;
    private int navigationRepeat;
    private int lastHotbarSlot = -1;
    private int hotbarNameTicks;
    private long lastSaveModified = -1L;
    private int saveCheckTicks;
    private int autosaveNoticeTicks;
    private GuiContainer slotScreen;
    private int focusedSlot = -1;
    private LegacyMovementInput125 movementInput;
    private final MenuButtonLatch125 menuButtonLatch = new MenuButtonLatch125();
    private final MenuOpeningGate125 menuOpeningGate = new MenuOpeningGate125();
    private final MiningCadence125 miningCadence = new MiningCadence125();

    public LegacyController(LegacyConfig config) {
        this(config, new LwjglControllerInput(config));
    }

    public LegacyController(LegacyConfig config, ControllerInput input) {
        this.config = config;
        this.input = input;
    }

    public void tickGame(Minecraft minecraft) {
        // ModLoader invokes both hooks when an in-world GUI is open. Leave polling and rendering
        // to tickGui in that case so button edges are consumed exactly once per frame.
        if (minecraft.currentScreen != null) {
            // The shield is not a menu: tickGui runs the full gameplay controller path for it.
            // Do not clear its retained analog/jump frame from the parallel in-game hook.
            if (minecraft.currentScreen instanceof LegacyInputShieldScreen) return;
            releaseGameplay(minecraft);
            return;
        }
        poll();
        if (!current.connected || minecraft.thePlayer == null || minecraft.theWorld == null) {
            releaseGameplay(minecraft);
            return;
        }

        GameSettings settings = minecraft.gameSettings;
        updateMovementInput(minecraft);

        minecraft.thePlayer.rotationYaw += current.rightX * config.lookSensitivity;
        float pitch = current.rightY * config.lookSensitivity * (config.invertLookY ? -1.0F : 1.0F);
        minecraft.thePlayer.rotationPitch = clamp(minecraft.thePlayer.rotationPitch + pitch, -90.0F, 90.0F);

        EnumSet<LegacyGameplayBindings125.Action> actions =
                LegacyGameplayBindings125.pressed(current, previous);
        if (actions.contains(LegacyGameplayBindings125.Action.HOTBAR_LEFT)) changeSlot(minecraft, -1);
        if (actions.contains(LegacyGameplayBindings125.Action.HOTBAR_RIGHT)) changeSlot(minecraft, 1);
        if (actions.contains(LegacyGameplayBindings125.Action.DROP)) minecraft.thePlayer.dropOneItem();
        if (actions.contains(LegacyGameplayBindings125.Action.TOGGLE_PERSPECTIVE)) {
            settings.thirdPersonView = (settings.thirdPersonView + 1) % 3;
            settings.saveOptions();
        }
        // Minecraft 1.2.5 predates integrated-server host options. Opening the
        // game menu is the closest safe equivalent for Legacy4J's Back binding.
        boolean suppressMenuOpen = menuButtonLatch.suppressMenuOpen(current);
        if (!suppressMenuOpen && (actions.contains(LegacyGameplayBindings125.Action.HOST_OPTIONS)
                || actions.contains(LegacyGameplayBindings125.Action.PAUSE))) {
            menuOpeningGate.onControllerRequest();
            ControllerDebugLog125.log("opening pause from controller actions=" + actions
                    + " current=" + current.buttons() + " previous=" + previous.buttons());
            minecraft.displayInGameMenu();
        }
        if (actions.contains(LegacyGameplayBindings125.Action.CRAFTING)) {
            minecraft.displayGuiScreen(new LegacyCraftingScreen(minecraft.thePlayer.inventorySlots));
        }
        if (actions.contains(LegacyGameplayBindings125.Action.INVENTORY)) {
            minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));
        }

        boolean attacking = LegacyGameplayBindings125.attacking(current);
        if (current.pressed(PadButton.RIGHT_TRIGGER, previous)) MinecraftActions.click(minecraft, 0);
        boolean instantMine = minecraft.playerController != null
                && minecraft.playerController.isInCreativeMode();
        if (miningCadence.shouldDamage(attacking, instantMine, System.currentTimeMillis())) {
            MinecraftActions.mine(minecraft, true);
        }
        if (current.released(PadButton.RIGHT_TRIGGER, previous)) MinecraftActions.mine(minecraft, false);

        if (useCooldown.shouldFire(LegacyGameplayBindings125.using(current),
                System.currentTimeMillis())) {
            MinecraftActions.click(minecraft, 1);
        }

        int selectedSlot = minecraft.thePlayer.inventory.currentItem;
        if (selectedSlot != lastHotbarSlot) {
            lastHotbarSlot = selectedSlot;
            hotbarNameTicks = 50;
        } else if (hotbarNameTicks > 0) {
            hotbarNameTicks--;
        }
        ItemStack selected = minecraft.thePlayer.inventory.getCurrentItem();
        String selectedName = selected == null ? "" : selected.getItem().getItemDisplayName(selected);
        updateAutosave(minecraft);
        LegacyHud.renderGameplay(minecraft,
                config.showTooltips && hotbarNameTicks > 0 ? selectedName : "",
                minecraft.thePlayer.capabilities.isFlying, autosaveNoticeTicks > 0);
    }

    public void tickGui(Minecraft minecraft, GuiScreen screen) {
        if (screen == null) return;
        // ModLoader 1.2.5 can finish a GUI hook with the screen that was current at the
        // beginning of the tick. Resume Game closes that screen during controller dispatch;
        // never route the stale vanilla pause instance back into the live screen afterward.
        if (screen != minecraft.currentScreen) {
            ControllerDebugLog125.log("ignoring stale GUI hook screen=" + screen.getClass().getName()
                    + " current=" + (minecraft.currentScreen == null ? "<none>"
                    : minecraft.currentScreen.getClass().getName()));
            return;
        }
        if (screen instanceof LegacyInputShieldScreen) {
            // GuiScreen presence makes vanilla send all keyboard events to the shield,
            // preventing repeated synthetic Escape from constructing GuiIngameMenu.
            // Temporarily expose gameplay state only while executing our controller path.
            minecraft.currentScreen = null;
            if (!minecraft.inGameHasFocus) minecraft.setIngameFocus();
            tickGame(minecraft);
            if (minecraft.currentScreen == null && current.connected) {
                minecraft.currentScreen = screen;
            }
            return;
        }
        if (screen instanceof GuiIngameMenu
                && !menuOpeningGate.allowVanillaPauseScreen(current.connected)) {
            ControllerDebugLog125.log("shielding synthetic Escape pause while controller is connected");
            minecraft.displayGuiScreen(new LegacyInputShieldScreen());
            return;
        }
        menuButtonLatch.onGuiTick();
        GuiScreen replacement = LegacyScreenRouter.replacement(screen);
        if (replacement != screen) {
            ControllerDebugLog125.log("routing GUI " + screen.getClass().getName() + " -> "
                    + replacement.getClass().getName() + " controller=" + current.buttons());
            // Reuse live player/workbench containers without closing their server window first.
            if (screen instanceof GuiContainer && replacement instanceof GuiContainer) {
                minecraft.currentScreen = replacement;
                replacement.setWorldAndResolution(minecraft, screen.width, screen.height);
            } else {
                minecraft.displayGuiScreen(replacement);
            }
            screen = replacement;
        }
        poll();
        releaseGameplay(minecraft);
        if (!current.connected) return;

        if (screen instanceof LegacyControllerScreen) {
            dispatchFocusedScreen((LegacyControllerScreen) screen);
            return;
        }

        if (screen instanceof GuiContainer) {
            dispatchGenericContainer(minecraft, (GuiContainer) screen);
            return;
        }

        ScaledResolution scaled = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth,
                minecraft.displayHeight);
        if (screen != cursorScreen) {
            cursorScreen = screen;
            cursorX = scaled.getScaledWidth() / 2.0F;
            cursorY = scaled.getScaledHeight() / 2.0F;
        }
        float dx = current.leftX * config.menuCursorSpeed;
        float dy = current.leftY * config.menuCursorSpeed;
        if (pressed(PadButton.DPAD_LEFT)) dx -= 28.0F;
        if (pressed(PadButton.DPAD_RIGHT)) dx += 28.0F;
        if (pressed(PadButton.DPAD_UP)) dy -= 20.0F;
        if (pressed(PadButton.DPAD_DOWN)) dy += 20.0F;
        cursorX = clamp(cursorX + dx, 0.0F, scaled.getScaledWidth() - 1.0F);
        cursorY = clamp(cursorY + dy, 0.0F, scaled.getScaledHeight() - 1.0F);
        moveSystemCursor(minecraft, scaled);

        if (pressed(PadButton.A)) MinecraftActions.guiClick(screen, Math.round(cursorX), Math.round(cursorY));
        if (current.released(PadButton.A, previous)) {
            MinecraftActions.guiRelease(screen, Math.round(cursorX), Math.round(cursorY));
        }
        if (pressed(PadButton.B) || pressed(PadButton.BACK) || pressed(PadButton.START)) {
            MinecraftActions.guiBack(screen);
        }

        if (config.showTooltips) LegacyHud.renderMenu(minecraft, cursorX, cursorY);
    }

    private void dispatchFocusedScreen(LegacyControllerScreen screen) {
        PadButton[] edgeButtons = {
                PadButton.A, PadButton.B, PadButton.X, PadButton.Y, PadButton.BACK,
                PadButton.LEFT_BUMPER, PadButton.RIGHT_BUMPER,
                PadButton.LEFT_TRIGGER, PadButton.RIGHT_TRIGGER,
                PadButton.LEFT_STICK, PadButton.RIGHT_STICK,
                PadButton.DPAD_UP, PadButton.DPAD_DOWN, PadButton.DPAD_LEFT, PadButton.DPAD_RIGHT,
                PadButton.START
        };
        for (PadButton button : edgeButtons) {
            if (pressed(button)) screen.onControllerButton(button);
        }

        PadButton stickDirection = stickNavigationDirection();
        if (stickDirection == null) {
            navigationDirection = null;
            navigationRepeat = 0;
        } else if (stickDirection != navigationDirection) {
            navigationDirection = stickDirection;
            navigationRepeat = 12;
            screen.onControllerButton(stickDirection);
        } else if (--navigationRepeat <= 0) {
            navigationRepeat = 5;
            screen.onControllerButton(stickDirection);
        }
    }

    /**
     * Capability fallback for arbitrary mod inventories. It never replaces the
     * foreign screen: the original renderer, fields, progress bars and container
     * stay authoritative while controller actions operate on its live slots.
     */
    private void dispatchGenericContainer(Minecraft minecraft, GuiContainer screen) {
        if (slotScreen != screen) {
            slotScreen = screen;
            focusedSlot = SlotNavigator.firstVisible(screen.inventorySlots.inventorySlots);
        }
        if (pressed(PadButton.B) || pressed(PadButton.BACK) || pressed(PadButton.START)) {
            MinecraftActions.guiBack(screen);
            return;
        }
        if (pressed(PadButton.A)) clickFocusedSlot(minecraft, screen, false);
        if (pressed(PadButton.X)) clickFocusedSlot(minecraft, screen, 1, false);
        if (pressed(PadButton.Y)) clickFocusedSlot(minecraft, screen, 0, true);
        PadButton direction = edgeDirection();
        PadButton stickDirection = stickNavigationDirection();
        if (direction != null) {
            focusedSlot = SlotNavigator.neighbor(screen.inventorySlots.inventorySlots, focusedSlot, direction);
            navigationDirection = null;
            navigationRepeat = 0;
        } else if (stickDirection == null) {
            navigationDirection = null;
            navigationRepeat = 0;
        } else if (stickDirection != navigationDirection) {
            navigationDirection = stickDirection;
            navigationRepeat = 12;
            focusedSlot = SlotNavigator.neighbor(screen.inventorySlots.inventorySlots, focusedSlot, stickDirection);
        } else if (--navigationRepeat <= 0) {
            navigationRepeat = 5;
            focusedSlot = SlotNavigator.neighbor(screen.inventorySlots.inventorySlots, focusedSlot, stickDirection);
        }
        if (config.showTooltips && focusedSlot >= 0 && focusedSlot < screen.inventorySlots.inventorySlots.size()) {
            Slot slot = (Slot) screen.inventorySlots.inventorySlots.get(focusedSlot);
            int left = (screen.width - containerSize(screen, "xSize", 176)) / 2;
            int top = (screen.height - containerSize(screen, "ySize", 166)) / 2;
            LegacyHud.renderContainerFocus(minecraft, left + slot.xDisplayPosition,
                    top + slot.yDisplayPosition);
        }
    }

    private PadButton edgeDirection() {
        if (pressed(PadButton.DPAD_LEFT)) return PadButton.DPAD_LEFT;
        if (pressed(PadButton.DPAD_RIGHT)) return PadButton.DPAD_RIGHT;
        if (pressed(PadButton.DPAD_UP)) return PadButton.DPAD_UP;
        if (pressed(PadButton.DPAD_DOWN)) return PadButton.DPAD_DOWN;
        return null;
    }

    private void clickFocusedSlot(Minecraft minecraft, GuiContainer screen, boolean quickMove) {
        clickFocusedSlot(minecraft, screen, 0, quickMove);
    }

    private void clickFocusedSlot(Minecraft minecraft, GuiContainer screen, int button, boolean quickMove) {
        if (focusedSlot < 0 || focusedSlot >= screen.inventorySlots.inventorySlots.size()) return;
        Slot slot = (Slot) screen.inventorySlots.inventorySlots.get(focusedSlot);
        minecraft.playerController.windowClick(screen.inventorySlots.windowId, slot.slotNumber, button,
                quickMove, minecraft.thePlayer);
    }

    private int containerSize(GuiContainer screen, String name, int fallback) {
        try {
            java.lang.reflect.Field field = GuiContainer.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.getInt(screen);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private PadButton stickNavigationDirection() {
        if (Math.abs(current.leftX) < 0.65F && Math.abs(current.leftY) < 0.65F) return null;
        if (Math.abs(current.leftX) > Math.abs(current.leftY)) {
            return current.leftX < 0.0F ? PadButton.DPAD_LEFT : PadButton.DPAD_RIGHT;
        }
        return current.leftY < 0.0F ? PadButton.DPAD_UP : PadButton.DPAD_DOWN;
    }

    private void poll() {
        previous = current;
        current = input.poll();
    }

    private boolean pressed(PadButton button) {
        return current.pressed(button, previous);
    }

    private void changeSlot(Minecraft minecraft, int amount) {
        int slot = minecraft.thePlayer.inventory.currentItem + amount;
        if (slot < 0) slot = 8;
        if (slot > 8) slot = 0;
        minecraft.thePlayer.inventory.currentItem = slot;
    }

    private void updateAutosave(Minecraft minecraft) {
        if (autosaveNoticeTicks > 0) autosaveNoticeTicks--;
        if (++saveCheckTicks < 20) return;
        saveCheckTicks = 0;
        try {
            String directory = minecraft.theWorld.getSaveHandler().getSaveDirectoryName();
            if (directory == null || directory.length() == 0) return;
            File level = new File(new File(new File(minecraft.mcDataDir, "saves"), directory), "level.dat");
            long modified = level.lastModified();
            if (lastSaveModified > 0L && modified > lastSaveModified) autosaveNoticeTicks = 50;
            if (modified > 0L) lastSaveModified = modified;
        } catch (RuntimeException ignored) {
            // Remote worlds and alternate save handlers do not necessarily expose a local level.dat.
        }
    }

    private void moveSystemCursor(Minecraft minecraft, ScaledResolution scaled) {
        int displayX = Math.round(cursorX * minecraft.displayWidth / scaled.getScaledWidth());
        int displayY = minecraft.displayHeight - Math.round(cursorY * minecraft.displayHeight / scaled.getScaledHeight()) - 1;
        try {
            Mouse.setCursorPosition(displayX, displayY);
        } catch (RuntimeException ignored) {
        }
    }

    private void releaseGameplay(Minecraft minecraft) {
        if (movementInput != null) movementInput.updateController(ControllerFrame.DISCONNECTED, false);
        MinecraftActions.mine(minecraft, false);
    }

    private void updateMovementInput(Minecraft minecraft) {
        if (minecraft.thePlayer.movementInput instanceof LegacyMovementInput125) {
            movementInput = (LegacyMovementInput125) minecraft.thePlayer.movementInput;
        } else {
            movementInput = new LegacyMovementInput125(minecraft.thePlayer.movementInput);
            minecraft.thePlayer.movementInput = movementInput;
        }
        movementInput.updateController(current, minecraft.thePlayer.capabilities.isFlying);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
