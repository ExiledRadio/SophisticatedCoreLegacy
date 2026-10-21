/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import com.exiledradio.scorelegacy.upgrades.FilterLogic;

import javax.annotation.Nullable;

/** Restricts what hoppers, pipes and other mods may put in or take out. */
public interface IIOFilterUpgrade {
    @Nullable
    FilterLogic getInputFilter();

    @Nullable
    FilterLogic getOutputFilter();
}
