/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

/** The input or output slot of a tank or battery: an outline icon and a tooltip while empty. */
public class UpgradeIOSlot extends SlotSuppliedHandler implements INameableEmptySlot {
    private final String emptyTooltipKey;

    public UpgradeIOSlot(Supplier<IItemHandler> itemHandlerSupplier, int slot, String backgroundSprite, String emptyTooltipKey) {
        super(itemHandlerSupplier, slot, -100, -100);
        this.emptyTooltipKey = emptyTooltipKey;
        setBackgroundName(backgroundSprite);
    }

    @Override
    public String getEmptyTooltip() {
        return emptyTooltipKey;
    }
}
