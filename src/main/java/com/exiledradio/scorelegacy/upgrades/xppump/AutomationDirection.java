/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.xppump;

import com.exiledradio.scorelegacy.util.INamedEnum;

public enum AutomationDirection implements INamedEnum {
    INPUT("input"),
    OUTPUT("output"),
    OFF("off");

    private final String name;

    AutomationDirection(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public AutomationDirection next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static AutomationDirection fromName(String name) {
        for (AutomationDirection value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return INPUT;
    }
}
