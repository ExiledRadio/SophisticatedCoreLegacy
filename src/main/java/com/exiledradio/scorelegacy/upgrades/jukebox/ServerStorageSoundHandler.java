/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.jukebox;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.PlayDiscMessage;
import com.exiledradio.scorelegacy.network.StopDiscPlaybackMessage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Which backpacks are playing in each dimension, and stopping the ones that went quiet. */
@Mod.EventBusSubscriber(modid = SophisticatedCoreLegacy.MODID)
public final class ServerStorageSoundHandler {
    private static final int KEEP_ALIVE_CHECK_INTERVAL = 10;
    private static final int SOUND_RANGE = 128;
    private static final Map<Integer, Long> lastWorldCheck = new HashMap<>();
    private static final Map<Integer, Map<UUID, KeepAliveInfo>> worldBackpackKeepAlive = new HashMap<>();

    private ServerStorageSoundHandler() {}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        World world = event.world;
        int dim = world.provider.getDimension();
        long time = world.getTotalWorldTime();
        if (lastWorldCheck.computeIfAbsent(dim, key -> time) > time - KEEP_ALIVE_CHECK_INTERVAL || !worldBackpackKeepAlive.containsKey(dim)) {
            return;
        }
        lastWorldCheck.put(dim, time);
        worldBackpackKeepAlive.get(dim).entrySet().removeIf(entry -> {
            if (entry.getValue().lastKeepAliveTime < time - KEEP_ALIVE_CHECK_INTERVAL) {
                sendToAllNear(world, entry.getValue().lastPosition, new StopDiscPlaybackMessage(entry.getKey()));
                return true;
            }
            return false;
        });
    }

    public static void updateKeepAlive(UUID backpackUuid, World world, Vec3d position, Runnable onNoLongerRunning) {
        Map<UUID, KeepAliveInfo> keepAlive = worldBackpackKeepAlive.get(world.provider.getDimension());
        if (keepAlive == null || !keepAlive.containsKey(backpackUuid)) {
            onNoLongerRunning.run();
            return;
        }
        keepAlive.get(backpackUuid).update(world.getTotalWorldTime(), position);
    }

    /** A client says the disc ended (or it stopped it). */
    public static void onSoundStopped(WorldServer world, UUID backpackUuid) {
        removeKeepAliveInfo(world, backpackUuid);
    }

    public static void startPlayingDisc(WorldServer world, BlockPos position, UUID backpackUuid, int discItemId, Runnable onStopHandler) {
        Vec3d pos = new Vec3d(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
        sendToAllNear(world, pos, new PlayDiscMessage(backpackUuid, discItemId, position));
        putKeepAliveInfo(world, backpackUuid, onStopHandler, pos);
    }

    public static void startPlayingDisc(WorldServer world, Vec3d position, UUID backpackUuid, int entityId, int discItemId, Runnable onStopHandler) {
        sendToAllNear(world, position, new PlayDiscMessage(backpackUuid, discItemId, entityId));
        putKeepAliveInfo(world, backpackUuid, onStopHandler, position);
    }

    public static void stopPlayingDisc(WorldServer world, Vec3d position, UUID backpackUuid) {
        removeKeepAliveInfo(world, backpackUuid);
        sendToAllNear(world, position, new StopDiscPlaybackMessage(backpackUuid));
    }

    private static void putKeepAliveInfo(WorldServer world, UUID backpackUuid, Runnable onStopHandler, Vec3d pos) {
        worldBackpackKeepAlive.computeIfAbsent(world.provider.getDimension(), dim -> new HashMap<>())
                .put(backpackUuid, new KeepAliveInfo(onStopHandler, world.getTotalWorldTime(), pos));
    }

    private static void removeKeepAliveInfo(World world, UUID backpackUuid) {
        Map<UUID, KeepAliveInfo> keepAlive = worldBackpackKeepAlive.get(world.provider.getDimension());
        if (keepAlive != null && keepAlive.containsKey(backpackUuid)) {
            keepAlive.remove(backpackUuid).runOnStop();
        }
    }

    private static void sendToAllNear(World world, Vec3d pos, IMessage message) {
        CorePacketHandler.CHANNEL.sendToAllAround(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.x, pos.y, pos.z, SOUND_RANGE));
    }

    private static class KeepAliveInfo {
        // Weak, so a wrapper that was replaced (or a backpack that is gone) isn't kept alive by this map.
        private final WeakReference<Runnable> onStopHandler;
        private long lastKeepAliveTime;
        private Vec3d lastPosition;

        private KeepAliveInfo(Runnable onStopHandler, long lastKeepAliveTime, Vec3d lastPosition) {
            this.onStopHandler = new WeakReference<>(onStopHandler);
            this.lastKeepAliveTime = lastKeepAliveTime;
            this.lastPosition = lastPosition;
        }

        private void update(long gameTime, Vec3d position) {
            lastKeepAliveTime = gameTime;
            lastPosition = position;
        }

        private void runOnStop() {
            Runnable handler = onStopHandler.get();
            if (handler != null) {
                handler.run();
            }
        }
    }
}
