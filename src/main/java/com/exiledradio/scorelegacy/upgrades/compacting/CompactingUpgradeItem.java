/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.compacting;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

/** Crafts 2x2 (advanced: also 3x3) compressible items into their compact form as they come in. */
public class CompactingUpgradeItem extends UpgradeItemBase<CompactingUpgradeWrapper> {
    public static final UpgradeType<CompactingUpgradeWrapper> TYPE = new UpgradeType<>(CompactingUpgradeWrapper::new);
    private final boolean shouldCompactThreeByThree;
    private final IntSupplier filterSlotCount;

    public CompactingUpgradeItem(String name, CreativeTabs tab, boolean shouldCompactThreeByThree, IntSupplier filterSlotCount) {
        super(name, tab);
        this.shouldCompactThreeByThree = shouldCompactThreeByThree;
        this.filterSlotCount = filterSlotCount;
    }

    @Override
    public UpgradeType<CompactingUpgradeWrapper> getType() {
        return TYPE;
    }

    public boolean shouldCompactThreeByThree() {
        return shouldCompactThreeByThree;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }
}
