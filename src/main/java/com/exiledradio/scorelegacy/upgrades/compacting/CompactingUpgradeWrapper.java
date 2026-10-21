/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.compacting;

import com.exiledradio.scorelegacy.api.IInsertResponseUpgrade;
import com.exiledradio.scorelegacy.api.ISlotChangeResponseUpgrade;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.IFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import com.exiledradio.scorelegacy.util.NBTHelper;
import com.exiledradio.scorelegacy.util.RecipeHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CompactingUpgradeWrapper extends UpgradeWrapperBase<CompactingUpgradeWrapper, CompactingUpgradeItem>
        implements IInsertResponseUpgrade, IFilteredUpgrade, ISlotChangeResponseUpgrade, ITickableUpgrade {
    private final FilterLogic filterLogic;
    private final Set<Integer> slotsToCompact = new HashSet<>();
    // Compacting inserts results, which would call back into onAfterInsert and recurse.
    private boolean compacting = false;

    public CompactingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(),
                stack -> !stack.hasTagCompound() && !RecipeHelper.getItemCompactingShapes(stack).isEmpty());
    }

    @Override
    public ItemStack onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public void onAfterInsert(IItemHandlerSimpleInserter inventoryHandler, int slot) {
        compactSlot(inventoryHandler, slot);
    }

    private void compactSlot(IItemHandlerSimpleInserter inventoryHandler, int slot) {
        if (compacting) {
            return;
        }
        ItemStack slotStack = inventoryHandler.getStackInSlot(slot);
        if (slotStack.isEmpty() || slotStack.hasTagCompound() || !filterLogic.matchesFilter(slotStack)) {
            return;
        }
        Set<RecipeHelper.CompactingShape> shapes = RecipeHelper.getItemCompactingShapes(slotStack);
        // Config-listed items (NoTreePunching's rocks) compact like reversible ones, even on the basic upgrade.
        boolean compactNonUncraftable = shouldCompactNonUncraftable() || RecipeHelper.isAlwaysCompacted(slotStack);
        compacting = true;
        try {
            if (upgradeItem.shouldCompactThreeByThree() && (shapes.contains(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)
                    || (compactNonUncraftable && shapes.contains(RecipeHelper.CompactingShape.THREE_BY_THREE)))) {
                tryCompacting(inventoryHandler, slotStack, 3);
            } else if (shapes.contains(RecipeHelper.CompactingShape.TWO_BY_TWO_UNCRAFTABLE)
                    || (compactNonUncraftable && shapes.contains(RecipeHelper.CompactingShape.TWO_BY_TWO))) {
                tryCompacting(inventoryHandler, slotStack, 2);
            }
        } finally {
            compacting = false;
        }
    }

    private void tryCompacting(IItemHandlerSimpleInserter inventoryHandler, ItemStack item, int size) {
        int totalCount = size * size;
        RecipeHelper.CompactingResult compactingResult = RecipeHelper.getCompactingResult(item, size);
        if (compactingResult.getResult().isEmpty()) {
            return;
        }
        ItemStack template = item.copy();
        template.setCount(totalCount);
        ItemStack extracted = InventoryHelper.extractFromInventory(template, inventoryHandler, true);
        while (extracted.getCount() == totalCount && fitsResultAndRemainingItems(inventoryHandler, compactingResult.getRemainingItems(), compactingResult.getResult())) {
            InventoryHelper.extractFromInventory(template, inventoryHandler, false);
            inventoryHandler.insertItem(compactingResult.getResult(), false);
            InventoryHelper.insertIntoInventory(compactingResult.getRemainingItems(), inventoryHandler, false);
            extracted = InventoryHelper.extractFromInventory(template, inventoryHandler, true);
        }
    }

    private static boolean fitsResultAndRemainingItems(IItemHandler inventoryHandler, List<ItemStack> remainingItems, ItemStack result) {
        if (!remainingItems.isEmpty()) {
            IItemHandler clonedHandler = InventoryHelper.cloneInventory(inventoryHandler);
            return InventoryHelper.insertIntoInventory(result, clonedHandler, false).isEmpty()
                    && InventoryHelper.insertIntoInventory(remainingItems, clonedHandler, false).isEmpty();
        }
        return InventoryHelper.insertIntoInventory(result, inventoryHandler, true).isEmpty();
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    public boolean shouldCompactNonUncraftable() {
        return NBTHelper.getBoolean(upgrade, "compactNonUncraftable", false);
    }

    public void setCompactNonUncraftable(boolean shouldCompactNonUncraftable) {
        NBTHelper.setBoolean(upgrade, "compactNonUncraftable", shouldCompactNonUncraftable);
        save();
    }

    @Override
    public void onSlotChange(IItemHandler inventoryHandler, int slot) {
        if (shouldWorkInGUI()) {
            slotsToCompact.add(slot);
        }
    }

    public void setShouldWorkdInGUI(boolean shouldWorkdInGUI) {
        NBTHelper.setBoolean(upgrade, "shouldWorkInGUI", shouldWorkdInGUI);
        save();
    }

    public boolean shouldWorkInGUI() {
        return NBTHelper.getBoolean(upgrade, "shouldWorkInGUI", false);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (slotsToCompact.isEmpty()) {
            return;
        }
        for (int slot : slotsToCompact) {
            compactSlot(storageWrapper.getInventoryHandler(), slot);
        }
        slotsToCompact.clear();
    }
}
