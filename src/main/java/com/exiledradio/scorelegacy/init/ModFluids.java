/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.init;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Liquid experience. 1.12 has no shared experience fluid tag; most mods (OpenBlocks, EnderIO,
 * Thermal Expansion's essence) agree on 20 mB per point, so any of those fluids is accepted,
 * and xpjuice is registered here only when no other mod already did.
 */
public final class ModFluids {
    public static final String XP_FLUID_NAME = "xpjuice";
    public static final ResourceLocation XP_STILL_TEXTURE = new ResourceLocation(SophisticatedCoreLegacy.MODID, "fluids/xp_still");
    public static final ResourceLocation XP_FLOWING_TEXTURE = new ResourceLocation(SophisticatedCoreLegacy.MODID, "fluids/xp_flowing");
    private static final Set<String> EXPERIENCE_FLUID_NAMES = ImmutableSet.of(XP_FLUID_NAME, "experience", "essence", "xp", "liquid_experience", "fluidexperience");

    private ModFluids() {}

    public static void register() {
        if (!FluidRegistry.isFluidRegistered(XP_FLUID_NAME)) {
            FluidRegistry.registerFluid(new Fluid(XP_FLUID_NAME, XP_STILL_TEXTURE, XP_FLOWING_TEXTURE).setLuminosity(10).setDensity(800).setViscosity(1500));
        }
    }

    public static boolean isExperience(@Nullable Fluid fluid) {
        return fluid != null && EXPERIENCE_FLUID_NAMES.contains(fluid.getName());
    }

    public static Fluid getExperienceFluid() {
        return FluidRegistry.getFluid(XP_FLUID_NAME);
    }
}
