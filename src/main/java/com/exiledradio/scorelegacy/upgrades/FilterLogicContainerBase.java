/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.common.gui.IFilterSlot;
import com.exiledradio.scorelegacy.common.gui.IServerUpdater;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Container side of a filter: its ghost slots, the settings buttons' state, and the
 * ore-dictionary ("tag") list editor.
 */
public class FilterLogicContainerBase<T extends FilterLogicBase, S extends Slot> {
    private static final String DATA_IS_ALLOW_LIST = "isAllowList";
    private static final String DATA_MATCH_DURABILITY = "matchDurability";
    private static final String DATA_MATCH_NBT = "matchNbt";
    private static final String DATA_PRIMARY_MATCH = "primaryMatch";
    private static final String DATA_ADD_TAG_NAME = "addTagName";
    private static final String DATA_REMOVE_TAG_NAME = "removeTagName";
    private static final String DATA_MATCH_ANY_TAG = "matchAnyTag";
    private static final String DATA_PARENT_TAG_KEY = "parentTagKey";

    protected final List<S> filterSlots = new ArrayList<>();
    protected final IServerUpdater serverUpdater;
    protected final Supplier<T> filterLogic;
    private final TagSelectionSlot tagSelectionSlot;
    private final Set<String> tagsToAdd = new TreeSet<>();
    private int selectedTagToAdd = 0;
    private int selectedTagToRemove = 0;

    public FilterLogicContainerBase(IServerUpdater serverUpdater, Supplier<T> filterLogic, Consumer<Slot> addSlot) {
        this.serverUpdater = serverUpdater;
        this.filterLogic = filterLogic;
        tagSelectionSlot = new TagSelectionSlot();
        addSlot.accept(tagSelectionSlot);
    }

    public int getSelectedTagToAdd() {
        return selectedTagToAdd;
    }

    public int getSelectedTagToRemove() {
        return selectedTagToRemove;
    }

    public TagSelectionSlot getTagSelectionSlot() {
        return tagSelectionSlot;
    }

    public List<S> getFilterSlots() {
        return filterSlots;
    }

    public Set<String> getTagNames() {
        return filterLogic.get().getTagNames();
    }

    public Set<String> getTagsToAdd() {
        return tagsToAdd;
    }

    public void addSelectedTag() {
        String tagName = getAtIndex(tagsToAdd, selectedTagToAdd);
        if (tagName == null) {
            return;
        }
        filterLogic.get().addTagName(tagName);
        sendDataToServer(() -> NBTHelper.withString(DATA_ADD_TAG_NAME, tagName));
        selectedTagToRemove = 0;
        tagsToAdd.remove(tagName);
        selectedTagToAdd = Math.max(0, selectedTagToAdd - 1);
    }

    public void removeSelectedTag() {
        String tagName = getAtIndex(getTagNames(), selectedTagToRemove);
        if (tagName == null) {
            return;
        }
        filterLogic.get().removeTagName(tagName);
        sendDataToServer(() -> NBTHelper.withString(DATA_REMOVE_TAG_NAME, tagName));
        if (FilterLogicBase.getOreNames(tagSelectionSlot.getStack()).contains(tagName)) {
            tagsToAdd.add(tagName);
        }
        selectedTagToRemove = Math.max(0, selectedTagToRemove - 1);
    }

    public void selectNextTagToRemove() {
        selectedTagToRemove = next(getTagNames().size(), selectedTagToRemove);
    }

    public void selectPreviousTagToRemove() {
        selectedTagToRemove = previous(getTagNames().size(), selectedTagToRemove);
    }

    public void selectNextTagToAdd() {
        selectedTagToAdd = next(tagsToAdd.size(), selectedTagToAdd);
    }

    public void selectPreviousTagToAdd() {
        selectedTagToAdd = previous(tagsToAdd.size(), selectedTagToAdd);
    }

    private static int next(int size, int index) {
        return index + 1 >= size ? 0 : index + 1;
    }

    private static int previous(int size, int index) {
        return index == 0 ? Math.max(0, size - 1) : index - 1;
    }

    private static String getAtIndex(Set<String> set, int index) {
        int current = 0;
        for (String value : set) {
            if (current == index) {
                return value;
            }
            current++;
        }
        return null;
    }

    public boolean isAllowList() {
        return filterLogic.get().isAllowList();
    }

    public boolean shouldMatchDurability() {
        return filterLogic.get().shouldMatchDurability();
    }

    public boolean shouldMatchNbt() {
        return filterLogic.get().shouldMatchNbt();
    }

    public PrimaryMatch getPrimaryMatch() {
        return filterLogic.get().getPrimaryMatch();
    }

