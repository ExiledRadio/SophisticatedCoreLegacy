/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.config;

import net.minecraftforge.common.config.Config;

/** A config category for one upgrade. Each storage mod holds its own instances in its own @Config. */
public class XpPumpUpgradeConfig {
    public XpPumpUpgradeConfig() {
    }

    @Config.Comment("Whether the Experience Pump can repair Mending gear from the tank")
    public boolean mendingOn = true;

    @Config.Comment("Most experience points used per repair (every 5 ticks; a point usually repairs 2 durability)")
    @Config.RangeInt(min = 1, max = 20)
    public int maxXpPointsPerMending = 5;
}
