/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.main;

import com.exiledradio.scorelegacy.settings.ISettingsCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;

/**
 * General behaviour settings. Each one has a player-level value (kept with the player, the
 * default for all their storages) that a storage can override for itself. The player-level tag
 * lives under Forge's persisted player NBT, so it survives death.
 */
public class MainSettingsCategory implements ISettingsCategory {
    public static final String NAME = "main";

    public static final Setting SHIFT_CLICK_INTO_OPEN_TAB_FIRST = new Setting("shiftClickOpenTab", true);
    public static final Setting KEEP_TAB_OPEN = new Setting("keepTabOpen", true);
    public static final Setting[] SETTINGS = {SHIFT_CLICK_INTO_OPEN_TAB_FIRST, KEEP_TAB_OPEN};

    private final String playerSettingsTagName;
    private NBTTagCompound categoryNbt;
    private final Consumer<NBTTagCompound> saveNbt;

    public MainSettingsCategory(String playerSettingsTagName, NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
        this.playerSettingsTagName = playerSettingsTagName;
        this.categoryNbt = categoryNbt;
        this.saveNbt = saveNbt;
    }

    public String getPlayerSettingsTagName() {
        return playerSettingsTagName;
    }

    /** This storage's value if it overrides the setting, else the player's, else the default. */
    public boolean getValue(EntityPlayer player, Setting setting) {
        if (categoryNbt.hasKey(setting.name)) {
            return categoryNbt.getBoolean(setting.name);
        }
        return getPlayerValue(player, setting);
    }

    public boolean getPlayerValue(EntityPlayer player, Setting setting) {
        NBTTagCompound playerTag = getPlayerSettingsTag(player);
        return playerTag.hasKey(setting.name) ? playerTag.getBoolean(setting.name) : setting.defaultValue;
    }

    public void setPlayerValue(EntityPlayer player, Setting setting, boolean value) {
        NBTTagCompound persisted = getPersistedTag(player);
        NBTTagCompound playerTag = persisted.getCompoundTag(playerSettingsTagName);
        if (value == setting.defaultValue) {
            playerTag.removeTag(setting.name);
        } else {
            playerTag.setBoolean(setting.name, value);
        }
        persisted.setTag(playerSettingsTagName, playerTag);
    }

    /** Overrides the setting for this storage; matching the player's value removes the override. */
    public void setStorageValue(EntityPlayer player, Setting setting, boolean value) {
        if (value == getPlayerValue(player, setting)) {
            categoryNbt.removeTag(setting.name);
        } else {
            categoryNbt.setBoolean(setting.name, value);
        }
        saveNbt.accept(categoryNbt);
    }

    public NBTTagCompound getPlayerSettingsTag(EntityPlayer player) {
        return getPersistedTag(player).getCompoundTag(playerSettingsTagName);
    }

    /** Client side: the server's copy of the player's settings (the client never gets player NBT otherwise). */
    public void setPlayerSettingsTag(EntityPlayer player, NBTTagCompound tag) {
        getPersistedTag(player).setTag(playerSettingsTagName, tag);
    }

    private static NBTTagCompound getPersistedTag(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        }
        return data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    @Override
    public void reloadFrom(NBTTagCompound categoryNbt) {
        this.categoryNbt = categoryNbt;
    }

    public static final class Setting {
        private final String name;
        private final boolean defaultValue;

        private Setting(String name, boolean defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }
    }
}
