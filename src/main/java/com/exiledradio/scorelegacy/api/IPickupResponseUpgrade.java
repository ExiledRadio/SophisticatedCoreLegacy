/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Gets first go at items the carrier picks up (and items thrown onto a placed backpack). */
public interface IPickupResponseUpgrade {
    ItemStack pickup(World world, ItemStack stack, boolean simulate);
}
