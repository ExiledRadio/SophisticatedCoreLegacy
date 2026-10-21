/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.pump;

import com.exiledradio.scorelegacy.config.PumpUpgradeConfig;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** Moves fluid between the backpack's tanks and the world, machines, or what players hold. */
public class PumpUpgradeItem extends UpgradeItemBase<PumpUpgradeWrapper> {
    public static final UpgradeType<PumpUpgradeWrapper> TYPE = new UpgradeType<>(PumpUpgradeWrapper::new);
    private final boolean interactWithHandDefault;
    private final boolean interactWithWorldDefault;

    private final PumpUpgradeConfig config;

    public PumpUpgradeItem(String name, CreativeTabs tab, boolean interactWithHandDefault, boolean interactWithWorldDefault, PumpUpgradeConfig config) {
        super(name, tab);
        this.config = config;
        this.interactWithHandDefault = interactWithHandDefault;
        this.interactWithWorldDefault = interactWithWorldDefault;
    }

    public PumpUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<PumpUpgradeWrapper> getType() {
        return TYPE;
    }

    public boolean getInteractWithHandDefault() {
        return interactWithHandDefault;
    }

    public boolean getInteractWithWorldDefault() {
        return interactWithWorldDefault;
    }
}
