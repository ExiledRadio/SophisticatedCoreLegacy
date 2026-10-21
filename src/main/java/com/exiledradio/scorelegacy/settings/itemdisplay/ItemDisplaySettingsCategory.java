/*
 * Modified from Sophisticated Backpacks / Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.itemdisplay;

import com.exiledradio.scorelegacy.settings.ISettingsCategory;
import com.exiledradio.scorelegacy.settings.ISlotColorCategory;
import com.exiledradio.scorelegacy.settings.SettingsColors;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The slots whose items are shown on the outside of the storage (Backpacks: one item on the front
 * pouch; Storage barrels: up to four on the front), each with its own rotation. Whenever what is
 * shown changes, the storage is told so it can update what clients render. An empty selected slot
 * shows the item its memory slot remembers, as in the original.
 */
public class ItemDisplaySettingsCategory implements ISettingsCategory, ISlotColorCategory {
    public static final String NAME = "item_display";
    private static final String SLOTS_TAG = "slots";
    private static final String ROTATIONS_TAG = "rotations";
    private static final String COLOR_TAG = "color";
    private static final String DISPLAY_SIDE_TAG = "displaySide";
    // Before multiple slots: one "slot" and one "rotation".
    private static final String LEGACY_SLOT_TAG = "slot";
    private static final String LEGACY_ROTATION_TAG = "rotation";

    private final Supplier<IItemHandler> inventory;
    private final Consumer<List<DisplayedItem>> onDisplayChanged;
    private final int itemNumberLimit;
    @Nullable
    private final Supplier<MemorySettingsCategory> memory;
    private NBTTagCompound categoryNbt;
    private final Consumer<NBTTagCompound> saveNbt;
    private EnumDyeColor color = EnumDyeColor.RED;
    private final List<Integer> slotIndexes = new ArrayList<>();
    private final Map<Integer, Integer> slotRotations = new HashMap<>();
    private DisplaySide displaySide = DisplaySide.FRONT;

    /** One shown slot: Backpacks' front pouch. Selecting another slot moves the display there. */
    public ItemDisplaySettingsCategory(Supplier<IItemHandler> inventory, BiConsumer<ItemStack, Integer> onDisplayChanged,
                                       NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
        this(inventory, items -> {
            DisplayedItem first = items.isEmpty() ? null : items.get(0);
            onDisplayChanged.accept(first == null ? ItemStack.EMPTY : first.getItem(), first == null ? 0 : first.getRotation());
        }, categoryNbt, saveNbt, 1, null);
    }

    public ItemDisplaySettingsCategory(Supplier<IItemHandler> inventory, Consumer<List<DisplayedItem>> onDisplayChanged,
                                       NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt, int itemNumberLimit,
                                       @Nullable Supplier<MemorySettingsCategory> memory) {
        this.inventory = inventory;
        this.onDisplayChanged = onDisplayChanged;
        this.categoryNbt = categoryNbt;
        this.saveNbt = saveNbt;
        this.itemNumberLimit = itemNumberLimit;
        this.memory = memory;
        deserialize();
    }

    private void deserialize() {
        slotIndexes.clear();
        slotRotations.clear();
        for (int slot : categoryNbt.getIntArray(SLOTS_TAG)) {
            slotIndexes.add(slot);
        }
        NBTTagCompound rotations = categoryNbt.getCompoundTag(ROTATIONS_TAG);
        for (String key : rotations.getKeySet()) {
            try {
                slotRotations.put(Integer.parseInt(key), rotations.getInteger(key));
            } catch (NumberFormatException e) {
                //skip a damaged entry
            }
        }
        if (categoryNbt.hasKey(LEGACY_SLOT_TAG, Constants.NBT.TAG_INT)) {
            int slot = categoryNbt.getInteger(LEGACY_SLOT_TAG);
            if (slot >= 0 && !slotIndexes.contains(slot)) {
                slotIndexes.add(slot);
                slotRotations.put(slot, categoryNbt.getInteger(LEGACY_ROTATION_TAG));
            }
            categoryNbt.removeTag(LEGACY_SLOT_TAG);
            categoryNbt.removeTag(LEGACY_ROTATION_TAG);
            serialize();
        }
        color = categoryNbt.hasKey(COLOR_TAG) ? EnumDyeColor.byMetadata(categoryNbt.getInteger(COLOR_TAG)) : EnumDyeColor.RED;
        displaySide = DisplaySide.fromName(categoryNbt.getString(DISPLAY_SIDE_TAG));
    }

