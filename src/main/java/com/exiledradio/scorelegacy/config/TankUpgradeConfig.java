/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/** A config category for one upgrade. Each storage mod holds its own instances in its own @Config. */
public class TankUpgradeConfig {
    public TankUpgradeConfig() {
    }

    @Config.Comment("Capacity in mB per row of backpack slots")
    @Config.RangeInt(min = 500, max = 20000)
    public int capacityPerSlotRow = 4000;

    @Config.Comment("How much stack upgrades raise capacity and transfer rate (0 = not at all, 1 = as much as item stacks)")
    @Config.RangeDouble(min = 0, max = 5)
    public double stackMultiplierRatio = 1.0;

    @Config.Comment("Ticks between filling or draining a bucket's worth to or from the item in a tank slot")
    @Config.RangeInt(min = 1, max = 100)
    public int autoFillDrainContainerCooldown = 20;

    @Config.Comment("Base mB moved per operation; multiplied by rows and stack upgrades like the capacity")
    @Config.RangeInt(min = 1, max = 1000)
    public int maxInputOutput = 20;
}
