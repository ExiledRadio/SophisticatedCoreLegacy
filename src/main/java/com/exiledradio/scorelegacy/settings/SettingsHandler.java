/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * All settings categories of one storage, saved under "settings" in its contents NBT. Which
 * categories exist is up to the storage (see IStorageWrapper#addSettingsCategories); the order
 * they are added is the order of their tabs in the settings screen.
 */
public class SettingsHandler {
    public static final String SETTINGS_TAG = "settings";

    private final NBTTagCompound contentsNbt;
    private final Runnable markContentsDirty;
    private final Map<String, ISettingsCategory> settingsCategories = new LinkedHashMap<>();
    private final Map<Class<?>, ISettingsCategory> typeCategories = new HashMap<>();
    private final Map<Class<?>, List<?>> interfaceCategories = new HashMap<>();

    public SettingsHandler(NBTTagCompound contentsNbt, Runnable markContentsDirty) {
        this.contentsNbt = contentsNbt;
        this.markContentsDirty = markContentsDirty;
    }

    /**
     * Adds a category. The factory gets the category's saved NBT and a consumer to call with the
     * changed NBT whenever the category changes.
     */
    public void addCategory(String name, BiFunction<NBTTagCompound, Consumer<NBTTagCompound>, ISettingsCategory> factory) {
        NBTTagCompound settingsNbt = contentsNbt.getCompoundTag(SETTINGS_TAG);
        ISettingsCategory category = factory.apply(settingsNbt.getCompoundTag(name), categoryNbt -> {
            NBTTagCompound current = contentsNbt.getCompoundTag(SETTINGS_TAG);
            current.setTag(name, categoryNbt);
            contentsNbt.setTag(SETTINGS_TAG, current);
            markContentsDirty.run();
        });
        settingsCategories.put(name, category);
        typeCategories.put(category.getClass(), category);
        interfaceCategories.clear();
    }

    public Map<String, ISettingsCategory> getSettingsCategories() {
        return Collections.unmodifiableMap(settingsCategories);
    }

    @Nullable
    public <T extends ISettingsCategory> T getTypeCategory(Class<T> categoryClass) {
        //noinspection unchecked - put in by addCategory under the instance's own class
        return (T) typeCategories.get(categoryClass);
    }

    public <T> List<T> getCategoriesThatImplement(Class<T> categoryInterface) {
        //noinspection unchecked
        return (List<T>) interfaceCategories.computeIfAbsent(categoryInterface, c -> {
            List<Object> ret = new ArrayList<>();
            for (ISettingsCategory category : settingsCategories.values()) {
                if (c.isInstance(category)) {
                    ret.add(category);
                }
            }
            return ret;
        });
    }

    public NBTTagCompound getNbt() {
        return contentsNbt.getCompoundTag(SETTINGS_TAG);
    }

    /** Copies per-slot settings to another storage's handler (see {@link ISettingsCategory#copySlotSettingsTo}). */
    public void copySlotSettingsTo(SettingsHandler target, int startFromSlot, int slotOffset) {
        settingsCategories.forEach((name, category) -> {
            ISettingsCategory other = target.settingsCategories.get(name);
            if (other != null && other.getClass() == category.getClass()) {
                category.copySlotSettingsTo(other, startFromSlot, slotOffset);
            }
        });
    }

    public void deleteSlotSettingsFrom(int slot) {
        for (ISettingsCategory category : settingsCategories.values()) {
            category.deleteSlotSettingsFrom(slot);
        }
    }

    /** Client side: takes the server's copy of all settings. */
    public void reloadFrom(NBTTagCompound settingsNbt) {
        contentsNbt.setTag(SETTINGS_TAG, settingsNbt.copy());
        NBTTagCompound saved = contentsNbt.getCompoundTag(SETTINGS_TAG);
        settingsCategories.forEach((name, category) -> category.reloadFrom(saved.getCompoundTag(name)));
    }
}
