/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Small helpers for reading and writing settings on an ItemStack, optionally under a parent
 * compound (an empty parent key means the stack's root tag).
 */
public final class NBTHelper {
    private NBTHelper() {}

    @Nullable
    public static NBTTagCompound getTag(ItemStack stack, String parentKey) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || parentKey.isEmpty()) {
            return tag;
        }
        return tag.hasKey(parentKey) ? tag.getCompoundTag(parentKey) : null;
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack, String parentKey) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        if (parentKey.isEmpty()) {
            return tag;
        }
        if (!tag.hasKey(parentKey)) {
            tag.setTag(parentKey, new NBTTagCompound());
        }
        return tag.getCompoundTag(parentKey);
    }

    public static boolean getBoolean(ItemStack stack, String key, boolean defaultValue) {
        return getBoolean(stack, "", key, defaultValue);
    }

    public static boolean getBoolean(ItemStack stack, String parentKey, String key, boolean defaultValue) {
        NBTTagCompound tag = getTag(stack, parentKey);
        return tag != null && tag.hasKey(key) ? tag.getBoolean(key) : defaultValue;
    }

    public static void setBoolean(ItemStack stack, String key, boolean value) {
        setBoolean(stack, "", key, value);
    }

    public static void setBoolean(ItemStack stack, String parentKey, String key, boolean value) {
        getOrCreateTag(stack, parentKey).setBoolean(key, value);
    }

    public static int getInt(ItemStack stack, String key, int defaultValue) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key) ? tag.getInteger(key) : defaultValue;
    }

    public static void setInt(ItemStack stack, String key, int value) {
        getOrCreateTag(stack, "").setInteger(key, value);
    }

    public static long getLong(ItemStack stack, String key, long defaultValue) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key) ? tag.getLong(key) : defaultValue;
    }

    public static void setLong(ItemStack stack, String key, long value) {
        getOrCreateTag(stack, "").setLong(key, value);
    }

    public static void removeTag(ItemStack stack, String key) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            tag.removeTag(key);
        }
    }

    public static <T extends Enum<T> & INamedEnum> T getEnum(ItemStack stack, String key, Function<String, T> fromName, T defaultValue) {
        return getEnum(stack, "", key, fromName, defaultValue);
    }

    public static <T extends Enum<T> & INamedEnum> T getEnum(ItemStack stack, String parentKey, String key, Function<String, T> fromName, T defaultValue) {
        NBTTagCompound tag = getTag(stack, parentKey);
        return tag != null && tag.hasKey(key) ? fromName.apply(tag.getString(key)) : defaultValue;
    }

    public static <T extends Enum<T> & INamedEnum> void setEnum(ItemStack stack, String key, T value) {
        setEnum(stack, "", key, value);
    }

    public static <T extends Enum<T> & INamedEnum> void setEnum(ItemStack stack, String parentKey, String key, T value) {
        getOrCreateTag(stack, parentKey).setString(key, value.getSerializedName());
    }

    public static NBTTagCompound withBoolean(String key, boolean value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(key, value);
        return tag;
    }

    public static NBTTagCompound withString(String key, String value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(key, value);
        return tag;
    }

    public static NBTTagCompound withInt(String key, int value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(key, value);
        return tag;
    }
}
