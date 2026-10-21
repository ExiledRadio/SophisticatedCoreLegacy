/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.config.AutoCookingUpgradeConfig;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** A furnace that pulls its input and fuel from the backpack and pushes results back. */
public class AutoSmeltingUpgradeItem extends UpgradeItemBase<AutoSmeltingUpgradeWrapper> {
    @Override
    public com.exiledradio.scorelegacy.api.UpgradeGroup getUpgradeGroup() {
        return com.exiledradio.scorelegacy.api.UpgradeGroup.COOKING_UPGRADES;
    }

    public static final UpgradeType<AutoSmeltingUpgradeWrapper> TYPE = new UpgradeType<>(AutoSmeltingUpgradeWrapper::new);

    private final AutoCookingUpgradeConfig config;

    public AutoSmeltingUpgradeItem(String name, CreativeTabs tab, AutoCookingUpgradeConfig config) {
        super(name, tab);
        this.config = config;
    }

    public AutoCookingUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<AutoSmeltingUpgradeWrapper> getType() {
        return TYPE;
    }
}
