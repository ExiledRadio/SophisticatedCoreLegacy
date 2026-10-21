/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings;

import javax.annotation.Nullable;

/** A settings category that marks some inventory slots with a colour in the storage GUI. */
public interface ISlotColorCategory {
    /** RGB colour for the slot, or null when this category doesn't mark it. */
    @Nullable
    Integer getSlotColor(int slotNumber);
}
