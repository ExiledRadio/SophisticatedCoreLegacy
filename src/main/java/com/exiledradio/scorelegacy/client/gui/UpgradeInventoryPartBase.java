/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;

/** What an upgrade draws in the inventory columns it takes (a tank, a battery). */
public abstract class UpgradeInventoryPartBase<C extends UpgradeContainerBase<?, ?>> {
    protected final C container;
    protected final int upgradeSlot;

    protected UpgradeInventoryPartBase(int upgradeSlot, C container) {
        this.container = container;
        this.upgradeSlot = upgradeSlot;
    }

    /** Positions are relative to the GUI's top left corner. */
    public abstract void render(int mouseX, int mouseY);

    public abstract boolean mouseClicked(int mouseX, int mouseY, int button);
}
