/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.filter;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.CompositeWidget;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.controls.Widget;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.upgrades.FilterLogicBase;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainerBase;
import com.exiledradio.scorelegacy.upgrades.PrimaryMatch;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * The filter part of an upgrade tab: the match buttons, the ghost slot grid, and (when matching
 * by ore dictionary) the name list with add/remove buttons that scroll through names.
 */
public abstract class FilterLogicControlBase<F extends FilterLogicBase, S extends Slot, C extends FilterLogicContainerBase<F, S>> extends CompositeWidget<Widget> {
    private static final int TAG_FONT_COLOR = 16383998;
    private static final int MORE_TAGS_FONT_COLOR = 13882323;
    private static final int MAX_TAG_NAME_WIDTH = 68;

    protected final MatchButton[] showMatchButtons;
    protected final int slotsTopYOffset;
    protected final int slotsPerRow;
    protected final int slotsInExtraRow;
    protected final int fullSlotRows;
    private final int totalSlotRows;
    private final StorageScreenBase screen;
    protected final C container;
    private final int height;
    private final int width;
    @Nullable
    private ToggleButton<Boolean> nbtButton = null;
    @Nullable
    private ToggleButton<Boolean> durabilityButton = null;
    private int tagButtonsYOffset;

    protected FilterLogicControlBase(StorageScreenBase screen, C container, Position position, boolean buttonsVisible, int slotsPerRow, MatchButton... showMatchButtons) {
        super(position);
        this.screen = screen;
        this.container = container;
        slotsTopYOffset = buttonsVisible ? 21 : 0;
        this.slotsPerRow = slotsPerRow;
        this.showMatchButtons = showMatchButtons;
        fullSlotRows = container.getFilterSlots().size() / slotsPerRow;
        slotsInExtraRow = container.getFilterSlots().size() % slotsPerRow;
        totalSlotRows = fullSlotRows + (slotsInExtraRow > 0 ? 1 : 0);

        if (shouldShow(MatchButton.ALLOW_LIST)) {
            addChild(new ToggleButton<>(new Position(x, y), ButtonDefinitions.ALLOW_LIST, button -> container.setAllowList(!container.isAllowList()), container::isAllowList));
        }
        if (shouldShow(MatchButton.PRIMARY_MATCH)) {
            addChild(new ToggleButton<>(new Position(x + 18, y), ButtonDefinitions.PRIMARY_MATCH, button -> {
                PrimaryMatch next = container.getPrimaryMatch().next();
                if (next == PrimaryMatch.TAGS) {
                    container.getFilterSlots().forEach(slot -> slot.xPos = StorageContainerBase.DISABLED_SLOT_X_POS);
                    onTagsMatchSelected();
                }
                container.setPrimaryMatch(next);
                setDurabilityAndNbtButtonsVisibility();
                moveSlotsToView();
            }, container::getPrimaryMatch));
            addTagButtons();
        }
        if (shouldShow(MatchButton.DURABILITY)) {
            durabilityButton = addChild(new ToggleButton<>(new Position(x + 36, y), ButtonDefinitions.MATCH_DURABILITY,
                    button -> container.setMatchDurability(!container.shouldMatchDurability()), container::shouldMatchDurability));
        }
        if (shouldShow(MatchButton.NBT)) {
            nbtButton = addChild(new ToggleButton<>(new Position(x + 54, y), ButtonDefinitions.MATCH_NBT,
                    button -> container.setMatchNbt(!container.shouldMatchNbt()), container::shouldMatchNbt));
        }

        width = Math.max(slotsPerRow * 18, getMaxButtonWidth());
        height = totalSlotRows * 18 + slotsTopYOffset;
        setDurabilityAndNbtButtonsVisibility();
    }

    private void setDurabilityAndNbtButtonsVisibility() {
        boolean visible = container.getPrimaryMatch() != PrimaryMatch.TAGS;
        if (nbtButton != null) {
            nbtButton.setVisible(visible);
        }
        if (durabilityButton != null) {
            durabilityButton.setVisible(visible);
        }
    }

