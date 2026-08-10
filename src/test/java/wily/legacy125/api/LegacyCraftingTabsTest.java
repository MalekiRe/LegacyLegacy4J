package wily.legacy125.api;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LegacyCraftingTabsTest {
    @BeforeAll
    static void initializeMinecraftInVanillaOrder() {
        // 1.2.5's static achievements require Block to finish before Item is accessed.
        assertEquals(1, Block.stone.blockID);
    }

    @Test
    void classifiesRepresentativeMinecraft125Recipes() {
        assertCategory("tools", new ItemStack(Item.pickaxeWood));
        assertCategory("armour", new ItemStack(Item.helmetSteel));
        assertCategory("food", new ItemStack(Item.bread));
        assertCategory("transport", new ItemStack(Item.minecartEmpty));
        assertCategory("transport", new ItemStack(Block.rail));
        assertCategory("mechanisms", new ItemStack(Block.pistonBase));
        assertCategory("mechanisms", new ItemStack(Item.redstoneRepeater));
        assertCategory("decoration", new ItemStack(Item.painting));
        assertCategory("decoration", new ItemStack(Block.cloth));
        assertCategory("structures", new ItemStack(Block.chest));
    }

    private static void assertCategory(String id, ItemStack output) {
        assertEquals(id, LegacyCraftingTabs.categoryFor(output).id);
    }
}
