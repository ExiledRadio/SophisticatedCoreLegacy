/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/** A config category for one upgrade. Each storage mod holds its own instances in its own @Config. */
public class PumpUpgradeConfig {
    public PumpUpgradeConfig() {
    }

    @Config.Comment("Number of fluid filter slots in the Advanced Pump")
    @Config.RangeInt(min = 1, max = 20)
    public int filterSlots = 4;

    @Config.Comment("Base mB moved per operation to or from machines; multiplied by rows and stack upgrades")
    @Config.RangeInt(min = 1, max = 1000)
    public int maxInputOutput = 20;

    @Config.Comment("How much stack upgrades raise the transfer rate (0 = not at all)")
    @Config.RangeDouble(min = 0, max = 5)
    public double stackMultiplierRatio = 1.0;
}
