package com.exiledradio.scorelegacy;

import com.exiledradio.scorelegacy.network.PlayDiscMessage;
import com.exiledradio.scorelegacy.network.StopDiscPlaybackMessage;
import com.exiledradio.scorelegacy.network.SyncSettingsMessage;
import com.exiledradio.scorelegacy.network.SyncSlotStackMessage;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/** Core's server-side proxy: the client-only message handlers are no-ops here. */
public class CommonProxy {

    public void preInit() {
        //noop on the server
    }

    /** The client's world, for recipe lookups when there is no integrated server. Null on a server. */
    @Nullable
    public World getClientWorld() {
        return null;
    }

    /** Client only: a full-count storage slot update from the server. */
    public void handleSyncSlotStack(SyncSlotStackMessage message) {
        //noop on the server
    }

    /** Client only: the open storage's settings changed on the server. */
    public void handleSyncSettings(SyncSettingsMessage message) {
        //noop on the server
    }

    /** Client only: a storage started playing a disc. */
    public void handlePlayDisc(PlayDiscMessage message) {
        //noop on the server
    }

    /** Client only: a storage stopped playing its disc. */
    public void handleStopDisc(StopDiscPlaybackMessage message) {
        //noop on the server
    }
}
