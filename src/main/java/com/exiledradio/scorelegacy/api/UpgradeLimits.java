/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Per-storage-type upgrade limits (the original's IUpgradeCountLimitConfig and
 * IUpgradeItem#checkUpgradePerStorageTypeLimit). Each mod registers its config; a storage whose
 * {@link IStorageWrapper#getStorageType()} is empty (backpacks) has no limits.
 */
public final class UpgradeLimits {
    private static final List<IUpgradeCountLimitConfig> CONFIGS = new ArrayList<>();

    private UpgradeLimits() {}

    public static void register(IUpgradeCountLimitConfig config) {
        CONFIGS.add(config);
    }

    public interface IUpgradeCountLimitConfig {
        /** How many of this upgrade a storage of the type may hold; Integer.MAX_VALUE for no limit. */
        int getMaxUpgradesPerStorage(String storageType, @Nullable ResourceLocation upgradeName);

        /** How many upgrades of the group a storage of the type may hold; Integer.MAX_VALUE for no limit. */
        int getMaxUpgradesInGroupPerStorage(String storageType, UpgradeGroup group);
    }

    /** Whether one more of this upgrade fits the storage's limits. */
    public static UpgradeSlotChangeResult check(IUpgradeItem<?> upgrade, IStorageWrapper storageWrapper) {
        if (!(upgrade instanceof com.exiledradio.scorelegacy.upgrades.infinity.InfinityUpgradeItem)
                && com.exiledradio.scorelegacy.upgrades.infinity.InfinityUpgradeItem.hasInfinityUpgrade(storageWrapper.getUpgradeHandler())) {
            // An infinity upgrade must be the storage's only upgrade (the original's upgrade conflict).
            return UpgradeSlotChangeResult.fail("add.no_upgrade_can_be_added", Collections.emptySet(), Collections.emptySet());
        }
        String storageType = storageWrapper.getStorageType();
        if (storageType.isEmpty() || !(upgrade instanceof Item)) {
            return UpgradeSlotChangeResult.SUCCESS;
        }
        Item item = (Item) upgrade;
        int perStorage = getMaxPerStorage(storageType, item.getRegistryName());
        int inGroup = getMaxInGroup(storageType, upgrade.getUpgradeGroup());
        if (perStorage == Integer.MAX_VALUE && inGroup == Integer.MAX_VALUE) {
            return UpgradeSlotChangeResult.SUCCESS;
        }
        String itemName = item.getTranslationKey() + ".name";
        if (perStorage == 0) {
            return UpgradeSlotChangeResult.fail("add.upgrade_not_allowed", Collections.emptySet(), Collections.emptySet(), itemName);
        }
        if (inGroup == 0) {
            return UpgradeSlotChangeResult.fail("add.upgrade_not_allowed", Collections.emptySet(), Collections.emptySet(), upgrade.getUpgradeGroup().getTranslationKey());
        }
        IItemHandler upgrades = storageWrapper.getUpgradeHandler();
        Set<Integer> sameUpgrade = new HashSet<>();
        Set<Integer> sameGroup = new HashSet<>();
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (stack.getItem() == item) {
                sameUpgrade.add(slot);
            }
            if (stack.getItem() instanceof IUpgradeItem && ((IUpgradeItem<?>) stack.getItem()).getUpgradeGroup() == upgrade.getUpgradeGroup()) {
                sameGroup.add(slot);
            }
        }
        if (sameUpgrade.size() >= perStorage) {
            return UpgradeSlotChangeResult.fail("add.only_x_upgrades_allowed", sameUpgrade, Collections.emptySet(), perStorage, itemName);
        }
        if (sameGroup.size() >= inGroup) {
            return UpgradeSlotChangeResult.fail("add.only_x_upgrades_allowed", sameGroup, Collections.emptySet(), inGroup, upgrade.getUpgradeGroup().getTranslationKey());
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }

    /**
     * Swapping {@code current} in a slot for {@code toPut}: the limits of what goes in, unless it
     * replaces an upgrade that already counted towards the same limit (the original's rules).
     */
    public static UpgradeSlotChangeResult checkSwap(IUpgradeItem<?> current, ItemStack toPut, IStorageWrapper storageWrapper) {
        if (!(toPut.getItem() instanceof IUpgradeItem) || storageWrapper.getStorageType().isEmpty()) {
            return UpgradeSlotChangeResult.SUCCESS;
        }
        IUpgradeItem<?> upgradeToPut = (IUpgradeItem<?>) toPut.getItem();
        String storageType = storageWrapper.getStorageType();
        int perStorage = getMaxPerStorage(storageType, toPut.getItem().getRegistryName());
        int inGroup = getMaxInGroup(storageType, upgradeToPut.getUpgradeGroup());
        boolean sameLimit = perStorage < inGroup ? toPut.getItem() == current : upgradeToPut.getUpgradeGroup() == current.getUpgradeGroup()
                && !upgradeToPut.getUpgradeGroup().isSolo();
        return sameLimit ? UpgradeSlotChangeResult.SUCCESS : check(upgradeToPut, storageWrapper);
    }

    private static int getMaxPerStorage(String storageType, @Nullable ResourceLocation upgradeName) {
        int max = Integer.MAX_VALUE;
        for (IUpgradeCountLimitConfig config : CONFIGS) {
            max = Math.min(max, config.getMaxUpgradesPerStorage(storageType, upgradeName));
        }
        return max;
    }

    private static int getMaxInGroup(String storageType, UpgradeGroup group) {
        if (group.isSolo()) {
            return Integer.MAX_VALUE;
        }
        int max = Integer.MAX_VALUE;
        for (IUpgradeCountLimitConfig config : CONFIGS) {
            max = Math.min(max, config.getMaxUpgradesInGroupPerStorage(storageType, group));
        }
        return max;
    }
}
