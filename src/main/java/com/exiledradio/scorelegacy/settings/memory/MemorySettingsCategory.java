/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.memory;

import com.exiledradio.scorelegacy.settings.ISettingsCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Slots that remember what was in them and then only take that item. In 1.12 an "item" is the
 * item plus its metadata (white wool and red wool differ), except for damageable items, where the
 * metadata is durability and any damage matches.
 */
public class MemorySettingsCategory implements ISettingsCategory {
    public static final String NAME = "memory";
    private static final String SLOT_FILTER_ITEMS_TAG = "slotFilterItems";

    private final Supplier<IItemHandler> inventory;
    private NBTTagCompound categoryNbt;
    private final Consumer<NBTTagCompound> saveNbt;
    private final Map<Integer, Memorized> slotFilterItems = new TreeMap<>();
    private final Map<Memorized, Set<Integer>> filterItemSlots = new HashMap<>();

    public MemorySettingsCategory(Supplier<IItemHandler> inventory, NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
        this.inventory = inventory;
        this.categoryNbt = categoryNbt;
        this.saveNbt = saveNbt;
        deserialize();
    }

    private void deserialize() {
        NBTTagCompound items = categoryNbt.getCompoundTag(SLOT_FILTER_ITEMS_TAG);
        for (String key : items.getKeySet()) {
            Memorized memorized = Memorized.fromString(items.getString(key));
            if (memorized != null) {
                try {
                    addSlotItem(Integer.parseInt(key), memorized);
                } catch (NumberFormatException e) {
                    //skip a damaged entry
                }
            }
        }
    }

    /** True when the slot isn't a memory slot or the stack is what it remembers. */
    public boolean matchesFilter(int slot, ItemStack stack) {
        Memorized memorized = slotFilterItems.get(slot);
        return memorized == null || (!stack.isEmpty() && memorized.matches(stack));
    }

    /** What the slot remembers, as a one-item stack for drawing, or EMPTY. */
    public ItemStack getSlotFilterStack(int slot) {
        Memorized memorized = slotFilterItems.get(slot);
        return memorized == null ? ItemStack.EMPTY : memorized.toStack();
    }

    public boolean isSlotSelected(int slot) {
        return slotFilterItems.containsKey(slot);
    }

    /** Memory slots that take this stack, lowest first. */
    public Set<Integer> getSlotsFor(ItemStack stack) {
        if (stack.isEmpty() || filterItemSlots.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> slots = filterItemSlots.get(Memorized.of(stack));
        return slots == null ? Collections.emptySet() : slots;
    }

    public boolean isMemorized(ItemStack stack) {
        return !getSlotsFor(stack).isEmpty();
    }

    public void unselectAllSlots() {
        slotFilterItems.clear();
        filterItemSlots.clear();
        serializeFilterItems();
    }

    /** Memorizes what is in the slots from minSlot (inclusive) to maxSlot (exclusive); empty slots are skipped. */
    public void selectSlots(int minSlot, int maxSlot) {
        IItemHandler handler = inventory.get();
        for (int slot = minSlot; slot < maxSlot && slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                removeSlot(slot);
                addSlotItem(slot, Memorized.of(stack));
            }
        }
        serializeFilterItems();
    }

    public void selectSlot(int slot) {
        selectSlots(slot, slot + 1);
    }

    public void unselectSlot(int slot) {
        removeSlot(slot);
        serializeFilterItems();
    }

    /** Memorizes a given item for the slot, whatever is in it (JEI drag into the memory tab). */
    public void setFilter(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        removeSlot(slot);
        addSlotItem(slot, Memorized.of(stack));
        serializeFilterItems();
    }

    private void addSlotItem(int slot, Memorized memorized) {
        slotFilterItems.put(slot, memorized);
        filterItemSlots.computeIfAbsent(memorized, k -> new TreeSet<>()).add(slot);
    }

    private void removeSlot(int slot) {
        Memorized memorized = slotFilterItems.remove(slot);
        if (memorized != null) {
            Set<Integer> slots = filterItemSlots.get(memorized);
            slots.remove(slot);
            if (slots.isEmpty()) {
                filterItemSlots.remove(memorized);
            }
        }
    }

    private void serializeFilterItems() {
        NBTTagCompound items = new NBTTagCompound();
        slotFilterItems.forEach((slot, memorized) -> items.setString(String.valueOf(slot), memorized.toString()));
        categoryNbt.setTag(SLOT_FILTER_ITEMS_TAG, items);
        saveNbt.accept(categoryNbt);
    }

    public Set<Integer> getSlotIndexes() {
        return Collections.unmodifiableSet(slotFilterItems.keySet());
    }

    @Override
    public void copySlotSettingsTo(ISettingsCategory target, int startFromSlot, int slotOffset) {
        MemorySettingsCategory other = (MemorySettingsCategory) target;
        slotFilterItems.forEach((slot, memorized) -> {
            if (slot >= startFromSlot) {
                other.removeSlot(slot + slotOffset);
                other.addSlotItem(slot + slotOffset, memorized);
            }
        });
        other.serializeFilterItems();
    }

    @Override
    public void deleteSlotSettingsFrom(int slot) {
        boolean changed = false;
        for (int memorySlot : new java.util.ArrayList<>(slotFilterItems.keySet())) {
            if (memorySlot >= slot) {
                removeSlot(memorySlot);
                changed = true;
            }
        }
        if (changed) {
            serializeFilterItems();
        }
    }

    @Override
    public void reloadFrom(NBTTagCompound categoryNbt) {
        this.categoryNbt = categoryNbt;
        slotFilterItems.clear();
        filterItemSlots.clear();
        deserialize();
    }

    /** An item and, unless it is damageable, its metadata. Saved as "modid:name@meta". */
    private static final class Memorized {
        private final Item item;
        private final int meta;

        private Memorized(Item item, int meta) {
            this.item = item;
            this.meta = meta;
        }

        static Memorized of(ItemStack stack) {
            return new Memorized(stack.getItem(), stack.isItemStackDamageable() ? 0 : stack.getMetadata());
        }

        @Nullable
        static Memorized fromString(String value) {
            int at = value.lastIndexOf('@');
            String name = at < 0 ? value : value.substring(0, at);
            Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
            if (item == null) {
                return null;
            }
            int meta = 0;
            if (at >= 0) {
                try {
                    meta = Integer.parseInt(value.substring(at + 1));
                } catch (NumberFormatException e) {
                    meta = 0;
                }
            }
            return new Memorized(item, meta);
        }

        boolean matches(ItemStack stack) {
            return stack.getItem() == item && (stack.isItemStackDamageable() || stack.getMetadata() == meta);
        }

        ItemStack toStack() {
            return new ItemStack(item, 1, meta);
        }

        @Override
        public String toString() {
            return Objects.requireNonNull(item.getRegistryName()) + "@" + meta;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Memorized)) {
                return false;
            }
            Memorized other = (Memorized) o;
            return item == other.item && meta == other.meta;
        }

        @Override
        public int hashCode() {
            return 31 * item.hashCode() + meta;
        }
    }
}
