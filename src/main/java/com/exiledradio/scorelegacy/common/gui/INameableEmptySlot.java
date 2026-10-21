/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import javax.annotation.Nullable;

/** A slot that shows a tooltip while empty (a translation key, or literal text). */
public interface INameableEmptySlot {
    @Nullable
    String getEmptyTooltip();
}
