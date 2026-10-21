/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface ITickableUpgrade {
    /** @param entity the carrier, or null when the backpack is placed as a block */
    void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos);
}
