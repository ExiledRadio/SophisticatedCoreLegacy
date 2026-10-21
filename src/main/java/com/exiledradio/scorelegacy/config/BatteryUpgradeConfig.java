/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/** A config category for one upgrade. Each storage mod holds its own instances in its own @Config. */
public class BatteryUpgradeConfig {
    public BatteryUpgradeConfig() {
    }

    @Config.Comment("Energy in FE per row of backpack slots")
    @Config.RangeInt(min = 500, max = 50000)
    public int energyPerSlotRow = 10000;

    @Config.Comment("How much stack upgrades raise capacity and transfer rate (0 = not at all, 1 = as much as item stacks)")
    @Config.RangeDouble(min = 0, max = 5)
    public double stackMultiplierRatio = 1.0;

    @Config.Comment("Base FE moved per operation; multiplied by rows and stack upgrades like the capacity")
    @Config.RangeInt(min = 1, max = 1000)
    public int maxInputOutput = 20;
}