    protected void onTagsMatchSelected() {
        //noop
    }

    private boolean isTagsMode() {
        return container.getPrimaryMatch() == PrimaryMatch.TAGS;
    }

    private void addTagButtons() {
        tagButtonsYOffset = slotsTopYOffset + getTagListHeight();
        addChild(new TagButton(new Position(x + 36, y + tagButtonsYOffset), ButtonDefinitions.REMOVE_TAG, button -> container.removeSelectedTag(), delta -> {
            if (delta < 0) {
                container.selectNextTagToRemove();
            } else {
                container.selectPreviousTagToRemove();
            }
        }) {
            @Override
            protected List<String> getTooltip() {
                return getRemoveTooltip();
            }
        });
        addChild(new TagButton(new Position(x + 18, y + tagButtonsYOffset), ButtonDefinitions.ADD_TAG, button -> container.addSelectedTag(), delta -> {
            if (delta < 0) {
                container.selectNextTagToAdd();
            } else {
                container.selectPreviousTagToAdd();
            }
        }) {
            @Override
            protected List<String> getTooltip() {
                return getAddTooltip();
            }
        });
        addChild(new ToggleButton<Boolean>(new Position(x + 54, y + tagButtonsYOffset), ButtonDefinitions.MATCH_ANY_TAG,
                button -> container.setMatchAnyTag(!container.shouldMatchAnyTag()), container::shouldMatchAnyTag) {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                if (isTagsMode()) {
                    super.render(mouseX, mouseY, partialTicks);
                }
            }

            @Override
            public boolean isMouseOver(int mouseX, int mouseY) {
                return isTagsMode() && super.isMouseOver(mouseX, mouseY);
            }
        });
    }

    private List<String> getRemoveTooltip() {
        List<String> lines = new ArrayList<>();
        lines.add(I18n.format(TranslationHelper.translUpgradeButton("remove_tag")));
        Set<String> tagNames = container.getTagNames();
        if (tagNames.isEmpty()) {
            lines.add(TextFormatting.RED + I18n.format(TranslationHelper.translUpgradeButton("remove_tag.empty")));
            return lines;
        }
        int index = 0;
        for (String tagName : tagNames) {
            lines.add(index == container.getSelectedTagToRemove() ? TextFormatting.RED + "-> " + tagName : TextFormatting.GRAY + "> " + tagName);
            index++;
        }
        lines.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format(TranslationHelper.translUpgradeButton("remove_tag.controls")));
        return lines;
    }

    private List<String> getAddTooltip() {
        List<String> lines = new ArrayList<>();
        lines.add(I18n.format(TranslationHelper.translUpgradeButton("add_tag")));
        if (container.getTagSelectionSlot().getStack().isEmpty()) {
            lines.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format(TranslationHelper.translUpgradeButton("add_tag.no_item")));
            return lines;
        }
        Set<String> tagsToAdd = container.getTagsToAdd();
        int index = 0;
        for (String tagName : tagsToAdd) {
            lines.add(index == container.getSelectedTagToAdd() ? TextFormatting.GREEN + "-> " + tagName : TextFormatting.GRAY + "> " + tagName);
            index++;
        }
        lines.add(TextFormatting.ITALIC + (tagsToAdd.isEmpty()
                ? TextFormatting.YELLOW + I18n.format(TranslationHelper.translUpgradeButton("add_tag.no_additional_tags"))
                : TextFormatting.DARK_GRAY + I18n.format(TranslationHelper.translUpgradeButton("add_tag.controls"))));
        return lines;
    }

    protected int getMaxButtonWidth() {
        int maxWidth = 0;
        for (Widget w : children) {
            maxWidth = Math.max(maxWidth, w.getX() + w.getWidth() - x);
        }
        return maxWidth;
    }

    protected boolean shouldShow(MatchButton matchButton) {
        for (MatchButton showMatchButton : showMatchButtons) {
            if (showMatchButton == matchButton) {
                return true;
            }
        }
        return false;
    }

    /** Puts the ghost slots (or, in ore dictionary mode, the item-to-read slot) under this control. */
    public void moveSlotsToView() {
        if (isTagsMode()) {
            Slot slot = container.getTagSelectionSlot();
            slot.xPos = x - screen.getGuiLeft() + 1;
            slot.yPos = y - screen.getGuiTop() + tagButtonsYOffset + 1;
            container.getFilterSlots().forEach(s -> s.xPos = StorageContainerBase.DISABLED_SLOT_X_POS);
        } else {
            int index = 0;
            for (S slot : container.getFilterSlots()) {
                slot.xPos = x - screen.getGuiLeft() + 1 + (index % slotsPerRow) * 18;
                slot.yPos = y - screen.getGuiTop() + slotsTopYOffset + 1 + (index / slotsPerRow) * 18;
                index++;
            }
            container.getTagSelectionSlot().xPos = StorageContainerBase.DISABLED_SLOT_X_POS;
        }
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(mouseX, mouseY, partialTicks);
        if (isTagsMode()) {
            renderTagNames(mouseX, mouseY);
        }
    }

    private void renderTagNames(int mouseX, int mouseY) {
        int count = 0;
        int prefixWidth = font.getStringWidth("...");
        Set<String> tagNames = container.getTagNames();
        int maxTagNameLines = getTagListHeight() / 10;
        for (String name : tagNames) {
            if (tagNames.size() > maxTagNameLines && count == maxTagNameLines - 1) {
                font.drawString(I18n.format(TranslationHelper.translUpgradeKey("tag_list.tag_overflow"), String.valueOf(tagNames.size() - (maxTagNameLines - 1))),
                        x + 2, y + slotsTopYOffset + 2 + count * 10, MORE_TAGS_FONT_COLOR);
                break;
            }
            String shortened = name;
            if (font.getStringWidth(name) > MAX_TAG_NAME_WIDTH) {
                shortened = "..." + font.trimStringToWidth(name, MAX_TAG_NAME_WIDTH - prefixWidth, true);
            }
            font.drawString(shortened, x + 2, y + slotsTopYOffset + 2 + count * 10, TAG_FONT_COLOR);
            count++;
        }
        if (mouseX >= x && mouseX < x + getTagListWidth() && mouseY >= y + slotsTopYOffset && mouseY < y + slotsTopYOffset + getTagListHeight()) {
            List<String> lines = new ArrayList<>();
            lines.add(I18n.format(TranslationHelper.translUpgradeKey("tag_list.title")));
            if (tagNames.isEmpty()) {
                lines.add(TextFormatting.DARK_GRAY + I18n.format(TranslationHelper.translUpgradeKey("tag_list.empty")));
            }
            for (String tagName : tagNames) {
                lines.add(TextFormatting.GRAY + "> " + tagName);
            }
            GuiHelper.setTooltipToRender(lines);
        }
    }

    private int getTagListHeight() {
        return Math.max(1, totalSlotRows - 1) * 18;
    }

    private int getTagListWidth() {
        return slotsPerRow * 18;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        if (!isTagsMode()) {
            GuiHelper.renderSlotsBackground(x, y + slotsTopYOffset, slotsPerRow, fullSlotRows, slotsInExtraRow);
        } else {
            GuiHelper.renderSlotsBackground(x, y + tagButtonsYOffset, 1, 1, 0);
            GuiHelper.renderControlBackground(x, y + slotsTopYOffset, getTagListWidth(), getTagListHeight());
        }
    }

    private class TagButton extends Button {
        private final IntConsumer onScroll;

        TagButton(Position position, ButtonDefinition buttonDefinition, IntConsumer onClick, IntConsumer onScroll) {
            super(position, buttonDefinition, onClick);
            this.onScroll = onScroll;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            if (isTagsMode()) {
                super.render(mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean isMouseOver(int mouseX, int mouseY) {
            return isTagsMode() && super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
            if (!isTagsMode()) {
                return false;
            }
            onScroll.accept(delta);
            return true;
        }
    }

    public enum MatchButton {
        ALLOW_LIST,
        PRIMARY_MATCH,
        DURABILITY,
        NBT
    }
}
