/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;

import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** A filter that can also say "anything already in this backpack". */
public class ContentsFilterLogic extends FilterLogic {
    private final Supplier<IItemHandler> getInventoryHandler;

    public ContentsFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, Supplier<IItemHandler> getInventoryHandler) {
        this(upgrade, saveHandler, filterSlotCount, getInventoryHandler, "");
    }

    /** With its settings under parentTagKey, for upgrades with two filters (Storage's hopper: "inputFilter", "outputFilter"). */
    public ContentsFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, Supplier<IItemHandler> getInventoryHandler, String parentTagKey) {
        super(upgrade, saveHandler, filterSlotCount, s -> true, parentTagKey);
        this.getInventoryHandler = getInventoryHandler;
    }

    public ContentsFilterType getFilterType() {
        if (shouldFilterByBackpack()) {
            return ContentsFilterType.BACKPACK;
        }
        return isAllowList() ? ContentsFilterType.ALLOW : ContentsFilterType.BLOCK;
    }

    public void setDepositFilterType(ContentsFilterType contentsFilterType) {
        switch (contentsFilterType) {
            case ALLOW:
                setFilterByBackpack(false);
                setAllowList(true);
                break;
            case BLOCK:
                setFilterByBackpack(false);
                setAllowList(false);
                break;
            case BACKPACK:
            default:
                setFilterByBackpack(true);
                save();
        }
    }

    @Override
    public boolean matchesFilter(ItemStack stack) {
        if (!shouldFilterByBackpack()) {
            return super.matchesFilter(stack);
        }
        // The original keeps a slot tracker for this; a scan is fine for 1.12-sized backpacks.
        IItemHandler inventory = getInventoryHandler.get();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (stackMatchesFilter(stack, inventory.getStackInSlot(slot))) {
                return true;
            }
        }
        // Items memory slots wait for count as contents too, even while those slots are empty.
        if (inventory instanceof InventoryHandler && ((InventoryHandler) inventory).getContents() != null) {
            SettingsHandler settings = ((InventoryHandler) inventory).getContents().getExistingSettingsHandler();
            MemorySettingsCategory memory = settings == null ? null : settings.getTypeCategory(MemorySettingsCategory.class);
            return memory != null && memory.isMemorized(stack);
        }
        return false;
    }

    private void setFilterByBackpack(boolean filterByBackpack) {
        NBTHelper.setBoolean(upgrade, "filterByBackpack", filterByBackpack);
        save();
    }

    private boolean shouldFilterByBackpack() {
        return NBTHelper.getBoolean(upgrade, "filterByBackpack", false);
    }
}
