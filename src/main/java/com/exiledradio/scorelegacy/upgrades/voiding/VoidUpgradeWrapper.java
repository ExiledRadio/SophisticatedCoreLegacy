/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.voiding;

import com.exiledradio.scorelegacy.api.IInsertResponseUpgrade;
import com.exiledradio.scorelegacy.api.IOverflowResponseUpgrade;
import com.exiledradio.scorelegacy.api.IPickupResponseUpgrade;
import com.exiledradio.scorelegacy.api.ISlotChangeResponseUpgrade;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicBase;
import com.exiledradio.scorelegacy.upgrades.IFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.PrimaryMatch;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Deletes matching items. Either everything matching the filter, or (overflow mode) only what
 * doesn't fit because a stack of it is already full.
 */
public class VoidUpgradeWrapper extends UpgradeWrapperBase<VoidUpgradeWrapper, VoidUpgradeItem>
        implements IPickupResponseUpgrade, IInsertResponseUpgrade, IFilteredUpgrade, ISlotChangeResponseUpgrade, ITickableUpgrade, IOverflowResponseUpgrade {
    private final FilterLogic filterLogic;
    private final Set<Integer> slotsToVoid = new HashSet<>();
    private boolean shouldVoidOverflow;

    public VoidUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount());
        filterLogic.setAllowByDefault();
        shouldVoidOverflow = NBTHelper.getBoolean(upgrade, "shouldVoidOverflow", false);
    }

    @Override
    public ItemStack pickup(World world, ItemStack stack, boolean simulate) {
        if (filterLogic.matchesFilter(stack)) {
            if (shouldVoidOverflow() && !simulate) {
                storageWrapper.getInventoryForUpgradeProcessing().insertItem(stack, false);
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public ItemStack onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemStack stack, boolean simulate) {
        if (shouldVoidOverflow() && inventoryHandler.getStackInSlot(slot).isEmpty()
                && (!filterLogic.shouldMatchNbt() || !filterLogic.shouldMatchDurability() || filterLogic.getPrimaryMatch() != PrimaryMatch.ITEM)
                && filterLogic.matchesFilter(stack)) {
            // Overflow mode: a new stack of something the backpack already holds is voided.
            for (int s = 0; s < inventoryHandler.getSlots(); s++) {
                if (s != slot && stackMatchesFilterStack(inventoryHandler.getStackInSlot(s), stack)) {
                    return ItemStack.EMPTY;
                }
            }
            return stack;
        }
        return !shouldVoidOverflow() && filterLogic.matchesFilter(stack) ? ItemStack.EMPTY : stack;
    }

    private boolean stackMatchesFilterStack(ItemStack stack, ItemStack filterStack) {
        if (stack.getItem() != filterStack.getItem()) {
            return false;
        }
        if (filterLogic.getPrimaryMatch() == PrimaryMatch.TAGS) {
            return true;
        }
        if (!FilterLogicBase.isSameItem(stack, filterStack)) {
            return false;
        }
        if (filterLogic.shouldMatchDurability() && stack.getItemDamage() != filterStack.getItemDamage()) {
            return false;
        }
        return !filterLogic.shouldMatchNbt() || ItemStack.areItemStackTagsEqual(stack, filterStack);
    }

    @Override
    public void onAfterInsert(IItemHandlerSimpleInserter inventoryHandler, int slot) {
        //noop
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    public void setShouldWorkdInGUI(boolean shouldWorkdInGUI) {
        NBTHelper.setBoolean(upgrade, "shouldWorkInGUI", shouldWorkdInGUI);
        save();
    }

    public boolean shouldWorkInGUI() {
        return NBTHelper.getBoolean(upgrade, "shouldWorkInGUI", false);
    }

    public void setShouldVoidOverflow(boolean shouldVoidOverflow) {
        this.shouldVoidOverflow = shouldVoidOverflow;
        NBTHelper.setBoolean(upgrade, "shouldVoidOverflow", shouldVoidOverflow);
        save();
    }

    /** Always overflow-only when the config turns "void anything" off. */
    public boolean shouldVoidOverflow() {
        return shouldVoidOverflow || !upgradeItem.isVoidAnythingEnabled();
    }

    @Override
    public void onSlotChange(IItemHandler inventoryHandler, int slot) {
        if (!shouldWorkInGUI() || shouldVoidOverflow()) {
            return;
        }
        if (filterLogic.matchesFilter(inventoryHandler.getStackInSlot(slot))) {
            slotsToVoid.add(slot);
        }
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (slotsToVoid.isEmpty()) {
            return;
        }
        InventoryHandler backpackInventory = storageWrapper.getInventoryHandler();
        for (int slot : slotsToVoid) {
            backpackInventory.extractItem(slot, backpackInventory.getStackInSlot(slot).getCount(), false);
        }
        slotsToVoid.clear();
    }

    @Override
    public boolean worksInGui() {
        return shouldWorkInGUI();
    }

    @Override
    public ItemStack onOverflow(ItemStack stack) {
        return filterLogic.matchesFilter(stack) ? ItemStack.EMPTY : stack;
    }

    @Override
    public boolean stackMatchesFilter(ItemStack stack) {
        return filterLogic.matchesFilter(stack);
    }
}
