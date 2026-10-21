/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/** A config category for one upgrade. Each storage mod holds its own instances in its own @Config. */
// Repeats CookingUpgradeConfig's fields for the same reason as MagnetUpgradeConfig.
public class AutoCookingUpgradeConfig {
    public AutoCookingUpgradeConfig() {
    }

    @Config.Comment("Smelting speed multiplier (1.0 is the speed of a vanilla furnace)")
    @Config.RangeDouble(min = 0.25, max = 4.0)
    public double cookingSpeedMultiplier = 1.0;

    @Config.Comment("Fuel efficiency multiplier (1.0 burns fuel as fast as a vanilla furnace)")
    @Config.RangeDouble(min = 0.25, max = 4.0)
    public double fuelEfficiencyMultiplier = 1.0;

    @Config.Comment("Number of input filter slots")
    @Config.RangeInt(min = 1, max = 20)
    public int inputFilterSlots = 8;

    @Config.Comment("Number of input filter slots shown in a row")
    @Config.RangeInt(min = 1, max = 6)
    public int inputFilterSlotsInRow = 4;

    @Config.Comment("Number of fuel filter slots")
    @Config.RangeInt(min = 1, max = 20)
    public int fuelFilterSlots = 4;

    @Config.Comment("Number of fuel filter slots shown in a row")
    @Config.RangeInt(min = 1, max = 6)
    public int fuelFilterSlotsInRow = 4;
}
