/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import net.minecraft.inventory.Slot;

import java.util.List;

/** The tank and battery tabs: just their input and output slot side by side. */
public class IOSlotsTab<C extends UpgradeContainerBase<?, ?>> extends UpgradeSettingsTab<C> {
    public IOSlotsTab(C container, Position position, StorageScreenBase screen, String upgradeName) {
        super(container, position, screen, upgradeName);
        openWidth = Math.max(openWidth, 48);
        openHeight = Math.max(openHeight, 48);
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        super.renderBg(mouseX, mouseY);
        if (isOpen()) {
            GuiHelper.renderSlotsBackground(x + 3, y + 24, 1, 1, 0);
            GuiHelper.renderSlotsBackground(x + 24, y + 24, 1, 1, 0);
        }
    }

    @Override
    protected void moveSlotsToTab() {
        List<Slot> slots = getContainer().getSlots();
        positionSlot(slots.get(0), 4);
        positionSlot(slots.get(1), 25);
    }

    private void positionSlot(Slot slot, int xOffset) {
        slot.xPos = x - screen.getGuiLeft() + xOffset;
        slot.yPos = y - screen.getGuiTop() + 25;
    }
}
