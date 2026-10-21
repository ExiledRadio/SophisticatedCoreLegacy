/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.PlayDiscMessage;
import com.exiledradio.scorelegacy.network.SoundStopNotificationMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Client side of the jukebox upgrade: one record sound per backpack. */
@Mod.EventBusSubscriber(modid = SophisticatedCoreLegacy.MODID, value = Side.CLIENT)
public final class StorageSoundHandler {
    private static final int SOUND_STOP_CHECK_INTERVAL = 10;
    private static final Map<UUID, ISound> backpackSounds = new ConcurrentHashMap<>();
    private static long lastPlaybackChecked = 0;

    private StorageSoundHandler() {}

    public static void play(PlayDiscMessage message) {
        Item disc = Item.getItemById(message.getDiscItemId());
        if (!(disc instanceof ItemRecord)) {
            return;
        }
        SoundEvent sound = ((ItemRecord) disc).getSound();
        if (message.isBlockBackpack()) {
            BlockPos pos = message.getPos();
            playBackpackSound(message.getBackpackUuid(), PositionedSoundRecord.getRecordSoundRecord(sound, pos.getX(), pos.getY(), pos.getZ()));
        } else {
            Entity entity = Minecraft.getMinecraft().world == null ? null : Minecraft.getMinecraft().world.getEntityByID(message.getEntityId());
            if (entity instanceof EntityLivingBase) {
                playBackpackSound(message.getBackpackUuid(), new EntitySound(sound, entity));
            }
        }
    }

    private static void playBackpackSound(UUID backpackUuid, ISound sound) {
        stopBackpackSound(backpackUuid);
        backpackSounds.put(backpackUuid, sound);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
    }

    public static void stopBackpackSound(UUID backpackUuid) {
        ISound sound = backpackSounds.remove(backpackUuid);
        if (sound != null) {
            Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
            CorePacketHandler.CHANNEL.sendToServer(new SoundStopNotificationMessage(backpackUuid));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.world == null || backpackSounds.isEmpty()
                || lastPlaybackChecked >= mc.world.getTotalWorldTime() - SOUND_STOP_CHECK_INTERVAL) {
            return;
        }
        lastPlaybackChecked = mc.world.getTotalWorldTime();
        backpackSounds.entrySet().removeIf(entry -> {
            if (!mc.getSoundHandler().isSoundPlaying(entry.getValue())) {
                CorePacketHandler.CHANNEL.sendToServer(new SoundStopNotificationMessage(entry.getKey()));
                return true;
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            backpackSounds.clear();
            lastPlaybackChecked = 0;
        }
    }

    /** A record that follows the entity carrying the backpack. */
    private static class EntitySound extends MovingSound {
        private final Entity entity;

        EntitySound(SoundEvent sound, Entity entity) {
            super(sound, SoundCategory.RECORDS);
            this.entity = entity;
            volume = 2;
            pitch = 1;
            update();
        }

        @Override
        public void update() {
            if (entity.isDead) {
                donePlaying = true;
                return;
            }
            xPosF = (float) entity.posX;
            yPosF = (float) entity.posY;
            zPosF = (float) entity.posZ;
        }
    }
}
