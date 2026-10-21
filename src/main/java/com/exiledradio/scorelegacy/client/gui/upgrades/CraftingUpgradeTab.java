/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.upgrades.crafting.CraftingUpgradeContainer;
import net.minecraft.inventory.Slot;

import java.util.List;

/** A 3x3 grid with the result below it; the button picks where shift-clicked results go. */
public class CraftingUpgradeTab extends UpgradeSettingsTab<CraftingUpgradeContainer> {
    private static final TextureBlitData ARROW = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(97, 216), new Dimension(15, 8));
    private static final int GRID_TOP = 44;

    public CraftingUpgradeTab(CraftingUpgradeContainer container, Position position, StorageScreenBase screen) {
        super(container, position, screen, "crafting");
        addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), ButtonDefinitions.SHIFT_CLICK_TARGET,
                button -> container.setShiftClickIntoBackpack(!container.shouldShiftClickIntoBackpack()), container::shouldShiftClickIntoBackpack));
        openWidth = Math.max(openWidth, 63);
        openHeight = 142;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        super.renderBg(mouseX, mouseY);
        if (isOpen()) {
            GuiHelper.renderSlotsBackground(x + 3, y + GRID_TOP, 3, 3, 0);
            GuiHelper.blit(x + 3 + 19, y + 101, ARROW);
            GuiHelper.blit(x + 3 + 14, y + 111, GuiHelper.CRAFTING_RESULT_SLOT);
        }
    }

    @Override
    protected void moveSlotsToTab() {
        List<Slot> slots = getContainer().getSlots();
        for (int slotNumber = 0; slotNumber < 9; slotNumber++) {
            Slot slot = slots.get(slotNumber);
            slot.xPos = x + 3 - screen.getGuiLeft() + 1 + (slotNumber % 3) * 18;
            slot.yPos = y + GRID_TOP - screen.getGuiTop() + 1 + (slotNumber / 3) * 18;
        }
        Slot resultSlot = slots.get(9);
        resultSlot.xPos = x + 3 - screen.getGuiLeft() + 19;
        resultSlot.yPos = y + GRID_TOP - screen.getGuiTop() + 72;
    }
}
