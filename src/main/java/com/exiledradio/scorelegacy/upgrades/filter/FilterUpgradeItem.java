/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.filter;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

/** Restricts what hoppers and pipes may put into or take out of the backpack. */
public class FilterUpgradeItem extends UpgradeItemBase<FilterUpgradeWrapper> {
    public static final UpgradeType<FilterUpgradeWrapper> TYPE = new UpgradeType<>(FilterUpgradeWrapper::new);
    private final IntSupplier filterSlotCount;

    public FilterUpgradeItem(String name, CreativeTabs tab, IntSupplier filterSlotCount) {
        super(name, tab);
        this.filterSlotCount = filterSlotCount;
    }

    @Override
    public UpgradeType<FilterUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }
}
