/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.crafting;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** A 3x3 crafting grid in an upgrade tab. */
public class CraftingUpgradeItem extends UpgradeItemBase<CraftingUpgradeWrapper> {
    public static final UpgradeType<CraftingUpgradeWrapper> TYPE = new UpgradeType<>(CraftingUpgradeWrapper::new);

    public CraftingUpgradeItem(String name, CreativeTabs tab) {
        super(name, tab);
    }

    @Override
    public UpgradeType<CraftingUpgradeWrapper> getType() {
        return TYPE;
    }
}
