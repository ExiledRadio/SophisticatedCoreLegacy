/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.xppump;

import com.exiledradio.scorelegacy.config.XpPumpUpgradeConfig;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** Stores the player's experience in a tank as liquid experience and gives it back. */
public class XpPumpUpgradeItem extends UpgradeItemBase<XpPumpUpgradeWrapper> {
    public static final UpgradeType<XpPumpUpgradeWrapper> TYPE = new UpgradeType<>(XpPumpUpgradeWrapper::new);

    private final XpPumpUpgradeConfig config;

    public XpPumpUpgradeItem(String name, CreativeTabs tab, XpPumpUpgradeConfig config) {
        super(name, tab);
        this.config = config;
    }

    public XpPumpUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<XpPumpUpgradeWrapper> getType() {
        return TYPE;
    }
}
