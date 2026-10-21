/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/** A config category for one upgrade. Each storage mod holds its own instances in its own @Config. */
public class CookingUpgradeConfig {
    public CookingUpgradeConfig() {
    }

    @Config.Comment("Smelting speed multiplier (1.0 is the speed of a vanilla furnace)")
    @Config.RangeDouble(min = 0.25, max = 4.0)
    public double cookingSpeedMultiplier = 1.0;

    @Config.Comment("Fuel efficiency multiplier (1.0 burns fuel as fast as a vanilla furnace)")
    @Config.RangeDouble(min = 0.25, max = 4.0)
    public double fuelEfficiencyMultiplier = 1.0;
}
