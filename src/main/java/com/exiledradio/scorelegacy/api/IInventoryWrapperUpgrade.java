/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;

/** Changes which inventory other upgrades work on (Inception adds the nested backpacks). */
public interface IInventoryWrapperUpgrade {
    IItemHandlerSimpleInserter wrapInventory(IItemHandlerSimpleInserter inventory);
}
