/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** Swaps in a tool for the entity the player is looking at when the tool swap key is pressed. */
public interface IEntityToolSwapUpgrade {
    boolean canProcessEntityInteract();

    boolean onEntityInteract(World world, Entity entity, EntityPlayer player);
}
