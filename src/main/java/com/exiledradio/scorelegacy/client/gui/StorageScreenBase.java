/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.SlotOverlays;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.util.SortBy;
import com.google.common.collect.ImmutableMap;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.common.gui.StorageBackgroundProperties;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.common.gui.INameableEmptySlot;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import java.util.Map;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class StorageScreenBase extends GuiContainer {
    private static final ResourceLocation SLOTS_BACKGROUND = new ResourceLocation(SophisticatedCoreLegacy.MODID, "textures/gui/slots_background.png");
    private static final ResourceLocation GUI_CONTROLS = new ResourceLocation(SophisticatedCoreLegacy.MODID, "textures/gui/gui_controls.png");
    private static final int SLOTS_X_OFFSET = 7;
    private static final int SLOTS_Y_OFFSET = 17;
    private static final int PLAYER_INVENTORY_HEIGHT = 97;
    private static final int UPGRADE_TOP_HEIGHT = 7;
    private static final int UPGRADE_SLOT_HEIGHT = 18;
    private static final int UPGRADE_SPACE_BETWEEN_SLOTS = 4;
    private static final int UPGRADE_BOTTOM_HEIGHT = 7;
    private static final int TOTAL_UPGRADE_GUI_HEIGHT = 252;
    private static final int UPGRADE_INVENTORY_OFFSET = 26;
    private static final int UPGRADE_PANEL_WIDTH = 29;
    private static final int LABEL_COLOR = 4210752;
    private static final int ERROR_COLOR = 0xFF5555;
    private static final int ERROR_BACKGROUND_COLOR = 0xF0100010;
    // Dye red (0xB02E26), as in the original.
    private static final int ERROR_BORDER_COLOR = 0xFFB02E26;
    private static final int ERROR_SLOT_COLOR = 0xAAB02E26;
    private static final int ERROR_MAX_LINE_WIDTH = 260;

    private final StorageContainerBase container;
    private final List<UpgradeInventoryPartBase<?>> inventoryParts = new ArrayList<>();
    private UpgradeSettingsTabControl settingsTabControl;
    private final List<ToggleButton<Boolean>> upgradeSwitches = new ArrayList<>();
    private static final ButtonDefinition SORT = new ButtonDefinition(Dimension.SQUARE_12, GuiHelper.SMALL_BUTTON_BACKGROUND, GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND,
            new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(0, 144), Dimension.SQUARE_12), TranslationHelper.tooltip(TranslationHelper.translButton("sort_action")));
    private static final ButtonDefinition.Toggle<SortBy> SORT_BY = new ButtonDefinition.Toggle<>(Dimension.SQUARE_12, GuiHelper.SMALL_BUTTON_BACKGROUND, ImmutableMap.of(
            SortBy.NAME, GuiHelper.getButtonStateData(new UV(24, 144), TranslationHelper.translButton("sort_by_name"), Dimension.SQUARE_12),
            SortBy.COUNT, GuiHelper.getButtonStateData(new UV(36, 144), TranslationHelper.translButton("sort_by_count"), Dimension.SQUARE_12),
            SortBy.TAGS, GuiHelper.getButtonStateData(new UV(12, 144), TranslationHelper.translButton("sort_by_tags"), Dimension.SQUARE_12)),
            GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND);
    private Button sortButton;
    private ToggleButton<SortBy> sortByButton;

    public StorageScreenBase(StorageContainerBase container) {
        super(container);
        this.container = container;
        // Full width even when a tank or battery takes columns: they are drawn in those columns.
        xSize = container.getBackgroundProperties().getSlotsOnLine() * 18 + 14;
        ySize = 114 + container.getStorageInventoryHeight();
    }

    public StorageContainerBase getContainer() {
        return container;
    }

    @Override
    public void initGui() {
        super.initGui();
        settingsTabControl = new UpgradeSettingsTabControl(new Position(guiLeft + xSize, guiTop + 4), this);
        initUpgradeInventoryParts();
        upgradeSwitches.clear();
        // Sort buttons at the right end of the title line, as the original's default.
        sortButton = new Button(new Position(guiLeft + xSize - 34, guiTop + 4), SORT, button -> container.sort());
        sortByButton = new ToggleButton<>(new Position(guiLeft + xSize - 20, guiTop + 4), SORT_BY,
                button -> container.setSortBy(container.getSortBy().next()), container::getSortBy);
        int switchTop = guiTop + getUpgradeTop() + 10;
        for (int slot = 0; slot < container.getNumberOfUpgradeSlots(); slot++) {
            if (container.canDisableUpgrade(slot)) {
                int finalSlot = slot;
                upgradeSwitches.add(new ToggleButton<>(new Position(guiLeft - 22, switchTop), ButtonDefinitions.UPGRADE_SWITCH,
                        button -> container.setUpgradeEnabled(finalSlot, !container.getUpgradeEnabled(finalSlot)), () -> container.getUpgradeEnabled(finalSlot)));
            }
            switchTop += 22;
        }
    }

    /** Tanks and batteries, left to right in upgrade slot order, in the columns they take. */
    private void initUpgradeInventoryParts() {
        inventoryParts.clear();
        if (container.getColumnsTaken() == 0) {
            return;
        }
        int x = SLOTS_X_OFFSET + container.getSlotsOnLine() * 18;
        int height = container.getNumberOfRows() * 18;
        for (Map.Entry<Integer, UpgradeContainerBase<?, ?>> entry : container.getUpgradeContainers().entrySet()) {
            UpgradeInventoryPartBase<?> part = UpgradeGuiManager.getInventoryPart(entry.getKey(), entry.getValue(), new Position(x, SLOTS_Y_OFFSET), height, this);
            if (part != null) {
                inventoryParts.add(part);
                x += 36;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawUpgradeError();
        renderHoveredToolTip(mouseX, mouseY);
        renderEmptySlotTooltip(mouseX, mouseY);
        GuiHelper.renderTooltip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(container.getStorageTitle(), 8, 6, LABEL_COLOR);
        for (int slotNumber = 0; slotNumber < container.getNumberOfInventorySlots(); slotNumber++) {
            Slot slot = container.getSlot(slotNumber);
            SlotOverlays.drawMemorizedStack(mc, slot, container.getMemorizedStack(slotNumber));
            SlotOverlays.drawColorStripes(slot, container.getSlotOverlayColors(slotNumber));
            if (slot instanceof StorageContainerBase.StorageInventorySlot && ((StorageContainerBase.StorageInventorySlot) slot).isInfinite()) {
                // Over the item, where its count would be.
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                String infinite = "∞";
                fontRenderer.drawStringWithShadow(infinite, slot.xPos + 17 - fontRenderer.getStringWidth(infinite), slot.yPos + 9, 0xFFFFFF);
                GlStateManager.enableDepth();
            }
        }
        int inventoryLabelX = container.getBackgroundProperties().getPlayerInventoryXOffset() + 8;
        fontRenderer.drawString(I18n.format("container.inventory"), inventoryLabelX, ySize - 94, LABEL_COLOR);
    }

    /**
     * Why the last upgrade couldn't be added or removed: the slots involved turn red and the
     * message shows in a tooltip-style box centred on the inventory label line, which stays on
     * screen however tall the backpack is.
     */
    private void drawUpgradeError() {
        UpgradeSlotChangeResult error = container.getRecentUpgradeError();
        if (error == null) {
            return;
        }
        // GuiContainer.drawScreen leaves item lighting on, which would shade the overlay.
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0);
        GlStateManager.disableDepth();
        for (int upgradeSlot : error.getErrorUpgradeSlots()) {
            drawSlotErrorOverlay(container.getSlot(container.getFirstUpgradeSlot() + upgradeSlot));
        }
        for (int inventorySlot : error.getErrorInventorySlots()) {
            if (inventorySlot < container.inventorySlots.size()) {
                drawSlotErrorOverlay(container.getSlot(inventorySlot));
            }
        }
        GlStateManager.popMatrix();

        // Item and group names come as translation keys (the check also runs on the server).
        Object[] params = error.getErrorParams().clone();
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof String && I18n.hasKey((String) params[i])) {
                params[i] = I18n.format((String) params[i]);
            }
        }
        String message = I18n.format(TranslationHelper.translErrorKey(error.getErrorKey()), params);
        List<String> lines = fontRenderer.listFormattedStringToWidth(message, ERROR_MAX_LINE_WIDTH);
        int boxWidth = 0;
        for (String line : lines) {
            boxWidth = Math.max(boxWidth, fontRenderer.getStringWidth(line));
        }
        int boxHeight = 8 + (lines.size() - 1) * 10;
        int left = (width - boxWidth) / 2;
        int top = guiTop + ySize - 94 + 4;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 300);
        drawGradientRect(left - 3, top - 4, left + boxWidth + 3, top - 3, ERROR_BACKGROUND_COLOR, ERROR_BACKGROUND_COLOR);
        drawGradientRect(left - 3, top + boxHeight + 3, left + boxWidth + 3, top + boxHeight + 4, ERROR_BACKGROUND_COLOR, ERROR_BACKGROUND_COLOR);
        drawGradientRect(left - 3, top - 3, left + boxWidth + 3, top + boxHeight + 3, ERROR_BACKGROUND_COLOR, ERROR_BACKGROUND_COLOR);
        drawGradientRect(left - 4, top - 3, left - 3, top + boxHeight + 3, ERROR_BACKGROUND_COLOR, ERROR_BACKGROUND_COLOR);
        drawGradientRect(left + boxWidth + 3, top - 3, left + boxWidth + 4, top + boxHeight + 3, ERROR_BACKGROUND_COLOR, ERROR_BACKGROUND_COLOR);
        drawGradientRect(left - 3, top - 3 + 1, left - 3 + 1, top + boxHeight + 3 - 1, ERROR_BORDER_COLOR, ERROR_BORDER_COLOR);
        drawGradientRect(left + boxWidth + 2, top - 3 + 1, left + boxWidth + 3, top + boxHeight + 3 - 1, ERROR_BORDER_COLOR, ERROR_BORDER_COLOR);
        drawGradientRect(left - 3, top - 3, left + boxWidth + 3, top - 3 + 1, ERROR_BORDER_COLOR, ERROR_BORDER_COLOR);
        drawGradientRect(left - 3, top + boxHeight + 2, left + boxWidth + 3, top + boxHeight + 3, ERROR_BORDER_COLOR, ERROR_BORDER_COLOR);
        int y = top;
        for (String line : lines) {
            fontRenderer.drawStringWithShadow(line, left, y, ERROR_COLOR);
            y += 10;
        }
        GlStateManager.popMatrix();
        GlStateManager.enableDepth();
    }

    private void drawSlotErrorOverlay(Slot slot) {
        if (slot.xPos < 0) {
            return;
        }
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, ERROR_SLOT_COLOR, ERROR_SLOT_COLOR);
        GlStateManager.colorMask(true, true, true, true);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawInventoryBackground();
        for (UpgradeInventoryPartBase<?> part : inventoryParts) {
            part.render(mouseX, mouseY);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawUpgradeBackground();
        for (ToggleButton<Boolean> upgradeSwitch : upgradeSwitches) {
            upgradeSwitch.render(mouseX, mouseY, partialTicks);
        }
        if (shouldShowSortButtons()) {
            sortButton.render(mouseX, mouseY, partialTicks);
            sortByButton.render(mouseX, mouseY, partialTicks);
        }
        // Tabs are drawn here, under the slots, so the slots they hold show on top of them.
        settingsTabControl.render(mouseX, mouseY, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawInventoryBackground() {
        StorageBackgroundProperties properties = container.getBackgroundProperties();
        mc.getTextureManager().bindTexture(properties.getTexture());
        // The texture has a top part and a bottom part; together they stretch to any row count.
        int halfSlotHeight = container.getStorageInventoryHeight() / 2;
        Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, SLOTS_Y_OFFSET + halfSlotHeight, 256, 256);
        int bottomHeight = container.getStorageInventoryHeight() - halfSlotHeight;
        Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop + SLOTS_Y_OFFSET + halfSlotHeight, 0, 256 - (PLAYER_INVENTORY_HEIGHT + bottomHeight),
                xSize, PLAYER_INVENTORY_HEIGHT + bottomHeight, 256, 256);
        drawStorageSlotsBackground();
    }

    /** The storage slots' backgrounds: rows of slots by default. */
    protected void drawStorageSlotsBackground() {
        mc.getTextureManager().bindTexture(SLOTS_BACKGROUND);
        int slotsOnLine = container.getSlotsOnLine();
        int fullRows = container.getNumberOfInventorySlots() / slotsOnLine;
        int remainingSlots = container.getNumberOfInventorySlots() % slotsOnLine;
        int x = guiLeft + SLOTS_X_OFFSET;
        int y = guiTop + SLOTS_Y_OFFSET;
        if (fullRows > 0) {
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, slotsOnLine * 18, fullRows * 18, 256, 256);
        }
        if (remainingSlots > 0) {
            Gui.drawModalRectWithCustomSizedTexture(x, y + fullRows * 18, 0, 0, remainingSlots * 18, 18, 256, 256);
        }
    }

    private void drawUpgradeBackground() {
        if (container.getNumberOfUpgradeSlots() == 0) {
            return;
        }
        mc.getTextureManager().bindTexture(GUI_CONTROLS);
        int firstPartHeight = getUpgradeHeightWithoutBottom();
        int top = guiTop + getUpgradeTop();
        Gui.drawModalRectWithCustomSizedTexture(guiLeft - UPGRADE_INVENTORY_OFFSET, top, 0, 0, UPGRADE_PANEL_WIDTH, firstPartHeight, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(guiLeft - UPGRADE_INVENTORY_OFFSET, top + firstPartHeight, 0, TOTAL_UPGRADE_GUI_HEIGHT - UPGRADE_BOTTOM_HEIGHT,
                UPGRADE_PANEL_WIDTH, UPGRADE_BOTTOM_HEIGHT, 256, 256);
    }

    private int getUpgradeTop() {
        return ySize - 94 - getUpgradeHeight();
    }

    private int getUpgradeHeight() {
        return getUpgradeHeightWithoutBottom() + UPGRADE_TOP_HEIGHT;
    }

    private int getUpgradeHeightWithoutBottom() {
        int slots = container.getNumberOfUpgradeSlots();
        return UPGRADE_BOTTOM_HEIGHT + slots * UPGRADE_SLOT_HEIGHT + (slots - 1) * UPGRADE_SPACE_BETWEEN_SLOTS;
    }

    // --- input -------------------------------------------------------------------------------

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (ToggleButton<Boolean> upgradeSwitch : upgradeSwitches) {
            if (upgradeSwitch.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        if (shouldShowSortButtons() && (sortButton.mouseClicked(mouseX, mouseY, mouseButton) || sortByButton.mouseClicked(mouseX, mouseY, mouseButton))) {
            return;
        }
        // The sort key (middle mouse by default, as the original's) over the storage's slots sorts
        // it. These containers opt out of Inventory Tweaks, whose click-based sorting can't handle big stacks.
        if (com.exiledradio.scorelegacy.client.CoreKeyBindings.SORT.isActiveAndMatches(mouseButton - 100) && trySortAt(mouseX, mouseY)) {
            return;
        }
        for (UpgradeInventoryPartBase<?> part : inventoryParts) {
            if (part.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        // A click on a slot inside an open tab is a slot click, not a tab click.
        if (getSlotUnderMouseAt(mouseX, mouseY) == null && settingsTabControl.isMouseOver(mouseX, mouseY)
                && settingsTabControl.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (com.exiledradio.scorelegacy.client.CoreKeyBindings.SORT.isActiveAndMatches(keyCode)) {
            int mouseX = org.lwjgl.input.Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - org.lwjgl.input.Mouse.getY() * height / mc.displayHeight - 1;
            if (trySortAt(mouseX, mouseY)) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    private boolean trySortAt(int mouseX, int mouseY) {
        if (!shouldShowSortButtons() || !mc.player.inventory.getItemStack().isEmpty()) {
            return false;
        }
        Slot slot = getSlotUnderMouseAt(mouseX, mouseY);
        if (slot != null && slot.slotNumber < container.getNumberOfInventorySlots()) {
            container.sort();
            return true;
        }
        return false;
    }

    private void renderEmptySlotTooltip(int mouseX, int mouseY) {
        Slot slot = getSlotUnderMouse();
        if (mc.player.inventory.getItemStack().isEmpty() && slot instanceof INameableEmptySlot && !slot.getHasStack()
                && ((INameableEmptySlot) slot).getEmptyTooltip() != null) {
            drawHoveringText(I18n.format(((INameableEmptySlot) slot).getEmptyTooltip()), mouseX, mouseY);
        }
    }

    private Slot getSlotUnderMouseAt(int mouseX, int mouseY) {
        for (Slot slot : container.inventorySlots) {
            if (slot.isEnabled() && isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            if (settingsTabControl.mouseScrolled(mouseX, mouseY, Integer.signum(wheel))) {
                return;
            }
        }
        super.handleMouseInput();
    }

    /** The upgrade column and open tabs count as inside, or clicking them would drop the held stack. */
    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeftIn, int guiTopIn) {
        if (container.getNumberOfUpgradeSlots() > 0) {
            int left = guiLeftIn - UPGRADE_INVENTORY_OFFSET;
            int top = guiTopIn + getUpgradeTop();
            if (mouseX >= left && mouseX < guiLeftIn && mouseY >= top && mouseY < top + getUpgradeHeight()) {
                return false;
            }
        }
        for (Rectangle tab : settingsTabControl.getTabRectangles()) {
            if (tab.contains(mouseX, mouseY)) {
                return false;
            }
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn);
    }

    /** Areas JEI must keep its overlay away from. */
    public List<Rectangle> getExtraAreas() {
        List<Rectangle> areas = new ArrayList<>(settingsTabControl.getTabRectangles());
        if (shouldShowSortButtons()) {
            areas.add(new Rectangle(sortButton.getX(), sortButton.getY(), 26, 12));
        }
        if (container.getNumberOfUpgradeSlots() > 0) {
            areas.add(new Rectangle(guiLeft - UPGRADE_INVENTORY_OFFSET, guiTop + getUpgradeTop(), UPGRADE_PANEL_WIDTH, getUpgradeHeight()));
        }
        return areas;
    }

    /** Storages whose slots never move (Storage's limited barrels) have no sort buttons. */
    protected boolean shouldShowSortButtons() {
        return true;
    }

    @Override
    public List<String> getItemToolTip(ItemStack stack) {
        List<String> tooltip = super.getItemToolTip(stack);
        if (stack.getCount() > 999) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui." + SophisticatedCoreLegacy.MODID + ".tooltip.stack_count",
                    TextFormatting.DARK_AQUA + NumberFormat.getNumberInstance().format(stack.getCount()) + TextFormatting.GRAY));
        }
        return tooltip;
    }
}
