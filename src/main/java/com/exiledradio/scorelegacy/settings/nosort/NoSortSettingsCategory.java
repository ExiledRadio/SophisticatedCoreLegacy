/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.nosort;

import com.exiledradio.scorelegacy.settings.ISettingsCategory;
import com.exiledradio.scorelegacy.settings.ISlotColorCategory;
import com.exiledradio.scorelegacy.settings.SettingsColors;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/** Slots that the sort button leaves alone. */
public class NoSortSettingsCategory implements ISettingsCategory, ISlotColorCategory {
    public static final String NAME = "no_sort";
    private static final String COLOR_TAG = "color";
    private static final String SELECTED_SLOTS_TAG = "selectedSlots";

    private NBTTagCompound categoryNbt;
    private final Consumer<NBTTagCompound> saveNbt;
    private final Set<Integer> selectedSlots = new TreeSet<>();
    private EnumDyeColor color = EnumDyeColor.LIME;

    public NoSortSettingsCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
        this.categoryNbt = categoryNbt;
        this.saveNbt = saveNbt;
        deserialize();
    }

    private void deserialize() {
        for (int slot : categoryNbt.getIntArray(SELECTED_SLOTS_TAG)) {
            selectedSlots.add(slot);
        }
        if (categoryNbt.hasKey(COLOR_TAG)) {
            color = EnumDyeColor.byMetadata(categoryNbt.getInteger(COLOR_TAG));
        }
    }

    public boolean isSlotSelected(int slot) {
        return selectedSlots.contains(slot);
    }

    public void unselectAllSlots() {
        selectedSlots.clear();
        serializeSelectedSlots();
    }

    /** Selects slots from minSlot (inclusive) to maxSlot (exclusive). */
    public void selectSlots(int minSlot, int maxSlot) {
        for (int slot = minSlot; slot < maxSlot; slot++) {
            selectedSlots.add(slot);
        }
        serializeSelectedSlots();
    }

    public void selectSlot(int slot) {
        selectSlots(slot, slot + 1);
    }

    public void unselectSlot(int slot) {
        selectedSlots.remove(slot);
        serializeSelectedSlots();
    }

    private void serializeSelectedSlots() {
        int[] slots = new int[selectedSlots.size()];
        int i = 0;
        for (int slot : selectedSlots) {
            slots[i++] = slot;
        }
        categoryNbt.setIntArray(SELECTED_SLOTS_TAG, slots);
        saveNbt.accept(categoryNbt);
    }

    @Override
    public void copySlotSettingsTo(ISettingsCategory target, int startFromSlot, int slotOffset) {
        NoSortSettingsCategory other = (NoSortSettingsCategory) target;
        for (int slot : selectedSlots) {
            if (slot >= startFromSlot) {
                other.selectedSlots.add(slot + slotOffset);
            }
        }
        other.serializeSelectedSlots();
    }

    @Override
    public void deleteSlotSettingsFrom(int slot) {
        if (selectedSlots.removeIf(s -> s >= slot)) {
            serializeSelectedSlots();
        }
    }

    public void setColor(EnumDyeColor color) {
        this.color = color;
        categoryNbt.setInteger(COLOR_TAG, color.getMetadata());
        saveNbt.accept(categoryNbt);
    }

    public EnumDyeColor getColor() {
        return color;
    }

    @Nullable
    @Override
    public Integer getSlotColor(int slotNumber) {
        return selectedSlots.contains(slotNumber) ? SettingsColors.rgb(color) : null;
    }

    public Set<Integer> getNoSortSlots() {
        return Collections.unmodifiableSet(selectedSlots);
    }

    @Override
    public void reloadFrom(NBTTagCompound categoryNbt) {
        this.categoryNbt = categoryNbt;
        selectedSlots.clear();
        color = EnumDyeColor.LIME;
        deserialize();
    }
}
