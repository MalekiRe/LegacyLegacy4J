package wily.legacy125.integration;

import net.minecraft.src.Block;
import net.minecraft.src.AnvilSaveHandler;
import net.minecraft.src.ContainerChest;
import net.minecraft.src.ContainerFurnace;
import net.minecraft.src.ContainerWorkbench;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.TileEntityFurnace;
import net.minecraft.src.World;
import net.minecraft.src.WorldSettings;
import net.minecraft.src.WorldType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wily.legacy125.client.LegacyGuiIngame125;
import wily.legacy125.client.screen.LegacyCraftingScreen;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Logical in-game scenarios using Minecraft 1.2.5's real world, blocks and containers. */
final class WorldContainerScenarioTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void placesChestAndMovesAnItemThroughItsContainer() {
        World world = newWorld("chest");
        assertTrue(world.setBlockWithNotify(0, 64, 0, Block.chest.blockID));
        TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(0, 64, 0);
        assertNotNull(chest);
        chest.setInventorySlotContents(0, new ItemStack(Item.coal, 3));

        ScenarioPlayer player = playerAt(world, 0.5D, 65.0D, 0.5D);
        ContainerChest menu = new ContainerChest(player.inventory, chest);
        assertTrue(menu.canInteractWith(player));
        menu.slotClick(0, 0, false, player);

        assertNull(chest.getStackInSlot(0));
        assertNotNull(player.inventory.getItemStack());
        assertEquals(Item.coal.shiftedIndex, player.inventory.getItemStack().itemID);
        assertEquals(3, player.inventory.getItemStack().stackSize);
    }

    @Test
    void chestSupportsControllerSecondaryAndQuickMoveActions() {
        World world = newWorld("chest-actions");
        assertTrue(world.setBlockWithNotify(0, 64, 0, Block.chest.blockID));
        TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(0, 64, 0);
        ScenarioPlayer player = playerAt(world, 0.5D, 65.0D, 0.5D);
        ContainerChest secondary = new ContainerChest(player.inventory, chest);
        chest.setInventorySlotContents(0, new ItemStack(Item.coal, 4));

        secondary.slotClick(0, 1, false, player);

        assertNotNull(player.inventory.getItemStack());
        assertEquals(2, player.inventory.getItemStack().stackSize);
        assertEquals(2, chest.getStackInSlot(0).stackSize);

        player.inventory.setItemStack(null);
        chest.setInventorySlotContents(0, new ItemStack(Item.ingotIron, 3));
        ContainerChest quickMove = new ContainerChest(player.inventory, chest);
        quickMove.slotClick(0, 0, true, player);

        assertNull(chest.getStackInSlot(0));
        assertTrue(inventoryContains(player, Item.ingotIron.shiftedIndex, 3));
    }

    @Test
    void placesFurnaceSmeltsIronAndTakesOutputThroughItsContainer() {
        World world = newWorld("furnace");
        assertTrue(world.setBlockWithNotify(0, 64, 0, Block.stoneOvenIdle.blockID));
        TileEntityFurnace furnace = (TileEntityFurnace) world.getBlockTileEntity(0, 64, 0);
        assertNotNull(furnace);
        furnace.setInventorySlotContents(0, new ItemStack(Block.oreIron));
        furnace.setInventorySlotContents(1, new ItemStack(Item.coal));

        for (int tick = 0; tick < 201; tick++) furnace.updateEntity();

        ItemStack output = furnace.getStackInSlot(2);
        assertNotNull(output);
        assertEquals(Item.ingotIron.shiftedIndex, output.itemID);
        ScenarioPlayer player = playerAt(world, 0.5D, 65.0D, 0.5D);
        ContainerFurnace menu = new ContainerFurnace(player.inventory, furnace);
        assertTrue(menu.canInteractWith(player));
        menu.slotClick(2, 0, false, player);

        assertNull(furnace.getStackInSlot(2));
        assertNotNull(player.inventory.getItemStack());
        assertEquals(Item.ingotIron.shiftedIndex, player.inventory.getItemStack().itemID);
    }

    @Test
    void placesCraftingTableCraftsSticksAndTakesResult() {
        World world = newWorld("crafting");
        assertTrue(world.setBlockWithNotify(0, 64, 0, Block.workbench.blockID));
        ScenarioPlayer player = playerAt(world, 0.5D, 65.0D, 0.5D);
        ContainerWorkbench menu = new ContainerWorkbench(player.inventory, world, 0, 64, 0);
        assertTrue(menu.canInteractWith(player));
        menu.craftMatrix.setInventorySlotContents(0, new ItemStack(Block.planks));
        menu.craftMatrix.setInventorySlotContents(3, new ItemStack(Block.planks));

        ItemStack result = menu.craftResult.getStackInSlot(0);
        assertNotNull(result);
        assertEquals(Item.stick.shiftedIndex, result.itemID);
        assertEquals(4, result.stackSize);
        menu.slotClick(0, 0, false, player);

        assertNotNull(player.inventory.getItemStack());
        assertEquals(Item.stick.shiftedIndex, player.inventory.getItemStack().itemID);
        assertEquals(4, player.inventory.getItemStack().stackSize);
    }

    @Test
    void suppressesGameplayHudForPlayerCraftingButNotCraftingTables() {
        World world = newWorld("crafting-hud-scope");
        ScenarioPlayer player = playerAt(world, 0.5D, 65.0D, 0.5D);
        LegacyCraftingScreen playerCrafting = new LegacyCraftingScreen(player.inventorySlots);
        LegacyCraftingScreen tableCrafting = new LegacyCraftingScreen(
                new ContainerWorkbench(player.inventory, world, 0, 64, 0));

        assertTrue(LegacyGuiIngame125.suppressesGameplayHud(playerCrafting));
        assertFalse(LegacyGuiIngame125.suppressesGameplayHud(tableCrafting));
    }

    private World newWorld(String name) {
        AnvilSaveHandler saves = new AnvilSaveHandler(temporaryDirectory.toFile(), name, false);
        WorldSettings settings = new WorldSettings(125L, 0, false, false, WorldType.FLAT);
        return new ScenarioWorld(saves, name, settings);
    }

    private static ScenarioPlayer playerAt(World world, double x, double y, double z) {
        ScenarioPlayer player = new ScenarioPlayer(world);
        player.setPosition(x, y, z);
        return player;
    }

    private static boolean inventoryContains(ScenarioPlayer player, int itemId, int count) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.itemID == itemId && stack.stackSize == count) return true;
        }
        return false;
    }

    private static final class ScenarioPlayer extends EntityPlayer {
        private ScenarioPlayer(World world) {
            super(world);
            username = "ScenarioPlayer";
        }

        @Override
        public void func_6420_o() {
        }
    }

    /** Avoids unrelated terrain population and ModLoader's removed Java Applet bootstrap. */
    private static final class ScenarioWorld extends World {
        private ScenarioWorld(ISaveHandler saves, String name, WorldSettings settings) {
            super(saves, name, settings);
        }

        @Override
        protected void generateSpawnPoint() {
            getWorldInfo().setSpawnPosition(0, 64, 0);
        }
    }
}
