/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.util;

/** Sort orders. "Tags" sorts by ore dictionary names in 1.12. */
public enum SortBy {
    NAME("name"),
    COUNT("count"),
    TAGS("tags");

    private final String name;

    SortBy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SortBy next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static SortBy fromName(String name) {
        for (SortBy value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return NAME;
    }
}
