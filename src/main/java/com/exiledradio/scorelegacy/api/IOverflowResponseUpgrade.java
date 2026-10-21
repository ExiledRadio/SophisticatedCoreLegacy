/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.item.ItemStack;

/** Gets whatever did not fit when something was inserted into the backpack. */
public interface IOverflowResponseUpgrade {
    boolean worksInGui();

    ItemStack onOverflow(ItemStack stack);

    boolean stackMatchesFilter(ItemStack stack);
}
