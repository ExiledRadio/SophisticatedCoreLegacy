/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.infinity;

import com.exiledradio.scorelegacy.api.IInventoryPartUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.inventory.IInventoryPart;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Every item in the storage becomes endless (the original 1.20 Core's InfinityUpgradeItem): a
 * slot that holds something gives it out without running down and takes nothing more. It must be
 * the storage's only upgrade. The admin version can only be taken out, or its storage broken, by
 * operators; the survival one by anyone. Neither has a recipe, as in the original.
 */
public class InfinityUpgradeItem extends UpgradeItemBase<InfinityUpgradeItem.Wrapper> {
    public static final UpgradeType<Wrapper> TYPE = new UpgradeType<>(Wrapper::new);
    /** The permission level operators have (EntityPlayer#canUseCommand). */
    private static final int ADMIN_PERMISSION_LEVEL = 2;

    private final boolean admin;

    public InfinityUpgradeItem(String name, CreativeTabs tab, boolean admin) {
        super(name, tab);
        this.admin = admin;
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    /** Only on its own: no other upgrade may be in the storage (and none can be added while it is; see UpgradeLimits). */
    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelStorage) {
        UpgradeHandler upgrades = storageWrapper.getUpgradeHandler();
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (!upgrades.getStackInSlot(slot).isEmpty()) {
                return UpgradeSlotChangeResult.fail("add.any_upgrade_exists", Collections.singleton(slot), Collections.emptySet());
            }
        }
        return super.canAddUpgradeTo(storageWrapper, upgradeStack, firstLevelStorage);
    }

    /** Whether this player may take the upgrade out, or break a storage holding it. */
    public boolean canBeHandledBy(EntityPlayer player) {
        return !admin || player.canUseCommand(ADMIN_PERMISSION_LEVEL, "");
    }

    /** Whether any of the storage's upgrades is an infinity upgrade this player may not handle. */
    public static boolean isLockedFor(UpgradeHandler upgrades, EntityPlayer player) {
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (stack.getItem() instanceof InfinityUpgradeItem && !((InfinityUpgradeItem) stack.getItem()).canBeHandledBy(player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInfinityUpgrade(UpgradeHandler upgrades) {
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (upgrades.getStackInSlot(slot).getItem() instanceof InfinityUpgradeItem) {
                return true;
            }
        }
        return false;
    }

    public static class Wrapper extends UpgradeWrapperBase<Wrapper, InfinityUpgradeItem> implements IInventoryPartUpgrade {
        protected Wrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public boolean canBeDisabled() {
            return false;
        }

        @Override
        public boolean hideSettingsTab() {
            return true;
        }

        @Nullable
        @Override
        public IInventoryPart getInventoryPart(InventoryHandler inventoryHandler, List<IInventoryPart> takenParts) {
            return takenParts.isEmpty() ? new InfinityInventoryPart(inventoryHandler) : null;
        }
    }

    /**
     * Every slot: one that holds a stack shows and gives out endless copies of it and takes
     * nothing; an empty one takes a stack like any slot (the original lets only players with the
     * permission fill it; 1.12's item handlers don't know who is inserting).
     */
    static class InfinityInventoryPart implements IInventoryPart {
        private final InventoryHandler parent;

        InfinityInventoryPart(InventoryHandler parent) {
            this.parent = parent;
        }

        @Override
        public int getFirstSlot() {
            return 0;
        }

        @Override
        public int getSlots() {
            return parent.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stored = parent.getRawStack(slot);
            if (stored.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack endless = stored.copy();
            endless.setCount(Integer.MAX_VALUE);
            return endless;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!parent.getRawStack(slot).isEmpty() || stack.isEmpty()) {
                return stack;
            }
            if (!simulate) {
                parent.setRawStack(slot, stack.copy());
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stored = parent.getRawStack(slot);
            if (stored.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack copy = stored.copy();
            copy.setCount(amount);
            return copy;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (parent.getRawStack(slot).isEmpty()) {
                parent.setRawStack(slot, stack.copy());
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return parent.getRawStack(slot).isEmpty();
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getStackLimit(int slot, ItemStack stack) {
            return Integer.MAX_VALUE;
        }
    }
}
