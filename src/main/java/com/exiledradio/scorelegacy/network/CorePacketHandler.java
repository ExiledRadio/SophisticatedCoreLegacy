package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/** Core's own channel, for the storage GUI and shared upgrades. Each storage mod has its own for the rest. */
public final class CorePacketHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(SophisticatedCoreLegacy.MODID);

    private CorePacketHandler() {}

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(SyncContainerClientDataMessage.Handler.class, SyncContainerClientDataMessage.class, id++, Side.SERVER);
        CHANNEL.registerMessage(SyncSlotStackMessage.Handler.class, SyncSlotStackMessage.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PlayDiscMessage.Handler.class, PlayDiscMessage.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(StopDiscPlaybackMessage.Handler.class, StopDiscPlaybackMessage.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(SoundStopNotificationMessage.Handler.class, SoundStopNotificationMessage.class, id++, Side.SERVER);
        CHANNEL.registerMessage(TankClickMessage.Handler.class, TankClickMessage.class, id++, Side.SERVER);
        CHANNEL.registerMessage(SyncSettingsMessage.Handler.class, SyncSettingsMessage.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(SetGhostSlotMessage.Handler.class, SetGhostSlotMessage.class, id++, Side.SERVER);
        CHANNEL.registerMessage(SetMemorySlotMessage.Handler.class, SetMemorySlotMessage.class, id++, Side.SERVER);
    }
}
