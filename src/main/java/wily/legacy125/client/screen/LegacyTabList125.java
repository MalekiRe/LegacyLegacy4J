package wily.legacy125.client.screen;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** Two-pass tab compositor corresponding to upstream TabList. */
final class LegacyTabList125 {
    private final List<LegacyTabButton125> tabs = new ArrayList<LegacyTabButton125>();
    private int selectedIndex;

    void add(LegacyTabButton125 tab) {
        tabs.add(tab);
    }

    void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    void renderUnselected(Minecraft minecraft) {
        for (int i = 0; i < tabs.size(); i++) if (i != selectedIndex) tabs.get(i).render(minecraft, false);
    }

    void renderSelected(Minecraft minecraft) {
        if (selectedIndex >= 0 && selectedIndex < tabs.size()) tabs.get(selectedIndex).render(minecraft, true);
    }
}
