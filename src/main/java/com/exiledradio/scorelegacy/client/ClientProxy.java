package com.exiledradio.scorelegacy.client;

import com.exiledradio.scorelegacy.CommonProxy;
import com.exiledradio.scorelegacy.network.PlayDiscMessage;
import com.exiledradio.scorelegacy.network.StopDiscPlaybackMessage;
import com.exiledradio.scorelegacy.network.SyncSlotStackMessage;
import com.exiledradio.scorelegacy.network.SyncSettingsMessage;
import com.exiledradio.scorelegacy.common.gui.ISyncedContainer;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.settings.main.MainSettingsCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/** Core's client proxy. Never loaded on a dedicated server. */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        CoreKeyBindings.register();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public void handleSyncSlotStack(SyncSlotStackMessage message) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (mc.player != null && mc.player.openContainer != null && mc.player.openContainer.windowId == message.getWindowId()) {
                mc.player.openContainer.putStackInSlot(message.getSlotNumber(), message.getStack());
            }
        });
    }

    @Override
    public void handleSyncSettings(SyncSettingsMessage message) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (mc.player != null && mc.player.openContainer instanceof ISyncedContainer && mc.player.openContainer.windowId == message.getWindowId()) {
                SettingsHandler settings = ((ISyncedContainer) mc.player.openContainer).getStorageWrapper().getSettingsHandler();
                settings.reloadFrom(message.getSettings());
                MainSettingsCategory main = settings.getTypeCategory(MainSettingsCategory.class);
                if (main != null) {
                    main.setPlayerSettingsTag(mc.player, message.getPlayerSettings());
                }
            }
        });
    }

    @Override
    public void handlePlayDisc(PlayDiscMessage message) {
        Minecraft.getMinecraft().addScheduledTask(() -> StorageSoundHandler.play(message));
    }

    @Override
    public void handleStopDisc(StopDiscPlaybackMessage message) {
        Minecraft.getMinecraft().addScheduledTask(() -> StorageSoundHandler.stopBackpackSound(message.getBackpackUuid()));
    }
}
