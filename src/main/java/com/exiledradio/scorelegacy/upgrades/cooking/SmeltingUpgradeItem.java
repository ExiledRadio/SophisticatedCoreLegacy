/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.config.CookingUpgradeConfig;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** A furnace in an upgrade tab. */
public class SmeltingUpgradeItem extends UpgradeItemBase<SmeltingUpgradeWrapper> {
    @Override
    public com.exiledradio.scorelegacy.api.UpgradeGroup getUpgradeGroup() {
        return com.exiledradio.scorelegacy.api.UpgradeGroup.COOKING_UPGRADES;
    }

    public static final UpgradeType<SmeltingUpgradeWrapper> TYPE = new UpgradeType<>(SmeltingUpgradeWrapper::new);

    private final CookingUpgradeConfig config;

    public SmeltingUpgradeItem(String name, CreativeTabs tab, CookingUpgradeConfig config) {
        super(name, tab);
        this.config = config;
    }

    public CookingUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<SmeltingUpgradeWrapper> getType() {
        return TYPE;
    }
}
