/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.util.INamedEnum;

public enum ContentsFilterType implements INamedEnum {
    ALLOW("allow"),
    BLOCK("block"),
    BACKPACK("backpack");

    private final String name;

    ContentsFilterType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public ContentsFilterType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static ContentsFilterType fromName(String name) {
        for (ContentsFilterType value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return BLOCK;
    }
}