    public boolean shouldMatchAnyTag() {
        return filterLogic.get().shouldMatchAnyTag();
    }

    public void setAllowList(boolean isAllowList) {
        filterLogic.get().setAllowList(isAllowList);
        sendDataToServer(() -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(DATA_IS_ALLOW_LIST, isAllowList);
            return tag;
        });
    }

    public void setMatchDurability(boolean matchDurability) {
        filterLogic.get().setMatchDurability(matchDurability);
        sendDataToServer(() -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(DATA_MATCH_DURABILITY, matchDurability);
            return tag;
        });
    }

    public void setMatchNbt(boolean matchNbt) {
        filterLogic.get().setMatchNbt(matchNbt);
        sendDataToServer(() -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(DATA_MATCH_NBT, matchNbt);
            return tag;
        });
    }

    public void setPrimaryMatch(PrimaryMatch primaryMatch) {
        filterLogic.get().setPrimaryMatch(primaryMatch);
        sendDataToServer(() -> NBTHelper.withString(DATA_PRIMARY_MATCH, primaryMatch.getSerializedName()));
    }

    public void setMatchAnyTag(boolean matchAnyTag) {
        filterLogic.get().setMatchAnyTag(matchAnyTag);
        sendDataToServer(() -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(DATA_MATCH_ANY_TAG, matchAnyTag);
            return tag;
        });
    }

    /**
     * Sends a setting to the server, marked with the filter's tag key when it has one, so an
     * upgrade with two filters (Storage's hopper) can tell them apart (as in the original).
     */
    protected void sendDataToServer(Supplier<NBTTagCompound> dataSupplier) {
        String parentTagKey = filterLogic.get().getParentTagKey();
        if (parentTagKey.isEmpty()) {
            serverUpdater.sendDataToServer(dataSupplier);
        } else {
            serverUpdater.sendDataToServer(() -> {
                NBTTagCompound tag = dataSupplier.get();
                tag.setString(DATA_PARENT_TAG_KEY, parentTagKey);
                return tag;
            });
        }
    }

    /** A message meant for another filter of the same upgrade. */
    protected boolean isOtherFilterMessage(NBTTagCompound data) {
        return data.hasKey(DATA_PARENT_TAG_KEY) && !filterLogic.get().getParentTagKey().equals(data.getString(DATA_PARENT_TAG_KEY));
    }

    /** @return true if the message was a filter setting */
    public boolean handleMessage(NBTTagCompound data) {
        if (isOtherFilterMessage(data)) {
            return false;
        }
        for (String key : data.getKeySet()) {
            switch (key) {
                case DATA_IS_ALLOW_LIST:
                    setAllowList(data.getBoolean(key));
                    return true;
                case DATA_MATCH_DURABILITY:
                    setMatchDurability(data.getBoolean(key));
                    return true;
                case DATA_MATCH_NBT:
                    setMatchNbt(data.getBoolean(key));
                    return true;
                case DATA_PRIMARY_MATCH:
                    setPrimaryMatch(PrimaryMatch.fromName(data.getString(key)));
                    return true;
                case DATA_ADD_TAG_NAME:
                    filterLogic.get().addTagName(data.getString(key));
                    return true;
                case DATA_REMOVE_TAG_NAME:
                    filterLogic.get().removeTagName(data.getString(key));
                    return true;
                case DATA_MATCH_ANY_TAG:
                    setMatchAnyTag(data.getBoolean(key));
                    return true;
                default:
            }
        }
        return false;
    }

    /** A ghost slot whose item only offers its ore dictionary names to the tag editor. */
    public class TagSelectionSlot extends Slot implements IFilterSlot {
        private ItemStack stack = ItemStack.EMPTY;
        private Runnable onUpdate = () -> {};

        TagSelectionSlot() {
            super(new InventoryBasic("", false, 1), 0, -1, -1);
        }

        public void setOnUpdate(Runnable onUpdate) {
            this.onUpdate = onUpdate;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.isEmpty() || !FilterLogicBase.getOreNames(stack).isEmpty();
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }

        @Nonnull
        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack decrStackSize(int amount) {
            stack = ItemStack.EMPTY;
            return stack;
        }

        @Override
        public boolean isSameInventory(Slot other) {
            return false;
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
            this.stack = stack;
            tagsToAdd.clear();
            tagsToAdd.addAll(FilterLogicBase.getOreNames(stack));
            tagsToAdd.removeAll(getTagNames());
            selectedTagToAdd = 0;
            onUpdate.run();
        }

        @Override
        public void onSlotChanged() {
            //noop
        }
    }
}
