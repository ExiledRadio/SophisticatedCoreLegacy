/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;

/** Runs when the player sneak-right-clicks an inventory block with the backpack in hand. */
public interface IItemHandlerInteractionUpgrade {
    void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player);
}
