/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.function.Consumer;

public abstract class UpgradeWrapperBase<W extends IUpgradeWrapper, T extends UpgradeItemBase<W>> implements IUpgradeWrapper {
    protected final IStorageWrapper storageWrapper;
    protected final Consumer<ItemStack> upgradeSaveHandler;
    protected final ItemStack upgrade;
    protected final T upgradeItem;

    private long cooldown = 0;

    protected UpgradeWrapperBase(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        this.storageWrapper = storageWrapper;
        this.upgrade = upgrade;
        //noinspection unchecked - the upgrade type that created this wrapper belongs to this item
        upgradeItem = (T) upgrade.getItem();
        this.upgradeSaveHandler = upgradeSaveHandler;
    }

    @Override
    public ItemStack getUpgradeStack() {
        return upgrade;
    }

    protected void save() {
        upgradeSaveHandler.accept(upgrade);
    }

    protected void setCooldown(World world, int time) {
        cooldown = world.getTotalWorldTime() + time;
    }

    public boolean isInCooldown(World world) {
        return cooldown > world.getTotalWorldTime();
    }

    @Override
    public boolean isEnabled() {
        return NBTHelper.getBoolean(upgrade, "enabled", true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        NBTHelper.setBoolean(upgrade, "enabled", enabled);
        save();
        storageWrapper.getUpgradeHandler().refreshWrappersThatImplementAndTypeWrappers();
    }
}
