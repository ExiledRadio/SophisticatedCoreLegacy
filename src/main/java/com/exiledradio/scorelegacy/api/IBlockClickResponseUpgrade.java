/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

/** Reacts to the carrier left-clicking a block; true stops other backpacks from reacting. */
public interface IBlockClickResponseUpgrade {
    boolean onBlockClick(EntityPlayer player, BlockPos pos);
}
