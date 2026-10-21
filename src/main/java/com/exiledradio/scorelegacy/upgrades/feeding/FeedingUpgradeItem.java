/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.feeding;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

/** Feeds the carrier (or players next to a placed backpack) from food in the backpack. */
public class FeedingUpgradeItem extends UpgradeItemBase<FeedingUpgradeWrapper> {
    public static final UpgradeType<FeedingUpgradeWrapper> TYPE = new UpgradeType<>(FeedingUpgradeWrapper::new);
    private final IntSupplier filterSlotCount;

    public FeedingUpgradeItem(String name, CreativeTabs tab, IntSupplier filterSlotCount) {
        super(name, tab);
        this.filterSlotCount = filterSlotCount;
    }

    @Override
    public UpgradeType<FeedingUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }
}
