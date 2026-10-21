/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Filter settings stored on the upgrade stack. 1.16's item tags become ore dictionary names
 * here, which is what 1.12 mods use for the same purpose.
 */
public class FilterLogicBase {
    private static final String TAGS_KEY = "tags";

    protected final ItemStack upgrade;
    protected final Consumer<ItemStack> saveHandler;
    protected final String parentTagKey;
    private boolean allowListDefault = false;
    @Nullable
    protected Set<String> tagNames = null;

    public FilterLogicBase(ItemStack upgrade, Consumer<ItemStack> saveHandler, String parentTagKey) {
        this.upgrade = upgrade;
        this.saveHandler = saveHandler;
        this.parentTagKey = parentTagKey;
    }

    /** Where this filter keeps its settings in the upgrade's NBT ("" for the root). */
    public String getParentTagKey() {
        return parentTagKey;
    }

    public void setAllowByDefault() {
        allowListDefault = true;
    }

    protected void save() {
        saveHandler.accept(upgrade);
    }

    public boolean stackMatchesFilter(ItemStack stack, ItemStack filter) {
        if (filter.isEmpty()) {
            return false;
        }

        PrimaryMatch primaryMatch = getPrimaryMatch();
        if (primaryMatch == PrimaryMatch.MOD) {
            //noinspection ConstantConditions - registered items always have a registry name
            if (!stack.getItem().getRegistryName().getNamespace().equals(filter.getItem().getRegistryName().getNamespace())) {
                return false;
            }
        } else if (primaryMatch == PrimaryMatch.ITEM && !isSameItem(stack, filter)) {
            return false;
        }

        if (shouldMatchDurability() && stack.isItemStackDamageable() && stack.getItemDamage() != filter.getItemDamage()) {
            return false;
        }

        return !shouldMatchNbt() || ItemStack.areItemStackTagsEqual(stack, filter);
    }

    /** Same item, and same metadata for items whose metadata is a variant (wool colour, dye...). */
    public static boolean isSameItem(ItemStack stack, ItemStack other) {
        return stack.getItem() == other.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == other.getMetadata());
    }

    public static Set<String> getOreNames(ItemStack stack) {
        Set<String> names = new TreeSet<>();
        if (!stack.isEmpty()) {
            for (int id : OreDictionary.getOreIDs(stack)) {
                names.add(OreDictionary.getOreName(id));
            }
        }
        return names;
    }

    public Set<String> getTagNames() {
        if (tagNames == null) {
            initTags();
        }
        return Collections.unmodifiableSet(tagNames);
    }

    public void addTagName(String tagName) {
        if (tagNames == null) {
            initTags();
        }
        tagNames.add(tagName);
        serializeTags();
    }

    public void removeTagName(String tagName) {
        if (tagNames == null) {
            initTags();
        }
        tagNames.remove(tagName);
        serializeTags();
    }

    private void serializeTags() {
        if (tagNames == null) {
            return;
        }
        NBTTagList list = new NBTTagList();
        for (String tagName : tagNames) {
            list.appendTag(new NBTTagString(tagName));
        }
        NBTHelper.getOrCreateTag(upgrade, parentTagKey).setTag(TAGS_KEY, list);
        save();
    }

    protected void initTags() {
        tagNames = new TreeSet<>();
        NBTTagCompound tag = NBTHelper.getTag(upgrade, parentTagKey);
        if (tag != null) {
            NBTTagList list = tag.getTagList(TAGS_KEY, Constants.NBT.TAG_STRING);
            for (int i = 0; i < list.tagCount(); i++) {
                tagNames.add(list.getStringTagAt(i));
            }
        }
    }

    public void setAllowList(boolean isAllowList) {
        NBTHelper.setBoolean(upgrade, parentTagKey, "isAllowList", isAllowList);
        save();
    }

    public boolean isAllowList() {
        return NBTHelper.getBoolean(upgrade, parentTagKey, "isAllowList", allowListDefault);
    }

    public boolean shouldMatchDurability() {
        return NBTHelper.getBoolean(upgrade, parentTagKey, "matchDurability", false);
    }

    public void setMatchDurability(boolean matchDurability) {
        NBTHelper.setBoolean(upgrade, parentTagKey, "matchDurability", matchDurability);
        save();
    }

    public boolean shouldMatchNbt() {
        return NBTHelper.getBoolean(upgrade, parentTagKey, "matchNbt", false);
    }

    public void setMatchNbt(boolean matchNbt) {
        NBTHelper.setBoolean(upgrade, parentTagKey, "matchNbt", matchNbt);
        save();
    }

    public PrimaryMatch getPrimaryMatch() {
        return NBTHelper.getEnum(upgrade, parentTagKey, "primaryMatch", PrimaryMatch::fromName, PrimaryMatch.ITEM);
    }

    public void setPrimaryMatch(PrimaryMatch primaryMatch) {
        NBTHelper.setEnum(upgrade, parentTagKey, "primaryMatch", primaryMatch);
        save();
    }

    public boolean shouldMatchAnyTag() {
        return NBTHelper.getBoolean(upgrade, parentTagKey, "matchAnyTag", true);
    }

    public void setMatchAnyTag(boolean matchAnyTag) {
        NBTHelper.setBoolean(upgrade, parentTagKey, "matchAnyTag", matchAnyTag);
        save();
    }
}
