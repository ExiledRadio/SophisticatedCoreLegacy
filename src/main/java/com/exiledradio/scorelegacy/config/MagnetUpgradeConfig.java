/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/** A config category for one upgrade. Each storage mod holds its own instances in its own @Config. */
// Forge's @Config only accepts categories that extend Object directly, so this repeats the
// filter fields rather than extending FilteredUpgradeConfig.
public class MagnetUpgradeConfig {
    @Config.Comment("Number of filter slots")
    @Config.RangeInt(min = 1, max = 20)
    public int filterSlots;

    @Config.Comment("Number of filter slots shown in a row")
    @Config.RangeInt(min = 1, max = 6)
    public int slotsInRow;

    @Config.Comment("Range around the backpack in blocks at which the magnet picks up items")
    @Config.RangeInt(min = 1, max = 20)
    public int magnetRange;

    public MagnetUpgradeConfig(int filterSlots, int slotsInRow, int magnetRange) {
        this.filterSlots = filterSlots;
        this.slotsInRow = slotsInRow;
        this.magnetRange = magnetRange;
    }
}
