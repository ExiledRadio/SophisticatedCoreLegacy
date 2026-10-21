/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.jukebox;

import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The disc lives in the upgrade stack. Playback itself is client side; the server only tells
 * nearby clients to start or stop, and the backpack sends a keep-alive while it ticks, so a
 * backpack that stops ticking (dropped into a chest, logged out) stops its music too.
 */
public class JukeboxUpgradeWrapper extends UpgradeWrapperBase<JukeboxUpgradeWrapper, JukeboxUpgradeItem> implements ITickableUpgrade {
    private static final int KEEP_ALIVE_SEND_INTERVAL = 5;
    private static final String DISC_INVENTORY_TAG = "discInventory";
    private final ItemStackHandler discInventory;
    private long lastKeepAliveSendTime = 0;
    private boolean isPlaying;

    public JukeboxUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        discInventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                upgrade.setTagInfo(DISC_INVENTORY_TAG, serializeNBT());
                save();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() instanceof ItemRecord;
            }
        };
        NBTTagCompound tag = upgrade.getTagCompound();
        if (tag != null && tag.hasKey(DISC_INVENTORY_TAG)) {
            discInventory.deserializeNBT(tag.getCompoundTag(DISC_INVENTORY_TAG));
        }
        isPlaying = NBTHelper.getBoolean(upgrade, "isPlaying", false);
    }

    public ItemStack getDisc() {
        return discInventory.getStackInSlot(0);
    }

    public IItemHandlerModifiable getDiscInventory() {
        return discInventory;
    }

    /** A placed backpack: the sound stays at the block. */
    public void play(World world, BlockPos pos) {
        UUID backpackUuid = storageWrapper.getContentsUuid();
        if (!(world instanceof WorldServer) || getDisc().isEmpty() || backpackUuid == null) {
            return;
        }
        ServerStorageSoundHandler.startPlayingDisc((WorldServer) world, pos, backpackUuid, Item.getIdFromItem(getDisc().getItem()), () -> setIsPlaying(false));
        setIsPlaying(true);
    }

    /** A carried backpack: the sound follows the carrier. */
    public void play(EntityLivingBase entity) {
        UUID backpackUuid = storageWrapper.getContentsUuid();
        if (!(entity.world instanceof WorldServer) || getDisc().isEmpty() || backpackUuid == null) {
            return;
        }
        ServerStorageSoundHandler.startPlayingDisc((WorldServer) entity.world, entity.getPositionVector(), backpackUuid, entity.getEntityId(),
                Item.getIdFromItem(getDisc().getItem()), () -> setIsPlaying(false));
        setIsPlaying(true);
    }

    public void stop(EntityLivingBase entity) {
        UUID backpackUuid = storageWrapper.getContentsUuid();
        if (!(entity.world instanceof WorldServer)) {
            return;
        }
        if (backpackUuid != null) {
            ServerStorageSoundHandler.stopPlayingDisc((WorldServer) entity.world, entity.getPositionVector(), backpackUuid);
        }
        setIsPlaying(false);
    }

    private void setIsPlaying(boolean playing) {
        isPlaying = playing;
        NBTHelper.setBoolean(upgrade, "isPlaying", playing);
        save();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        UUID backpackUuid = storageWrapper.getContentsUuid();
        if (isPlaying && backpackUuid != null && lastKeepAliveSendTime < world.getTotalWorldTime() - KEEP_ALIVE_SEND_INTERVAL) {
            Vec3d position = entity != null ? entity.getPositionVector() : new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            ServerStorageSoundHandler.updateKeepAlive(backpackUuid, world, position, () -> setIsPlaying(false));
            lastKeepAliveSendTime = world.getTotalWorldTime();
        }
    }
}
