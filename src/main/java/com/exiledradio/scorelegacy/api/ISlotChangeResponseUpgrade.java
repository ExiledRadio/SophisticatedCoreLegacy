/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraftforge.items.IItemHandler;

public interface ISlotChangeResponseUpgrade {
    void onSlotChange(IItemHandler inventoryHandler, int slot);
}
