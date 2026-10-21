/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.item.ItemStack;

/** Live behaviour of one upgrade item sitting in a backpack's upgrade slot. */
public interface IUpgradeWrapper {
    boolean isEnabled();

    void setEnabled(boolean enabled);

    default boolean canBeDisabled() {
        return true;
    }

    ItemStack getUpgradeStack();

    default boolean hideSettingsTab() {
        return false;
    }

    default void onBeforeRemoved() {
        //noop
    }
}
