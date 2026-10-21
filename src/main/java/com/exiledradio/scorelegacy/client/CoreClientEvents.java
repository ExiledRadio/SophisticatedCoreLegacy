package com.exiledradio.scorelegacy.client;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.init.ModFluids;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Sprites Core's GUI and fluids draw from the block atlas. */
@Mod.EventBusSubscriber(modid = SophisticatedCoreLegacy.MODID, value = Side.CLIENT)
public final class CoreClientEvents {
    private CoreClientEvents() {}

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Pre event) {
        if (event.getMap() != Minecraft.getMinecraft().getTextureMapBlocks()) {
            return;
        }
        event.getMap().registerSprite(new ResourceLocation(StorageContainerBase.EMPTY_UPGRADE_SLOT_SPRITE));
        event.getMap().registerSprite(new ResourceLocation(TankUpgradeContainer.EMPTY_TANK_INPUT_SLOT_SPRITE));
        event.getMap().registerSprite(new ResourceLocation(TankUpgradeContainer.EMPTY_TANK_OUTPUT_SLOT_SPRITE));
        event.getMap().registerSprite(new ResourceLocation(BatteryUpgradeContainer.EMPTY_BATTERY_INPUT_SLOT_SPRITE));
        event.getMap().registerSprite(new ResourceLocation(BatteryUpgradeContainer.EMPTY_BATTERY_OUTPUT_SLOT_SPRITE));
        event.getMap().registerSprite(ModFluids.XP_STILL_TEXTURE);
        event.getMap().registerSprite(ModFluids.XP_FLOWING_TEXTURE);
    }
}
