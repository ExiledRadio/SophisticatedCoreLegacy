/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.pickup;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

public class PickupUpgradeItem extends UpgradeItemBase<PickupUpgradeWrapper> {
    public static final UpgradeType<PickupUpgradeWrapper> TYPE = new UpgradeType<>(PickupUpgradeWrapper::new);
    private final IntSupplier filterSlotCount;

    public PickupUpgradeItem(String name, CreativeTabs tab, IntSupplier filterSlotCount) {
        super(name, tab);
        this.filterSlotCount = filterSlotCount;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    @Override
    public UpgradeType<PickupUpgradeWrapper> getType() {
        return TYPE;
    }
}
