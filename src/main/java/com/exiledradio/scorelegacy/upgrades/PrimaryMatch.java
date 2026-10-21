/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.util.INamedEnum;

/** What a filter compares first. TAGS means ore dictionary names in 1.12. */
public enum PrimaryMatch implements INamedEnum {
    ITEM("item"),
    MOD("mod"),
    TAGS("tags");

    private final String name;

    PrimaryMatch(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public PrimaryMatch next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static PrimaryMatch fromName(String name) {
        for (PrimaryMatch value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return ITEM;
    }
}
