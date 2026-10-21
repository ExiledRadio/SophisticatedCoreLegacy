/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.inventory;

import com.exiledradio.scorelegacy.api.IInsertResponseUpgrade;
import com.exiledradio.scorelegacy.api.IInventoryPartUpgrade;
import com.exiledradio.scorelegacy.api.IOverflowResponseUpgrade;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import java.util.Collections;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntConsumer;

public class InventoryHandler extends ItemStackHandler implements IItemHandlerSimpleInserter {
    public static final String INVENTORY_TAG = "inventory";
    // Item NBT stores the count as a byte; this keeps counts above 127 for stack upgrades.
    private static final String REAL_COUNT_TAG = "realCount";

    private final NBTTagCompound contentsNbt;
    private final Runnable markDirty;
    private final StorageContents contents;
    private final List<IntConsumer> onContentsChangedListeners = new ArrayList<>();
    // Per-slot serialized form, so a change to one slot doesn't re-serialize the whole backpack.
    private final Map<Integer, NBTTagCompound> stackNbts = new LinkedHashMap<>();
    private boolean persistent = true;
    private int slotLimit = 64;
    private int usableSlots = Integer.MAX_VALUE;
    /** Slot runs upgrades have taken over (compression); rebuilt when the upgrades change. */
    private List<IInventoryPart> parts = Collections.emptyList();
    private boolean partsDirty = true;

    public InventoryHandler(int numberOfSlots, NBTTagCompound contentsNbt, Runnable markDirty, StorageContents contents) {
        super(numberOfSlots);
        this.contentsNbt = contentsNbt;
        this.markDirty = markDirty;
        this.contents = contents;
        deserializeNBT(contentsNbt.getCompoundTag(INVENTORY_TAG));
        initStackNbts();
    }

