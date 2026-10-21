/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FilterLogic extends FilterLogicBase {
    private static final String FILTERS_KEY = "filters";

    private final int filterSlotCount;
    private final Predicate<ItemStack> isItemValid;
    private FilterItemStackHandler filterHandler = null;
    private boolean emptyAllowListMatchesEverything = false;

    public FilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount) {
        this(upgrade, saveHandler, filterSlotCount, s -> true, "");
    }

    public FilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, Predicate<ItemStack> isItemValid) {
        this(upgrade, saveHandler, filterSlotCount, isItemValid, "");
    }

    public FilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, Predicate<ItemStack> isItemValid, String parentTagKey) {
        super(upgrade, saveHandler, parentTagKey);
        this.filterSlotCount = filterSlotCount;
        this.isItemValid = isItemValid;
    }

    public void setEmptyAllowListMatchesEverything() {
        emptyAllowListMatchesEverything = true;
    }

    public FilterItemStackHandler getFilterHandler() {
        if (filterHandler == null) {
            filterHandler = new FilterItemStackHandler(filterSlotCount) {
                @Override
                protected void onContentsChanged(int slot) {
                    super.onContentsChanged(slot);
                    NBTHelper.getOrCreateTag(upgrade, parentTagKey).setTag(FILTERS_KEY, serializeNBT());
                    save();
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt) {
                    // Size comes from the config, not the saved filters.
                    NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
                    for (int i = 0; i < tagList.tagCount(); i++) {
                        NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
                        int slot = itemTags.getInteger("Slot");
                        if (slot >= 0 && slot < stacks.size()) {
                            stacks.set(slot, new ItemStack(itemTags));
                        }
                    }
                    onLoad();
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    return stack.isEmpty() || (doesNotContain(stack) && isItemValid.test(stack));
                }

                private boolean doesNotContain(ItemStack stack) {
                    for (ItemStack filter : stacks) {
                        if (ItemHandlerHelper.canItemStacksStack(filter, stack)) {
                            return false;
                        }
                    }
                    return true;
                }
            };
            NBTTagCompound tag = NBTHelper.getTag(upgrade, parentTagKey);
            if (tag != null && tag.hasKey(FILTERS_KEY)) {
                filterHandler.deserializeNBT(tag.getCompoundTag(FILTERS_KEY));
            }
        }
        return filterHandler;
    }

    public boolean matchesFilter(ItemStack stack) {
        if (isAllowList()) {
            if (getPrimaryMatch() == PrimaryMatch.TAGS) {
                return isTagMatch(stack);
            }
            return (getFilterHandler().hasOnlyEmptyFilters() && emptyAllowListMatchesEverything) || anyFilterMatches(stack);
        }
        if (getPrimaryMatch() == PrimaryMatch.TAGS) {
            return !isTagMatch(stack);
        }
        return getFilterHandler().hasOnlyEmptyFilters() || !anyFilterMatches(stack);
    }

    private boolean anyFilterMatches(ItemStack stack) {
        FilterItemStackHandler handler = getFilterHandler();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (stackMatchesFilter(stack, handler.getStackInSlot(slot))) {
                return true;
            }
        }
        return false;
    }

    private boolean isTagMatch(ItemStack stack) {
        if (tagNames == null) {
            initTags();
        }
        Set<String> stackTags = getOreNames(stack);
        if (shouldMatchAnyTag()) {
            for (String tag : stackTags) {
                if (tagNames.contains(tag)) {
                    return true;
                }
            }
            return false;
        }
        return stackTags.containsAll(tagNames);
    }
}
