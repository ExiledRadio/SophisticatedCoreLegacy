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
    private final CookingType cookingType;

    public SmeltingUpgradeItem(String name, CreativeTabs tab, CookingUpgradeConfig config) {
        this(name, tab, config, CookingType.SMELTING);
    }

    /** A smoking or blasting upgrade (the original's SmokingUpgradeItem, BlastingUpgradeItem and their auto versions). */
    public SmeltingUpgradeItem(String name, CreativeTabs tab, CookingUpgradeConfig config, CookingType cookingType) {
        super(name, tab);
        this.config = config;
        this.cookingType = cookingType;
    }

    public CookingType getCookingType() {
        return cookingType;
    }

    /** Smoking and blasting only show up when a mod adds a smoker or blast furnace. */
    @Override
    public boolean isAvailable() {
        return cookingType.isAvailable();
    }

    public CookingUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<SmeltingUpgradeWrapper> getType() {
        return TYPE;
    }
}
