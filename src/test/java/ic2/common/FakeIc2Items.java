package ic2.common;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;

/** Package-shaped test fixtures for the reflection-free legacy mod owner classifier. */
public final class FakeIc2Items {
    private FakeIc2Items() {
    }

    public static final class Equipment extends NamedItem {
        public Equipment(int id) {
            super(id, "EU-Reader");
        }
    }

    public static final class Component extends NamedItem {
        public Component(int id) {
            super(id, "Copper Ingot");
        }
    }

    public static final class Machine extends ItemBlock {
        public Machine(int blockId) {
            super(blockId - 256);
        }

        @Override
        public String getItemDisplayName(ItemStack stack) {
            return "Macerator";
        }
    }

    public static final class MachineBlock extends Block {
        public MachineBlock(int id) {
            super(id, Material.rock);
        }
    }

    private static class NamedItem extends Item {
        private final String name;

        private NamedItem(int id, String name) {
            super(id);
            this.name = name;
        }

        @Override
        public String getItemDisplayName(ItemStack stack) {
            return name;
        }
    }
}
