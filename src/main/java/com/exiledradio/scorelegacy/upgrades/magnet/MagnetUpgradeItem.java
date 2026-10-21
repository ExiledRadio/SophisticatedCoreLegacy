/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.magnet;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

public class MagnetUpgradeItem extends UpgradeItemBase<MagnetUpgradeWrapper> {
    public static final UpgradeType<MagnetUpgradeWrapper> TYPE = new UpgradeType<>(MagnetUpgradeWrapper::new);
    private final IntSupplier radius;
    private final IntSupplier filterSlotCount;

    public MagnetUpgradeItem(String name, CreativeTabs tab, IntSupplier radius, IntSupplier filterSlotCount) {
        super(name, tab);
        this.radius = radius;
        this.filterSlotCount = filterSlotCount;
    }

    @Override
    public UpgradeType<MagnetUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    public int getRadius() {
        return radius.getAsInt();
    }
}
