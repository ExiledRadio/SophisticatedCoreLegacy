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
    private final CookingType cookingType;

    public AutoSmeltingUpgradeItem(String name, CreativeTabs tab, AutoCookingUpgradeConfig config) {
        this(name, tab, config, CookingType.SMELTING);
    }

    /** A smoking or blasting upgrade (the original's SmokingUpgradeItem, BlastingUpgradeItem and their auto versions). */
    public AutoSmeltingUpgradeItem(String name, CreativeTabs tab, AutoCookingUpgradeConfig config, CookingType cookingType) {
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

    public AutoCookingUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<AutoSmeltingUpgradeWrapper> getType() {
        return TYPE;
    }
}
