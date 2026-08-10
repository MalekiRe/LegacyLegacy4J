package wily.legacy125.api;

/** Immutable, loader-neutral description consumed by the 1.2.5 UI bridge. */
public final class LegacyContainerLayout {
    public enum Style {
        CHEST,
        FURNACE,
        GENERIC
    }

    private final Style style;
    private final String title;
    private final int width;
    private final int height;

    public LegacyContainerLayout(Style style, String title, int width, int height) {
        if (style == null) throw new IllegalArgumentException("style");
        if (width < 32 || height < 32) throw new IllegalArgumentException("Container layout is too small");
        this.style = style;
        this.title = title == null ? "" : title;
        this.width = width;
        this.height = height;
    }

    public Style style() {
        return style;
    }

    public String title() {
        return title;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
