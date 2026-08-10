package wily.legacy125.client.screen;

import net.minecraft.src.CraftingManager;
import net.minecraft.src.Block;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import wily.legacy125.api.LegacyCraftingTabs;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyRecipeGroupTest {
    @BeforeAll
    static void initializeMinecraftInVanillaOrder() {
        assertEquals(1, Block.stone.blockID);
    }

    @Test
    void materialVariantsShareOneRecipeTile() {
        assertEquals(LegacyRecipeGroup.key(new ItemStack(Item.pickaxeWood)),
                LegacyRecipeGroup.key(new ItemStack(Item.pickaxeDiamond)));
        assertEquals(LegacyRecipeGroup.key(new ItemStack(Item.helmetLeather)),
                LegacyRecipeGroup.key(new ItemStack(Item.helmetGold)));
    }

    @Test
    void yeOldeTimesFamiliesGroupWithoutLinkingTheirModClasses() {
        assertEquals(LegacyRecipeGroup.key(named(30000, "Wood Sickle")),
                LegacyRecipeGroup.key(named(30001, "Ruby Sickle")));
        assertEquals(LegacyRecipeGroup.key(named(30002, "Wooden Transport Pipe")),
                LegacyRecipeGroup.key(named(30003, "Cobblestone Transport Pipe")));
        assertEquals(LegacyRecipeGroup.key(named(30004, "Ruby Block")),
                LegacyRecipeGroup.key(named(30005, "Sapphire Block")));
    }

    @Test
    void groupsRedPowerJacketedVariantsByTheirJacketMaterial() {
        assertSameGroup(named(30006, "Snow Jacketed Wire"),
                named(30007, "Snow Jacketed Cable"),
                named(30008, "Snow Jacketed Bluewire"));
        assertSameGroup(named(30009, "Pumpkin Jacketed Wire"),
                named(30010, "Pumpkin Jacketed Cable"),
                named(30011, "Pumpkin Jacketed Bluewire"));
        assertSameGroup(named(30012, "Ruby Block Jacketed Wire"),
                named(30013, "Ruby Block Jacketed Cable"),
                named(30014, "Ruby Block Jacketed Bluewire"));

        assertFalse(LegacyRecipeGroup.key(named(30006, "Snow Jacketed Wire")).equals(
                LegacyRecipeGroup.key(named(30009, "Pumpkin Jacketed Wire"))));
    }

    @Test
    void groupsEe2MkMachinesAndBuildCraftEngines() {
        assertSameGroup(named(30015, "Energy Collector"),
                named(30016, "Collector MK2"), named(30017, "Collector MK3"));
        assertSameGroup(named(30018, "Anti-Matter Relay"),
                named(30019, "Relay MK2"), named(30020, "Relay MK3"));
        assertSameGroup(named(30021, "Redstone Engine"),
                named(30022, "Steam Engine"), named(30023, "Combustion Engine"));

        assertFalse(LegacyRecipeGroup.key(named(30024, "Relay")).equals(
                LegacyRecipeGroup.key(named(30019, "Relay MK2"))));
    }

    @Test
    void readsIc2StyleDirectRecipeImplementations() {
        ForeignRecipe shaped = new ForeignRecipe(new ItemStack(Item.pickaxeSteel),
                new ItemStack[]{new ItemStack(Item.ingotIron), new ItemStack(Item.ingotIron),
                        new ItemStack(Item.stick), new ItemStack(Item.stick)}, 2, false);
        LegacyRecipe read = LegacyRecipe.read(shaped);
        assertNotNull(read);
        assertEquals(2, read.width);
        assertEquals(2, read.height);
        assertFalse(read.shapeless);

        ForeignShapelessRecipe shapeless = new ForeignShapelessRecipe(new ItemStack(Item.dyePowder),
                new ItemStack[]{new ItemStack(Item.coal), new ItemStack(Item.redstone)}, false);
        LegacyRecipe readShapeless = LegacyRecipe.read(shapeless);
        assertNotNull(readShapeless);
        assertTrue(readShapeless.shapeless);
        assertEquals(2, readShapeless.cells.length);

        assertNull(LegacyRecipe.read(new ForeignRecipe(new ItemStack(Item.pickaxeSteel),
                shaped.input, 2, true)));
    }

    @Test
    void alternateRecipesForOneOutputDoNotDuplicateTheDropdownVariant() {
        LegacyRecipe shaped = LegacyRecipe.read(new ForeignRecipe(new ItemStack(Item.pickaxeWood),
                new ItemStack[]{new ItemStack(Block.planks), new ItemStack(Block.planks),
                        new ItemStack(Block.planks), null, new ItemStack(Item.stick), null,
                        null, new ItemStack(Item.stick), null}, 3, false));
        LegacyRecipe alternate = LegacyRecipe.read(new ForeignShapelessRecipe(
                new ItemStack(Item.pickaxeWood),
                new ItemStack[]{new ItemStack(Item.pickaxeWood), new ItemStack(Item.pickaxeWood)}, false));
        LegacyRecipe diamond = LegacyRecipe.read(new ForeignRecipe(new ItemStack(Item.pickaxeDiamond),
                new ItemStack[]{new ItemStack(Item.diamond), new ItemStack(Item.diamond),
                        new ItemStack(Item.diamond), null, new ItemStack(Item.stick), null,
                        null, new ItemStack(Item.stick), null}, 3, false));

        LegacyRecipeGroup group = new LegacyRecipeGroup("tool:pickaxe");
        group.add(shaped);
        group.add(alternate);
        group.add(diamond);

        assertEquals(2, group.recipes.size());
        assertEquals(Item.pickaxeWood.shiftedIndex, group.recipes.get(0).output.itemID);
        assertEquals(Item.pickaxeDiamond.shiftedIndex, group.recipes.get(1).output.itemID);
    }

    @Test
    void everyReadableVanillaRecipeHasATabAndStableGroupKey() {
        int readable = 0;
        Set<String> categories = new HashSet<String>();
        for (Object value : CraftingManager.getInstance().getRecipeList()) {
            if (!(value instanceof IRecipe)) continue;
            LegacyRecipe recipe = LegacyRecipe.read((IRecipe) value);
            if (recipe == null) continue;
            readable++;
            assertNotNull(LegacyCraftingTabs.categoryFor(recipe.output));
            assertTrue(LegacyRecipeGroup.key(recipe.output).length() > 0);
            categories.add(LegacyCraftingTabs.categoryFor(recipe.output).id);
        }
        assertTrue(readable > 100, "expected the real 1.2.5 recipe manager");
        assertEquals(7, categories.size(), "all seven console categories should contain recipes");
    }

    private static ItemStack named(int id, String name) {
        return new ItemStack(new NamedItem(id, name));
    }

    private static void assertSameGroup(ItemStack first, ItemStack... others) {
        String expected = LegacyRecipeGroup.key(first);
        for (ItemStack other : others) assertEquals(expected, LegacyRecipeGroup.key(other));
    }

    private static final class NamedItem extends Item {
        private final String displayName;

        private NamedItem(int id, String displayName) {
            super(id);
            this.displayName = displayName;
        }

        @Override
        public String getItemDisplayName(ItemStack stack) {
            return displayName;
        }
    }

    private static class ForeignRecipe implements IRecipe {
        public final ItemStack output;
        public final ItemStack[] input;
        public final int inputWidth;
        public final boolean hidden;

        private ForeignRecipe(ItemStack output, ItemStack[] input, int inputWidth, boolean hidden) {
            this.output = output;
            this.input = input;
            this.inputWidth = inputWidth;
            this.hidden = hidden;
        }

        public boolean matches(InventoryCrafting crafting) {
            return false;
        }

        public ItemStack getCraftingResult(InventoryCrafting crafting) {
            return output.copy();
        }

        public int getRecipeSize() {
            return input.length;
        }

        public ItemStack getRecipeOutput() {
            return output;
        }
    }

    private static final class ForeignShapelessRecipe extends ForeignRecipe {
        private ForeignShapelessRecipe(ItemStack output, ItemStack[] input, boolean hidden) {
            super(output, input, input.length, hidden);
        }
    }
}
