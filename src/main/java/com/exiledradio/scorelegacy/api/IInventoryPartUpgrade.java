/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import com.exiledradio.scorelegacy.inventory.IInventoryPart;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;

import javax.annotation.Nullable;
import java.util.List;

/** An upgrade that takes over some inventory slots (Storage's compression upgrade). */
public interface IInventoryPartUpgrade {
    /**
     * The part this upgrade runs, or null if it has no room.
     *
     * @param takenParts parts of upgrades in earlier upgrade slots, which this one must not overlap
     */
    @Nullable
    IInventoryPart getInventoryPart(InventoryHandler inventoryHandler, List<IInventoryPart> takenParts);
}
