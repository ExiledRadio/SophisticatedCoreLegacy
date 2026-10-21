/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.upgrades.BatteryInventoryPart;
import com.exiledradio.scorelegacy.client.gui.upgrades.CookingUpgradeTabs;
import com.exiledradio.scorelegacy.client.gui.upgrades.CraftingUpgradeTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.IOSlotsTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.JukeboxUpgradeTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.PumpUpgradeTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.TankInventoryPart;
import com.exiledradio.scorelegacy.client.gui.upgrades.XpPumpUpgradeTab;
import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeGuiManager;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.filter.ContentsFilterControl;
import com.exiledradio.scorelegacy.client.gui.filter.FilterLogicControl;
import com.exiledradio.scorelegacy.client.gui.filter.FilterLogicControlBase;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.upgrades.ContentsFilteredUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import com.exiledradio.scorelegacy.upgrades.FilteredUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.compacting.CompactingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.feeding.FeedingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.feeding.HungerLevel;
import com.exiledradio.scorelegacy.upgrades.filter.FilterUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.filter.FilterUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.magnet.MagnetUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.pickup.PickupUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.voiding.VoidUpgradeContainer;
import com.google.common.collect.ImmutableMap;
import com.exiledradio.scorelegacy.upgrades.pump.PumpUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeContainer;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.createToggleButtonDefinition;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.getBooleanStateData;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.icon;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.iconWithDetail;

/**
 * Settings tabs shared by every storage mod. Each mod registers them for its own upgrade
 * items with UpgradeGuiManager.registerTab.
 */
public final class CoreUpgradeTabs {
    public static final int TOP_ROW = 24;
    public static final int SECOND_ROW = 44;

    public static final ButtonDefinition.Toggle<FilterUpgradeWrapper.Direction> DIRECTION = createToggleButtonDefinition(ImmutableMap.of(
            FilterUpgradeWrapper.Direction.BOTH, icon(0, 32, "direction_both"),
            FilterUpgradeWrapper.Direction.INPUT, icon(16, 32, "direction_input"),
            FilterUpgradeWrapper.Direction.OUTPUT, icon(32, 32, "direction_output")));

    public static final ButtonDefinition.Toggle<Boolean> PICKUP_ITEMS = createToggleButtonDefinition(
            getBooleanStateData(icon(128, 48, "pickup_items"), icon(144, 48, "do_not_pickup_items")));

    public static final ButtonDefinition.Toggle<Boolean> PICKUP_XP = createToggleButtonDefinition(
            getBooleanStateData(iconWithDetail(96, 48, "pickup_xp"), icon(112, 48, "do_not_pickup_xp")));

    public static final ButtonDefinition.Toggle<Boolean> VOID_OVERFLOW = createToggleButtonDefinition(
            getBooleanStateData(iconWithDetail(224, 16, "void_overflow"), icon(208, 16, "void_any")));

    public static final ButtonDefinition.Toggle<HungerLevel> HUNGER_LEVEL = createToggleButtonDefinition(ImmutableMap.of(
            HungerLevel.ANY, icon(128, 0, "hunger_level_any"),
            HungerLevel.HALF, icon(112, 0, "hunger_level_half"),
            HungerLevel.FULL, icon(96, 0, "hunger_level_full")));

    public static final ButtonDefinition.Toggle<Boolean> FEED_IMMEDIATELY_WHEN_HURT = createToggleButtonDefinition(
            getBooleanStateData(icon(96, 16, "feed_immediately_when_hurt"), icon(112, 16, "do_not_consider_health")));

    public static final ButtonDefinition.Toggle<Boolean> COMPACT_UNCRAFTABLE = createToggleButtonDefinition(ImmutableMap.of(
            false, icon(80, 48, "compact_only_uncraftable"),
            true, icon(80, 32, "compact_anything")));

    /** Tabs whose content ends with a filter control; the filter decides where slots go. */
    public abstract static class FilteredTab<C extends UpgradeContainerBase<?, ?>> extends UpgradeSettingsTab<C> {
        protected FilterLogicControlBase<?, ?, ?> filterLogicControl;

        public FilteredTab(C container, Position position, StorageScreenBase screen, String name) {
            super(container, position, screen, name);
        }

        @Override
        protected void moveSlotsToTab() {
            filterLogicControl.moveSlotsToView();
        }
    }

    public static class ContentsTab<W extends com.exiledradio.scorelegacy.api.IUpgradeWrapper & com.exiledradio.scorelegacy.upgrades.IContentsFilteredUpgrade>
            extends FilteredTab<ContentsFilteredUpgradeContainer<W>> {
        public ContentsTab(ContentsFilteredUpgradeContainer<W> container, Position position, StorageScreenBase screen, String name, boolean advanced, int slotsInRow) {
            super(container, position, screen, name);
            Position at = new Position(x + 3, y + TOP_ROW);
            filterLogicControl = addHideableChild(advanced
                    ? new ContentsFilterControl.Advanced(screen, at, container.getFilterLogicContainer(), slotsInRow)
                    : new ContentsFilterControl.Basic(screen, at, container.getFilterLogicContainer(), slotsInRow));
        }
    }

