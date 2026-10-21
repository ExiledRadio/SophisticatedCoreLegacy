/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.jukebox;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.common.gui.SlotSuppliedHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class JukeboxUpgradeContainer extends UpgradeContainerBase<JukeboxUpgradeWrapper, JukeboxUpgradeContainer> {
    private static final String ACTION_DATA = "action";

    public JukeboxUpgradeContainer(EntityPlayer player, int containerId, JukeboxUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                   UpgradeContainerType<JukeboxUpgradeWrapper, JukeboxUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        slots.add(new SlotSuppliedHandler(() -> getUpgradeWrapper().getDiscInventory(), 0, -100, -100) {
            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                // Taking the disc out (or swapping it) ends the song that was playing.
                if (!player.world.isRemote && getUpgradeWrapper().isPlaying()) {
                    getUpgradeWrapper().stop(player);
                }
            }
        });
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (!data.hasKey(ACTION_DATA)) {
            return;
        }
        String action = data.getString(ACTION_DATA);
        if ("play".equals(action)) {
            if (player.openContainer instanceof StorageContainerBase) {
                BlockPos pos = ((StorageContainerBase) player.openContainer).getStorageBlockPos();
                if (pos != null) {
                    getUpgradeWrapper().play(player.world, pos);
                } else {
                    getUpgradeWrapper().play(player);
                }
            }
        } else if ("stop".equals(action)) {
            getUpgradeWrapper().stop(player);
        }
    }

    public void play() {
        sendDataToServer(() -> NBTHelper.withString(ACTION_DATA, "play"));
    }

    public void stop() {
        sendDataToServer(() -> NBTHelper.withString(ACTION_DATA, "stop"));
    }
}
