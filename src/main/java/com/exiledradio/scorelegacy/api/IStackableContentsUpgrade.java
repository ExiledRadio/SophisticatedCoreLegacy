/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

/** An upgrade whose own contents need at least this stack multiplier (blocks removing stack upgrades). */
public interface IStackableContentsUpgrade {
    int getMinimumMultiplierRequired();
}
