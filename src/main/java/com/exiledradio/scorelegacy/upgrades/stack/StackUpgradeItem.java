/*
 * Modified from Sophisticated Backpacks / Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.stack;

import com.exiledradio.scorelegacy.api.IStackableContentsUpgrade;
import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Multiplies how much fits in each storage slot. Several multiply together. A multiplier below 1
 * is a stack downgrade (the original 1.20's: 1/8, 1/16 and 1/32 of a normal stack, at least one
 * item), and Integer.MAX_VALUE is the omega tier.
 */
public class StackUpgradeItem extends UpgradeItemBase<StackUpgradeItem.Wrapper> {
    public static final UpgradeType<Wrapper> TYPE = new UpgradeType<>(Wrapper::new);

    @Override
    public com.exiledradio.scorelegacy.api.UpgradeGroup getUpgradeGroup() {
        return com.exiledradio.scorelegacy.api.UpgradeGroup.STACK_UPGRADES;
    }
    private final double stackSizeMultiplier;

    public StackUpgradeItem(String name, CreativeTabs tab, double stackSizeMultiplier) {
        super(name, tab);
        this.stackSizeMultiplier = stackSizeMultiplier;
    }

    /** The slot limit with every stack upgrade (normal stacks are 64). */
    public static int getInventorySlotLimit(UpgradeHandler upgradeHandler) {
        double multiplier = 1;
        for (Wrapper stackWrapper : upgradeHandler.getTypeWrappers(TYPE)) {
            if (Integer.MAX_VALUE / stackWrapper.getStackSizeMultiplier() < multiplier) {
                return Integer.MAX_VALUE;
            }
            multiplier *= stackWrapper.getStackSizeMultiplier();
        }
        return Integer.MAX_VALUE / 64D < multiplier ? Integer.MAX_VALUE : Math.max(1, (int) (multiplier * 64));
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    double getStackSizeMultiplier() {
        return stackSizeMultiplier;
    }

    /** The storage's multiplier now (stack upgrades and its own base, e.g. a limited barrel's). */
    private static double getCurrentMultiplier(IStorageWrapper storageWrapper) {
        return storageWrapper.getInventoryHandler().getBaseSlotLimit() / 64D;
    }

    /** A downgrade can only go in while everything still fits under it (the original's canAddUpgradeTo). */
    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(com.exiledradio.scorelegacy.api.IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelStorage) {
        if (stackSizeMultiplier < 1) {
            UpgradeSlotChangeResult result = isMultiplierHighEnough(storageWrapper, getCurrentMultiplier(storageWrapper) * stackSizeMultiplier);
            if (!result.isSuccessful()) {
                return result;
            }
        }
        return super.canAddUpgradeTo(storageWrapper, upgradeStack, firstLevelStorage);
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper) {
        if (stackSizeMultiplier <= 1) {
            // Taking a downgrade out only raises the limit.
            return UpgradeSlotChangeResult.SUCCESS;
        }
        return isMultiplierHighEnough(storageWrapper, getCurrentMultiplier(storageWrapper) / stackSizeMultiplier);
    }

    @Override
    public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, IStorageWrapper storageWrapper) {
        UpgradeSlotChangeResult limits = com.exiledradio.scorelegacy.api.UpgradeLimits.checkSwap(this, upgradeStackToPut, storageWrapper);
        if (!limits.isSuccessful()) {
            return limits;
        }
        if (!(upgradeStackToPut.getItem() instanceof StackUpgradeItem)) {
            return canRemoveUpgradeFrom(storageWrapper);
        }
        StackUpgradeItem other = (StackUpgradeItem) upgradeStackToPut.getItem();
        if (other.stackSizeMultiplier >= stackSizeMultiplier) {
            return UpgradeSlotChangeResult.SUCCESS;
        }
        return isMultiplierHighEnough(storageWrapper, getCurrentMultiplier(storageWrapper) / stackSizeMultiplier * other.stackSizeMultiplier);
    }

    private static UpgradeSlotChangeResult isMultiplierHighEnough(IStorageWrapper storageWrapper, double multiplier) {
        Set<Integer> slotsOverMultiplier = new HashSet<>();
        InventoryHandler inventory = storageWrapper.getInventoryHandler();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getCount() <= 1) {
                continue;
            }
            if ((double) stack.getCount() / stack.getMaxStackSize() > multiplier) {
                slotsOverMultiplier.add(slot);
            }
        }
        Set<Integer> errorUpgradeSlots = new HashSet<>();
        for (Map.Entry<Integer, IUpgradeWrapper> entry : storageWrapper.getUpgradeHandler().getSlotWrappers().entrySet()) {
            if (entry.getValue() instanceof IStackableContentsUpgrade && ((IStackableContentsUpgrade) entry.getValue()).getMinimumMultiplierRequired() > multiplier) {
                errorUpgradeSlots.add(entry.getKey());
            }
        }
        if (!slotsOverMultiplier.isEmpty() || !errorUpgradeSlots.isEmpty()) {
            return UpgradeSlotChangeResult.fail("remove.stack_low_multiplier", errorUpgradeSlots, slotsOverMultiplier, formatMultiplier(multiplier));
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }

    private static String formatMultiplier(double multiplier) {
        return multiplier == Math.rint(multiplier) ? String.valueOf((long) multiplier) : String.valueOf(multiplier);
    }

    public static class Wrapper extends UpgradeWrapperBase<Wrapper, StackUpgradeItem> {
        protected Wrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler);
        }

        public double getStackSizeMultiplier() {
            return upgradeItem.getStackSizeMultiplier();
        }

        @Override
        public boolean hideSettingsTab() {
            return true;
        }

        @Override
        public boolean canBeDisabled() {
            return false;
        }
    }
}
