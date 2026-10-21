/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings;

import net.minecraft.nbt.NBTTagCompound;

/** One group of storage settings (memory slots, no-sort slots...), saved in its own NBT compound. */
public interface ISettingsCategory {
    /** Replaces everything with what the given NBT says (a client getting the server's copy). */
    void reloadFrom(NBTTagCompound categoryNbt);

    /**
     * Copies the settings of slots from startFromSlot on to the same category of another storage,
     * each slot moved by slotOffset (Storage's double chests joining and splitting). Categories
     * without per-slot settings copy nothing.
     */
    default void copySlotSettingsTo(ISettingsCategory target, int startFromSlot, int slotOffset) {
    }

    /** Forgets the settings of this slot and every slot after it. */
    default void deleteSlotSettingsFrom(int slot) {
    }
}
