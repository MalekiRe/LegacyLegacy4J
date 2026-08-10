package wily.legacy125.api;

import net.minecraft.src.ItemStack;

/**
 * A recipe-book category understood by the 1.2.5 crafting bridge.
 * Mods may register more tabs without replacing the Legacy4J screen.
 */
public final class LegacyCraftingTab {
    public interface Matcher {
        boolean matches(ItemStack output);
    }

    public final String id;
    public final String title;
    public final String iconTexture;
    private final Matcher matcher;

    public LegacyCraftingTab(String id, String title, String iconTexture, Matcher matcher) {
        if (id == null || title == null || iconTexture == null || matcher == null) {
            throw new IllegalArgumentException("Crafting tab fields cannot be null");
        }
        this.id = id;
        this.title = title;
        this.iconTexture = iconTexture;
        this.matcher = matcher;
    }

    public boolean matches(ItemStack output) {
        return output != null && matcher.matches(output);
    }
}
