/*
 * Sophisticated Core Legacy - the shared library of the unofficial 1.12.2 ports of
 * Sophisticated Backpacks and Sophisticated Storage.
 * Original mods (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio, also GPLv3.
 */
package com.exiledradio.scorelegacy;

import com.exiledradio.scorelegacy.init.ModFluids;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The upgrade system, storage GUI framework, filters, network sync and the liquid experience
 * fluid. It adds no items of its own: each storage mod registers its own upgrade items built
 * from these classes, as the original mods do.
 */
@Mod(
        modid = SophisticatedCoreLegacy.MODID,
        name = SophisticatedCoreLegacy.NAME,
        version = SophisticatedCoreLegacy.VERSION
)
public class SophisticatedCoreLegacy {

    public static final String MODID = "scorelegacy";
    public static final String NAME = "Sophisticated Core Legacy";
    // Replaced at build time by ForgeGradle from mod_version in gradle.properties.
    public static final String VERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static SophisticatedCoreLegacy instance;

    // Client-only code lives behind ClientProxy so that no client class is reachable from
    // common code: @SideOnly on methods doesn't stop a dedicated server from verifying them.
    @SidedProxy(
            clientSide = "com.exiledradio.scorelegacy.client.ClientProxy",
            serverSide = "com.exiledradio.scorelegacy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModFluids.register();
        CorePacketHandler.init();
        proxy.preInit();
    }

    @Mod.EventHandler
    public void postInit(net.minecraftforge.fml.common.event.FMLPostInitializationEvent event) {
        com.exiledradio.scorelegacy.compat.MaterialCompat.postInit();
    }
}