    private void serialize() {
        if (slotIndexes.isEmpty()) {
            categoryNbt.removeTag(SLOTS_TAG);
            categoryNbt.removeTag(ROTATIONS_TAG);
        } else {
            int[] slots = new int[slotIndexes.size()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = slotIndexes.get(i);
            }
            categoryNbt.setTag(SLOTS_TAG, new NBTTagIntArray(slots));
            NBTTagCompound rotations = new NBTTagCompound();
            slotRotations.forEach((slot, rotation) -> {
                if (rotation != 0 && slotIndexes.contains(slot)) {
                    rotations.setInteger(String.valueOf(slot), rotation);
                }
            });
            categoryNbt.setTag(ROTATIONS_TAG, rotations);
        }
        saveNbt.accept(categoryNbt);
    }

    private void updateDisplay() {
        List<DisplayedItem> shown = new ArrayList<>();
        IItemHandler handler = inventory.get();
        for (int slot : slotIndexes) {
            if (slot < 0 || slot >= handler.getSlots()) {
                continue;
            }
            ItemStack item = handler.getStackInSlot(slot).copy();
            if (item.isEmpty() && memory != null && memory.get() != null) {
                item = memory.get().getSlotFilterStack(slot);
            }
            if (!item.isEmpty()) {
                item.setCount(1);
                shown.add(new DisplayedItem(item, getRotation(slot), slot, displaySide));
            }
        }
        onDisplayChanged.accept(shown);
    }

    public int getItemNumberLimit() {
        return itemNumberLimit;
    }

    /**
     * Shows this slot too. With room for one item it moves the display here instead; when all the
     * room is taken it does nothing, like the original.
     */
    public void selectSlot(int slot) {
        if (slotIndexes.contains(slot)) {
            return;
        }
        if (itemNumberLimit == 1) {
            slotIndexes.clear();
        } else if (slotIndexes.size() >= itemNumberLimit) {
            return;
        }
        slotIndexes.add(slot);
        serialize();
        updateDisplay();
    }

    public void unselectSlot(int slot) {
        if (slotIndexes.remove(Integer.valueOf(slot))) {
            slotRotations.remove(slot);
            serialize();
            updateDisplay();
        }
    }

    /** Shown slots move along, as far as the other storage has room for more shown items. */
    @Override
    public void copySlotSettingsTo(ISettingsCategory target, int startFromSlot, int slotOffset) {
        ItemDisplaySettingsCategory other = (ItemDisplaySettingsCategory) target;
        boolean changed = false;
        for (int slot : slotIndexes) {
            int targetSlot = slot + slotOffset;
            if (slot >= startFromSlot && !other.slotIndexes.contains(targetSlot) && other.slotIndexes.size() < other.itemNumberLimit) {
                other.slotIndexes.add(targetSlot);
                other.slotRotations.put(targetSlot, getRotation(slot));
                changed = true;
            }
        }
        if (changed) {
            other.serialize();
            other.updateDisplay();
        }
    }

    @Override
    public void deleteSlotSettingsFrom(int slot) {
        if (slotIndexes.removeIf(s -> s >= slot)) {
            slotRotations.keySet().removeIf(s -> s >= slot);
            serialize();
            updateDisplay();
        }
    }

    public List<Integer> getSlots() {
        return Collections.unmodifiableList(slotIndexes);
    }

    public boolean isSlotSelected(int slot) {
        return slotIndexes.contains(slot);
    }

    /** The first shown slot, or -1. */
    public int getSlot() {
        return slotIndexes.isEmpty() ? -1 : slotIndexes.get(0);
    }

    public int getRotation(int slot) {
        return slotRotations.getOrDefault(slot, 0);
    }

    public void rotate(int slot, boolean clockwise) {
        if (!slotIndexes.contains(slot)) {
            return;
        }
        slotRotations.put(slot, (getRotation(slot) + (clockwise ? 45 : -45) + 360) % 360);
        serialize();
        updateDisplay();
    }

    public void setColor(EnumDyeColor color) {
        this.color = color;
        categoryNbt.setInteger(COLOR_TAG, color.getMetadata());
        saveNbt.accept(categoryNbt);
    }

    public EnumDyeColor getColor() {
        return color;
    }

