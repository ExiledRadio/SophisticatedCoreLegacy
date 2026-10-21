/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Swaps in a tool for the block the player is looking at when the tool swap key is pressed. */
public interface IBlockToolSwapUpgrade {
    boolean canProcessBlockInteract();

    boolean onBlockInteract(World world, BlockPos pos, IBlockState blockState, EntityPlayer player);
}
