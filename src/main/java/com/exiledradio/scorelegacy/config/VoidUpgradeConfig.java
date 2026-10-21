/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/**
 * The void upgrade's config: FilteredUpgradeConfig's two values plus whether it may void anything
 * or only overflow. Its own class rather than a subclass: Forge's @Config only takes category
 * classes that extend Object directly.
 */
public class VoidUpgradeConfig {
    @Config.Comment("Number of filter slots")
    @Config.RangeInt(min = 1, max = 20)
    public int filterSlots;

    @Config.Comment("Number of filter slots shown in a row")
    @Config.RangeInt(min = 1, max = 6)
    public int slotsInRow;

    @Config.Comment("Determines whether void upgrade allows voiding anything or it only has overflow option")
    public boolean voidAnythingEnabled = true;

    public VoidUpgradeConfig(int filterSlots, int slotsInRow) {
        this.filterSlots = filterSlots;
        this.slotsInRow = slotsInRow;
    }
}
