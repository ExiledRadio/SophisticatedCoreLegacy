/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.settings;

import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.SlotOverlays;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.common.gui.StorageBackgroundProperties;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.io.IOException;

/**
 * The settings screen: the storage's slots, read-only, with the settings tabs beside them.
 * Clicking (or dragging over) slots selects them for the open tab instead of moving items.
 */
public class SettingsScreenBase extends GuiContainer {
    private static final int SLOTS_X_OFFSET = 7;
    private static final int SLOTS_Y_OFFSET = 17;
    private static final int PLAYER_INVENTORY_HEIGHT = 97;
    private static final int LABEL_COLOR = 4210752;

    private final SettingsContainerBase container;
    private SettingsTabControl tabControl;
    private int dragButton = -1;

    public SettingsScreenBase(SettingsContainerBase container) {
        super(container);
        this.container = container;
        xSize = container.getBackgroundProperties().getSlotsOnLine() * 18 + 14;
        ySize = 114 + container.getStorageInventoryHeight();
    }

    @Override
    public void initGui() {
        super.initGui();
        tabControl = new SettingsTabControl(new Position(guiLeft + xSize, guiTop + 4), this, container);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        GuiHelper.renderTooltip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format(container.getStorageWrapper().getSettingsTitleKey()), 8, 6, LABEL_COLOR);
        for (Slot slot : container.inventorySlots) {
            SlotOverlays.drawMemorizedStack(mc, slot, container.getMemorizedStack(slot.getSlotIndex()));
            SlotOverlays.drawColorStripes(slot, tabControl.getSlotOverlayColors(slot.getSlotIndex()));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        StorageBackgroundProperties properties = container.getBackgroundProperties();
        mc.getTextureManager().bindTexture(properties.getTexture());
        int halfSlotHeight = container.getStorageInventoryHeight() / 2;
        int bottomHeight = container.getStorageInventoryHeight() - halfSlotHeight;
        Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, SLOTS_Y_OFFSET + halfSlotHeight, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop + SLOTS_Y_OFFSET + halfSlotHeight, 0, 256 - (PLAYER_INVENTORY_HEIGHT + bottomHeight),
                xSize, PLAYER_INVENTORY_HEIGHT + bottomHeight, 256, 256);
        drawStorageSlotsBackground();
        tabControl.render(mouseX, mouseY, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** The storage slots' backgrounds: rows of slots by default. */
    protected void drawStorageSlotsBackground() {
        mc.getTextureManager().bindTexture(GuiHelper.SLOTS_BACKGROUND);
        int slotsOnLine = container.getSlotsOnLine();
        int fullRows = container.getNumberOfSlots() / slotsOnLine;
        int remaining = container.getNumberOfSlots() % slotsOnLine;
        int x = guiLeft + SLOTS_X_OFFSET;
        int y = guiTop + SLOTS_Y_OFFSET;
        if (fullRows > 0) {
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, slotsOnLine * 18, fullRows * 18, 256, 256);
        }
        if (remaining > 0) {
            Gui.drawModalRectWithCustomSizedTexture(x, y + fullRows * 18, 0, 0, remaining * 18, 18, 256, 256);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (tabControl.isMouseOver(mouseX, mouseY) && tabControl.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        Slot slot = getSlotAt(mouseX, mouseY);
        if (slot != null) {
            dragButton = mouseButton;
            tabControl.handleSlotClick(slot.getSlotIndex(), mouseButton);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        Slot slot = getSlotAt(mouseX, mouseY);
        if (slot != null && dragButton == clickedMouseButton) {
            tabControl.handleSlotClick(slot.getSlotIndex(), clickedMouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragButton = -1;
    }

    /** Nothing here moves items, whatever vanilla thinks was clicked. */
    @Override
    protected void handleMouseClick(@Nullable Slot slot, int slotId, int mouseButton, ClickType type) {
        //noop
    }

    @Nullable
    private Slot getSlotAt(int mouseX, int mouseY) {
        for (Slot slot : container.inventorySlots) {
            if (isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    /** Escape (or the inventory key) goes back to the storage rather than closing everything. */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 || mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
            container.backToStorage();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeftIn, int guiTopIn) {
        for (Rectangle tab : tabControl.getTabRectangles()) {
            if (tab.contains(mouseX, mouseY)) {
                return false;
            }
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn);
    }

    /** Areas JEI must keep its overlay away from. */
    public java.util.List<Rectangle> getExtraAreas() {
        return tabControl.getTabRectangles();
    }

    /** True while the memory tab is open, so JEI items can be dropped onto slots to memorize them. */
    public boolean isMemoryTabOpen() {
        return tabControl.getOpenTab() instanceof SlotSelectionTabs.MemoryTab;
    }
}
