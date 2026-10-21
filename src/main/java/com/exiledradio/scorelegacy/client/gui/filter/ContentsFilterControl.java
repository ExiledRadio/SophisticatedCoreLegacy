/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.filter;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogic;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogicContainer;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterType;
import com.exiledradio.scorelegacy.upgrades.PrimaryMatch;
import com.google.common.collect.ImmutableMap;

/** A filter whose first button cycles allow / block / "anything already in the backpack". */
public abstract class ContentsFilterControl extends FilterLogicControl<ContentsFilterLogic, ContentsFilterLogicContainer> {
    public static final ButtonDefinition.Toggle<ContentsFilterType> BACKPACK_CONTENTS_FILTER_TYPE = ButtonDefinitions.createToggleButtonDefinition(
            ImmutableMap.of(
                    ContentsFilterType.ALLOW, ButtonDefinitions.icon(0, 0, "allow"),
                    ContentsFilterType.BLOCK, ButtonDefinitions.icon(16, 0, "block"),
                    ContentsFilterType.BACKPACK, ButtonDefinitions.icon(80, 16, "match_backpack_contents")));

    protected ContentsFilterControl(StorageScreenBase screen, Position position, ContentsFilterLogicContainer filterLogicContainer, int slotsPerRow, MatchButton... matchButtons) {
        super(screen, position, filterLogicContainer, slotsPerRow, true, matchButtons);
        addChild(new ToggleButton<>(new Position(x, y), getFilterTypeDefinition(screen.getContainer().getStorageWrapper().getMatchContentsButtonKey()),
                button -> updateFilterType(), container::getFilterType));
    }

    /** The filter type button with the storage's own wording for its third state. */
    private static ButtonDefinition.Toggle<ContentsFilterType> getFilterTypeDefinition(String matchContentsKey) {
        if (matchContentsKey.equals(com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper.translUpgradeButton("match_backpack_contents"))) {
            return BACKPACK_CONTENTS_FILTER_TYPE;
        }
        return ButtonDefinitions.createToggleButtonDefinition(ImmutableMap.of(
                ContentsFilterType.ALLOW, ButtonDefinitions.icon(0, 0, "allow"),
                ContentsFilterType.BLOCK, ButtonDefinitions.icon(16, 0, "block"),
                ContentsFilterType.BACKPACK, com.exiledradio.scorelegacy.client.gui.utils.GuiHelper.getButtonStateData(
                        new com.exiledradio.scorelegacy.client.gui.utils.UV(80, 16), matchContentsKey,
                        com.exiledradio.scorelegacy.client.gui.utils.Dimension.SQUARE_16, new Position(1, 1))));
    }

    private void updateFilterType() {
        ContentsFilterType next = container.getFilterType().next();
        if (container.getPrimaryMatch() == PrimaryMatch.TAGS && next == ContentsFilterType.BACKPACK) {
            next = next.next();
        }
        container.setFilterType(next);
        boolean slotsEnabled = next != ContentsFilterType.BACKPACK;
        container.getFilterSlots().forEach(slot -> slot.setEnabled(slotsEnabled));
    }

    @Override
    protected void onTagsMatchSelected() {
        if (container.getFilterType() == ContentsFilterType.BACKPACK) {
            updateFilterType();
        }
    }

    public static class Basic extends ContentsFilterControl {
        public Basic(StorageScreenBase screen, Position position, ContentsFilterLogicContainer filterLogicContainer, int slotsPerRow) {
            super(screen, position, filterLogicContainer, slotsPerRow);
        }
    }

    public static class Advanced extends ContentsFilterControl {
        public Advanced(StorageScreenBase screen, Position position, ContentsFilterLogicContainer filterLogicContainer, int slotsPerRow) {
            super(screen, position, filterLogicContainer, slotsPerRow, MatchButton.PRIMARY_MATCH, MatchButton.DURABILITY, MatchButton.NBT);
        }
    }
}
