/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.stonecutter;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.compat.MaterialCompat;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** A stonecutter in an upgrade tab; listed only when a mod adds a stonecutter with recipes to read. */
public class StonecutterUpgradeItem extends UpgradeItemBase<StonecutterUpgradeWrapper> {
    public static final UpgradeType<StonecutterUpgradeWrapper> TYPE = new UpgradeType<>(StonecutterUpgradeWrapper::new);

    public StonecutterUpgradeItem(String name, CreativeTabs tab) {
        super(name, tab);
    }

    @Override
    public boolean isAvailable() {
        return MaterialCompat.Material.STONECUTTER.isAvailable();
    }

    @Override
    public UpgradeType<StonecutterUpgradeWrapper> getType() {
        return TYPE;
    }
}