    public DisplaySide getDisplaySide() {
        return displaySide;
    }

    /** Which face shows the items; only storages that allow it offer the choice (double chests). */
    public void setDisplaySide(DisplaySide displaySide) {
        if (this.displaySide == displaySide) {
            return;
        }
        this.displaySide = displaySide;
        if (displaySide == DisplaySide.FRONT) {
            categoryNbt.removeTag(DISPLAY_SIDE_TAG);
        } else {
            categoryNbt.setString(DISPLAY_SIDE_TAG, displaySide.getName());
        }
        saveNbt.accept(categoryNbt);
        updateDisplay();
    }

    /** The item in each shown slot (one-item copies; empty slots left out), taken before a sort for {@link #followItems}. */
    public Map<Integer, ItemStack> getShownSlotItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        IItemHandler handler = inventory.get();
        for (int slot : slotIndexes) {
            if (slot >= 0 && slot < handler.getSlots() && !handler.getStackInSlot(slot).isEmpty()) {
                ItemStack item = handler.getStackInSlot(slot).copy();
                item.setCount(1);
                items.put(slot, item);
            }
        }
        return items;
    }

    /**
     * After a sort: each shown slot whose item moved follows it to a slot now holding it, keeping
     * its rotation and its place in the display order. Deviation: the original keeps the slot, so
     * sorting changed what a storage shows.
     */
    public void followItems(Map<Integer, ItemStack> itemsBefore) {
        IItemHandler handler = inventory.get();
        List<Integer> newSlots = new ArrayList<>(slotIndexes);
        java.util.Set<Integer> taken = new java.util.HashSet<>();
        List<Integer> moved = new ArrayList<>();
        for (int i = 0; i < slotIndexes.size(); i++) {
            int slot = slotIndexes.get(i);
            ItemStack before = itemsBefore.getOrDefault(slot, ItemStack.EMPTY);
            if (before.isEmpty() || holds(handler, slot, before)) {
                taken.add(slot);
            } else {
                moved.add(i);
            }
        }
        if (moved.isEmpty()) {
            return;
        }
        for (int i : moved) {
            int slot = slotIndexes.get(i);
            ItemStack before = itemsBefore.get(slot);
            for (int candidate = 0; candidate < handler.getSlots(); candidate++) {
                if (!taken.contains(candidate) && holds(handler, candidate, before)) {
                    newSlots.set(i, candidate);
                    break;
                }
            }
            taken.add(newSlots.get(i));
        }
        Map<Integer, Integer> newRotations = new HashMap<>();
        for (int i = 0; i < newSlots.size(); i++) {
            newRotations.put(newSlots.get(i), slotRotations.getOrDefault(slotIndexes.get(i), 0));
        }
        slotIndexes.clear();
        slotIndexes.addAll(newSlots);
        slotRotations.clear();
        slotRotations.putAll(newRotations);
        serialize();
        updateDisplay();
    }

    private static boolean holds(IItemHandler handler, int slot, ItemStack item) {
        return slot < handler.getSlots() && net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack(handler.getStackInSlot(slot), item);
    }

    /** Inventory listener: a shown slot's item changed. */
    public void itemChanged(int changedSlot) {
        if (slotIndexes.contains(changedSlot)) {
            updateDisplay();
        }
    }

    @Nullable
    @Override
    public Integer getSlotColor(int slotNumber) {
        return slotIndexes.contains(slotNumber) ? SettingsColors.rgb(color) : null;
    }

    @Override
    public void reloadFrom(NBTTagCompound categoryNbt) {
        this.categoryNbt = categoryNbt;
        deserialize();
    }

    /** One item shown on the storage: a one-item copy, its rotation, the slot it comes from and the face it is on. */
    public static final class DisplayedItem {
        private final ItemStack item;
        private final int rotation;
        private final int slot;
        private final DisplaySide displaySide;

        public DisplayedItem(ItemStack item, int rotation, int slot) {
            this(item, rotation, slot, DisplaySide.FRONT);
        }

        public DisplayedItem(ItemStack item, int rotation, int slot, DisplaySide displaySide) {
            this.item = item;
            this.rotation = rotation;
            this.slot = slot;
            this.displaySide = displaySide;
        }

        public DisplaySide getDisplaySide() {
            return displaySide;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getRotation() {
            return rotation;
        }

        public int getSlot() {
            return slot;
        }
    }
}
