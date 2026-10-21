/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.filter;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;

public class FilterLogicControl<L extends FilterLogic, C extends FilterLogicContainer<L>> extends FilterLogicControlBase<L, FilterLogicContainer.FilterLogicSlot, C> {
    public FilterLogicControl(StorageScreenBase screen, Position position, C filterLogicContainer, int slotsPerRow, MatchButton... showMatchButtons) {
        this(screen, position, filterLogicContainer, slotsPerRow, showMatchButtons.length > 0, showMatchButtons);
    }

    protected FilterLogicControl(StorageScreenBase screen, Position position, C filterLogicContainer, int slotsPerRow, boolean buttonsVisible, MatchButton... showMatchButtons) {
        super(screen, filterLogicContainer, position, buttonsVisible, slotsPerRow, showMatchButtons);
    }

    public static class Basic extends FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> {
        public Basic(StorageScreenBase screen, Position position, FilterLogicContainer<FilterLogic> filterLogicContainer, int slotsPerRow) {
            super(screen, position, filterLogicContainer, slotsPerRow, MatchButton.ALLOW_LIST);
        }
    }

    public static class Advanced extends FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> {
        public Advanced(StorageScreenBase screen, Position position, FilterLogicContainer<FilterLogic> filterLogicContainer, int slotsPerRow) {
            super(screen, position, filterLogicContainer, slotsPerRow, MatchButton.ALLOW_LIST, MatchButton.PRIMARY_MATCH, MatchButton.DURABILITY, MatchButton.NBT);
        }
    }
}
