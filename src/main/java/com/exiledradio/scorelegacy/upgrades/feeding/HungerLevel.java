/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.feeding;

import com.exiledradio.scorelegacy.util.INamedEnum;

public enum HungerLevel implements INamedEnum {
    ANY("any"),
    HALF("half"),
    FULL("full");

    private final String name;

    HungerLevel(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public HungerLevel next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static HungerLevel fromName(String name) {
        for (HungerLevel value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return HALF;
    }
}