    public static class FilterTab extends FilteredTab<FilterUpgradeContainer> {
        public FilterTab(FilterUpgradeContainer container, Position position, StorageScreenBase screen, String name, boolean advanced, int slotsInRow) {
            super(container, position, screen, name);
            addHideableChild(new ToggleButton<>(new Position(x + 3, y + TOP_ROW), DIRECTION,
                    button -> container.setDirection(container.getDirection().next()), container::getDirection));
            Position at = new Position(x + 3, y + SECOND_ROW);
            filterLogicControl = addHideableChild(advanced
                    ? new ContentsFilterControl.Advanced(screen, at, container.getFilterLogicContainer(), slotsInRow)
                    : new ContentsFilterControl.Basic(screen, at, container.getFilterLogicContainer(), slotsInRow));
        }
    }

    public static class MagnetTab extends FilteredTab<MagnetUpgradeContainer> {
        public MagnetTab(MagnetUpgradeContainer container, Position position, StorageScreenBase screen, String name, boolean advanced, int slotsInRow) {
            super(container, position, screen, name);
            addHideableChild(new ToggleButton<>(new Position(x + 3, y + TOP_ROW), PICKUP_ITEMS,
                    button -> container.setPickupItems(!container.shouldPickupItems()), container::shouldPickupItems));
            addHideableChild(new ToggleButton<>(new Position(x + 21, y + TOP_ROW), PICKUP_XP,
                    button -> container.setPickupXp(!container.shouldPickupXp()), container::shouldPickupXp));
            Position at = new Position(x + 3, y + SECOND_ROW);
            filterLogicControl = addHideableChild(advanced
                    ? new ContentsFilterControl.Advanced(screen, at, container.getFilterLogicContainer(), slotsInRow)
                    : new ContentsFilterControl.Basic(screen, at, container.getFilterLogicContainer(), slotsInRow));
        }
    }

    public static class VoidTab extends FilteredTab<VoidUpgradeContainer> {
        public VoidTab(VoidUpgradeContainer container, Position position, StorageScreenBase screen, String name, boolean advanced, int slotsInRow) {
            super(container, position, screen, name);
            addHideableChild(new ToggleButton<>(new Position(x + 3, y + TOP_ROW), ButtonDefinitions.WORK_IN_GUI,
                    button -> container.setShouldWorkInGUI(!container.shouldWorkInGUI()), container::shouldWorkInGUI));
            addHideableChild(new ToggleButton<>(new Position(x + 21, y + TOP_ROW), VOID_OVERFLOW,
                    button -> container.setShouldVoidOverflow(!container.shouldVoidOverflow()), container::shouldVoidOverflow));
            Position at = new Position(x + 3, y + SECOND_ROW);
            filterLogicControl = addHideableChild(advanced
                    ? new FilterLogicControl.Advanced(screen, at, container.getFilterLogicContainer(), slotsInRow)
                    : new FilterLogicControl.Basic(screen, at, container.getFilterLogicContainer(), slotsInRow));
        }
    }

    public static class FeedingTab extends FilteredTab<FeedingUpgradeContainer> {
        public FeedingTab(FeedingUpgradeContainer container, Position position, StorageScreenBase screen, String name, boolean advanced, int slotsInRow) {
            super(container, position, screen, name);
            if (advanced) {
                addHideableChild(new ToggleButton<>(new Position(x + 3, y + TOP_ROW), HUNGER_LEVEL,
                        button -> container.setFeedAtHungerLevel(container.getFeedAtHungerLevel().next()), container::getFeedAtHungerLevel));
                addHideableChild(new ToggleButton<>(new Position(x + 21, y + TOP_ROW), FEED_IMMEDIATELY_WHEN_HURT,
                        button -> container.setFeedImmediatelyWhenHurt(!container.shouldFeedImmediatelyWhenHurt()), container::shouldFeedImmediatelyWhenHurt));
                filterLogicControl = addHideableChild(new FilterLogicControl.Advanced(screen, new Position(x + 3, y + SECOND_ROW), container.getFilterLogicContainer(), slotsInRow));
            } else {
                filterLogicControl = addHideableChild(new FilterLogicControl.Basic(screen, new Position(x + 3, y + TOP_ROW), container.getFilterLogicContainer(), slotsInRow));
            }
        }
    }

    public static class CompactingTab extends FilteredTab<CompactingUpgradeContainer> {
        public CompactingTab(CompactingUpgradeContainer container, Position position, StorageScreenBase screen, String name, boolean advanced, int slotsInRow) {
            super(container, position, screen, name);
            addHideableChild(new ToggleButton<>(new Position(x + 3, y + TOP_ROW), COMPACT_UNCRAFTABLE,
                    button -> container.setCompactNonUncraftable(!container.shouldCompactNonUncraftable()), container::shouldCompactNonUncraftable));
            addHideableChild(new ToggleButton<>(new Position(x + 21, y + TOP_ROW), ButtonDefinitions.WORK_IN_GUI,
                    button -> container.setShouldWorkInGUI(!container.shouldWorkInGUI()), container::shouldWorkInGUI));
            Position at = new Position(x + 3, y + SECOND_ROW);
            filterLogicControl = addHideableChild(advanced
                    ? new FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>>(screen, at, container.getFilterLogicContainer(), slotsInRow,
                    FilterLogicControlBase.MatchButton.ALLOW_LIST, FilterLogicControlBase.MatchButton.PRIMARY_MATCH)
                    : new FilterLogicControl.Basic(screen, at, container.getFilterLogicContainer(), slotsInRow));
        }
    }

    private CoreUpgradeTabs() {}
}