    private void initStackNbts() {
        stackNbts.clear();
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack slotStack = stacks.get(slot);
            if (!slotStack.isEmpty()) {
                stackNbts.put(slot, getSlotStackNbt(slot, slotStack));
            }
        }
    }

    @Override
    public void setSize(int size) {
        // Size comes from the backpack tier, never from the saved NBT, so this is a no-op. Items
        // saved in slots the current size doesn't have stay in the NBT until the backpack grows.
    }

    /** Adds slots after a tier upgrade, keeping every item where it was. */
    public void grow(int newSize) {
        NonNullList<ItemStack> grown = NonNullList.withSize(newSize, ItemStack.EMPTY);
        for (int slot = 0; slot < stacks.size(); slot++) {
            grown.set(slot, stacks.get(slot));
        }
        stacks = grown;
        // Pick up anything that was saved beyond the old size.
        deserializeNBT(contentsNbt.getCompoundTag(INVENTORY_TAG));
        initStackNbts();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (persistent && updateSlotNbt(slot)) {
            saveInventory();
            for (IntConsumer listener : new ArrayList<>(onContentsChangedListeners)) {
                listener.accept(slot);
            }
        }
    }

    private boolean updateSlotNbt(int slot) {
        ItemStack slotStack = stacks.get(slot);
        if (slotStack.isEmpty()) {
            return stackNbts.remove(slot) != null;
        }
        NBTTagCompound itemTag = getSlotStackNbt(slot, slotStack);
        if (!itemTag.equals(stackNbts.get(slot))) {
            stackNbts.put(slot, itemTag);
            return true;
        }
        return false;
    }

    private static NBTTagCompound getSlotStackNbt(int slot, ItemStack slotStack) {
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setInteger("Slot", slot);
        itemTag.setInteger(REAL_COUNT_TAG, slotStack.getCount());
        slotStack.writeToNBT(itemTag);
        return itemTag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");
            if (slot >= 0 && slot < stacks.size()) {
                ItemStack slotStack = new ItemStack(itemTags);
                if (itemTags.hasKey(REAL_COUNT_TAG)) {
                    slotStack.setCount(itemTags.getInteger(REAL_COUNT_TAG));
                }
                stacks.set(slot, slotStack);
            }
        }
        onLoad();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();
        for (NBTTagCompound stackNbt : stackNbts.values()) {
            nbtTagList.appendTag(stackNbt);
        }
        // Keep items from slots beyond the current size (a config lowered the slot count).
        NBTTagList saved = contentsNbt.getCompoundTag(INVENTORY_TAG).getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < saved.tagCount(); i++) {
            NBTTagCompound itemTags = saved.getCompoundTagAt(i);
            if (itemTags.getInteger("Slot") >= stacks.size()) {
                nbtTagList.appendTag(itemTags);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        return nbt;
    }

    public void saveInventory() {
        contentsNbt.setTag(INVENTORY_TAG, serializeNBT());
        markDirty.run();
    }

    /** While false, slot changes (client container sync) don't write back into the contents NBT. */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    // --- parts (upgrades that take over slots) ------------------------------------------------

    /** The upgrades changed: parts are looked up again on next use. */
    public void refreshParts() {
        partsDirty = true;
    }

    /**
     * The part covering this slot, or null. Only on the server thread: the client's copy of the
     * inventory just mirrors what the server shows in each slot.
     */
    @Nullable
    private IInventoryPart getPart(int slot) {
        if (!FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            return null;
        }
        for (IInventoryPart part : getParts()) {
            if (part.coversSlot(slot)) {
                return part;
            }
        }
        return null;
    }

    public List<IInventoryPart> getParts() {
        if (partsDirty) {
            partsDirty = false;
            UpgradeHandler upgrades = contents.getExistingUpgradeHandler();
            List<IInventoryPart> found = new ArrayList<>();
            if (upgrades != null) {
                for (IInventoryPartUpgrade upgrade : upgrades.getListOfWrappersThatImplement(IInventoryPartUpgrade.class)) {
                    IInventoryPart part = upgrade.getInventoryPart(this, Collections.unmodifiableList(found));
                    if (part != null) {
                        found.add(part);
                    }
                }
            }
            parts = found.isEmpty() ? Collections.emptyList() : found;
        }
        return parts;
    }

    /** Slots a part has taken over, which sorting leaves alone. */
    public Set<Integer> getPartSlots() {
        Set<Integer> slots = new java.util.HashSet<>();
        for (IInventoryPart part : getParts()) {
            for (int slot = part.getFirstSlot(); slot < part.getFirstSlot() + part.getSlots(); slot++) {
                slots.add(slot);
            }
        }
        return slots;
    }

    /** The stack really stored in a slot, whatever a part shows there. */
    public ItemStack getRawStack(int slot) {
        return stacks.get(slot);
    }

    /** Stores a stack in a slot directly (parts keep their real stacks this way). */
    public void setRawStack(int slot, ItemStack stack) {
        super.setStackInSlot(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        IInventoryPart part = getPart(slot);
        return part != null ? part.getStackInSlot(slot) : super.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        IInventoryPart part = getPart(slot);
        if (part != null) {
            part.setStackInSlot(slot, stack);
        } else {
            super.setStackInSlot(slot, stack);
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IInventoryPart part = getPart(slot);
        return part != null ? part.extractItem(slot, amount, simulate) : super.extractItem(slot, amount, simulate);
    }

    // --- limits ------------------------------------------------------------------------------

    @Override
    public int getSlotLimit(int slot) {
        IInventoryPart part = getPart(slot);
        return part != null ? part.getSlotLimit(slot) : slotLimit;
    }

    /** The slot limit of a normal slot (parts may report other limits for theirs). */
    public int getBaseSlotLimit() {
        return slotLimit;
    }

    public int getStackSizeMultiplier() {
        return Math.max(1, slotLimit / 64);
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        IInventoryPart part = getPart(slot);
        return part != null ? part.getStackLimit(slot, stack) : getBaseStackLimit(stack);
    }

    /** How many of this stack a normal slot holds with the current stack upgrades. */
    public int getBaseStackLimit(@Nonnull ItemStack stack) {
        if (!canStackBeyondNormal(stack)) {
            return stack.getMaxStackSize();
        }
        if (slotLimit < 64) {
            // A stack downgrade: the same share of every item's normal stack, at least one.
            return (int) Math.max(1, (double) stack.getMaxStackSize() * slotLimit / 64);
        }
        return (int) Math.min(slotLimit, (long) stack.getMaxStackSize() * getStackSizeMultiplier());
    }

    private boolean canStackBeyondNormal(ItemStack stack) {
        ResourceLocation name = stack.getItem().getRegistryName();
        if (name == null) {
            return true;
        }
        for (String nonStackable : contents.getPolicy().getNonStackableItems()) {
            if (name.toString().equals(nonStackable)) {
                return false;
            }
        }
        return true;
    }

    /** How many of this stack fit in the slot, without running any insert simulation or upgrade hooks. */
    public int getMaxStackSizeFor(int slot, ItemStack stack) {
        return getStackLimit(slot, stack);
    }

    /** Plain insert for GUI moves: limits apply, upgrade insert hooks don't (they react to slot changes instead). */
    public ItemStack insertItemFromGui(int slot, ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) ? insertCopy(slot, stack, simulate) : stack;
    }

    /**
     * ItemStackHandler stores the caller's own stack object when it lands in an empty slot, so a
     * caller that then shrinks its stack (as vanilla container code does) would empty the slot
     * too, after the NBT already saved the full count. Inserting a copy keeps the two apart.
     */
    private ItemStack insertCopy(int slot, ItemStack stack, boolean simulate) {
        IInventoryPart part = getPart(slot);
        if (part != null) {
            return part.insertItem(slot, stack, simulate);
        }
        return super.insertItem(slot, simulate ? stack : stack.copy(), simulate);
    }

    public void setSlotLimit(int slotLimit) {
        this.slotLimit = slotLimit;
    }

    // --- what may go in ----------------------------------------------------------------------

    /**
     * Slots from this index on sit under a tank or battery. They are kept empty (the upgrade
     * can't be added while they hold anything) and refuse inserts from upgrades and automation.
     */
    public void setUsableSlots(int usableSlots) {
        this.usableSlots = usableSlots;
    }

    public int getUsableSlots() {
        return Math.min(usableSlots, getSlots());
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot >= usableSlots) {
            return false;
        }
        IInventoryPart part = getPart(slot);
        if (part != null && !part.isItemValid(slot, stack)) {
            return false;
        }
        MemorySettingsCategory memory = getMemorySettings();
        if (memory != null && !memory.matchesFilter(slot, stack)) {
            return false;
        }
        return contents.getPolicy().isItemValid(contents, slot, stack);
    }

    // --- inserting ---------------------------------------------------------------------------

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack remaining = insertItemWithoutOverflow(slot, stack, simulate);
        return remaining.isEmpty() ? remaining : triggerOverflowUpgrades(remaining);
    }

    /** Inserts anywhere: first onto matching stacks, then into empty slots. */
    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < getSlots() && !remaining.isEmpty(); slot++) {
            if (ItemHandlerHelper.canItemStacksStack(getStackInSlot(slot), remaining)) {
                remaining = insertItemWithoutOverflow(slot, remaining, simulate);
            }
        }
        // Memory slots that remember this item come before any other empty slot.
        MemorySettingsCategory memory = getMemorySettings();
        Set<Integer> memorySlots = memory == null ? Collections.emptySet() : memory.getSlotsFor(remaining);
        for (int slot : memorySlots) {
            if (remaining.isEmpty()) {
                break;
            }
            if (slot < getSlots() && getStackInSlot(slot).isEmpty()) {
                remaining = insertItemWithoutOverflow(slot, remaining, simulate);
            }
        }
        for (int slot = 0; slot < getSlots() && !remaining.isEmpty(); slot++) {
            if (getStackInSlot(slot).isEmpty() && (memory == null || !memory.isSlotSelected(slot))) {
                remaining = insertItemWithoutOverflow(slot, remaining, simulate);
            }
        }
        return remaining.isEmpty() ? remaining : triggerOverflowUpgrades(remaining);
    }

    private ItemStack insertItemWithoutOverflow(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack remaining = runOnBeforeInsert(slot, stack, simulate);
        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }
        remaining = insertCopy(slot, remaining, simulate);
        if (!simulate && remaining.getCount() != stack.getCount()) {
            runOnAfterInsert(slot);
        }
        return remaining;
    }

    private ItemStack triggerOverflowUpgrades(ItemStack stack) {
        UpgradeHandler upgrades = contents.getExistingUpgradeHandler();
        if (upgrades == null) {
            return stack;
        }
        ItemStack remaining = stack;
        for (IOverflowResponseUpgrade overflowUpgrade : upgrades.getWrappersThatImplement(IOverflowResponseUpgrade.class)) {
            remaining = overflowUpgrade.onOverflow(remaining);
            if (remaining.isEmpty()) {
                break;
            }
        }
        return remaining;
    }

    private ItemStack runOnBeforeInsert(int slot, ItemStack stack, boolean simulate) {
        UpgradeHandler upgrades = contents.getExistingUpgradeHandler();
        if (upgrades == null) {
            return stack;
        }
        ItemStack remaining = stack;
        for (IInsertResponseUpgrade upgrade : upgrades.getWrappersThatImplementFromMainBackpack(IInsertResponseUpgrade.class)) {
            remaining = upgrade.onBeforeInsert(this, slot, remaining, simulate);
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return remaining;
    }

    private void runOnAfterInsert(int slot) {
        UpgradeHandler upgrades = contents.getExistingUpgradeHandler();
        if (upgrades == null) {
            return;
        }
        for (IInsertResponseUpgrade upgrade : upgrades.getWrappersThatImplementFromMainBackpack(IInsertResponseUpgrade.class)) {
            upgrade.onAfterInsert(this, slot);
        }
    }

    @Nullable
    private MemorySettingsCategory getMemorySettings() {
        SettingsHandler settings = contents.getExistingSettingsHandler();
        return settings == null ? null : settings.getTypeCategory(MemorySettingsCategory.class);
    }

    @Nullable
    public StorageContents getContents() {
        return contents;
    }

    public void addListener(IntConsumer onContentsChanged) {
        onContentsChangedListeners.add(onContentsChanged);
    }

    public void removeListener(IntConsumer onContentsChanged) {
        onContentsChangedListeners.remove(onContentsChanged);
    }
}
