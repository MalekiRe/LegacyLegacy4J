package wily.legacy125.api;

import net.minecraft.src.GuiChest;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiFurnace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Public registry used to give modded containers an exact Legacy presentation. */
public final class LegacyContainers {
    private static final List<LegacyContainerAdapter> ADAPTERS = new ArrayList<LegacyContainerAdapter>();

    static {
        register(new LegacyContainerAdapter() {
            public boolean supports(GuiContainer screen) {
                return screen instanceof GuiChest;
            }

            public LegacyContainerLayout describe(GuiContainer screen) {
                int rows = Math.max(1, (screen.inventorySlots.inventorySlots.size() - 36) / 9);
                return new LegacyContainerLayout(LegacyContainerLayout.Style.CHEST, "Chest", 130,
                        128 + (rows - 3) * 13);
            }
        });
        register(new LegacyContainerAdapter() {
            public boolean supports(GuiContainer screen) {
                return screen instanceof GuiFurnace;
            }

            public LegacyContainerLayout describe(GuiContainer screen) {
                return new LegacyContainerLayout(LegacyContainerLayout.Style.FURNACE, "Furnace", 130, 145);
            }
        });
    }

    private LegacyContainers() {
    }

    /**
     * Registers ahead of built-ins, allowing a mod to specialize a GuiChest or
     * GuiFurnace subclass without replacing its container implementation.
     */
    public static synchronized void registerFirst(LegacyContainerAdapter adapter) {
        if (adapter == null) throw new IllegalArgumentException("adapter");
        ADAPTERS.add(0, adapter);
    }

    public static synchronized void register(LegacyContainerAdapter adapter) {
        if (adapter == null) throw new IllegalArgumentException("adapter");
        ADAPTERS.add(adapter);
    }

    public static synchronized LegacyContainerLayout describe(GuiContainer screen) {
        for (LegacyContainerAdapter adapter : ADAPTERS) {
            if (adapter.supports(screen)) return adapter.describe(screen);
        }
        return null;
    }

    public static synchronized List<LegacyContainerAdapter> adapters() {
        return Collections.unmodifiableList(new ArrayList<LegacyContainerAdapter>(ADAPTERS));
    }
}
