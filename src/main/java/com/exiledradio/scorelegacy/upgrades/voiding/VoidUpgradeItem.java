/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.voiding;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

public class VoidUpgradeItem extends UpgradeItemBase<VoidUpgradeWrapper> {
    public static final UpgradeType<VoidUpgradeWrapper> TYPE = new UpgradeType<>(VoidUpgradeWrapper::new);
    private final IntSupplier filterSlotCount;

    private final java.util.function.BooleanSupplier voidAnythingEnabled;

    public VoidUpgradeItem(String name, CreativeTabs tab, IntSupplier filterSlotCount) {
        this(name, tab, filterSlotCount, () -> true);
    }

    /** {@code voidAnythingEnabled} false leaves only the overflow mode (Storage's config). */
    public VoidUpgradeItem(String name, CreativeTabs tab, IntSupplier filterSlotCount, java.util.function.BooleanSupplier voidAnythingEnabled) {
        super(name, tab);
        this.filterSlotCount = filterSlotCount;
        this.voidAnythingEnabled = voidAnythingEnabled;
    }

    public boolean isVoidAnythingEnabled() {
        return voidAnythingEnabled.getAsBoolean();
    }

    @Override
    public UpgradeType<VoidUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }
}
