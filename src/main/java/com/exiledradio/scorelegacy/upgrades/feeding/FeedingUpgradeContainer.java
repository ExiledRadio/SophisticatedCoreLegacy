/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.feeding;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class FeedingUpgradeContainer extends UpgradeContainerBase<FeedingUpgradeWrapper, FeedingUpgradeContainer> {
    private static final String DATA_HUNGER_LEVEL = "hungerLevel";
    private static final String DATA_FEED_IMMEDIATELY_WHEN_HURT = "feedImmediatelyWhenHurt";
    private final FilterLogicContainer<FilterLogic> filterLogicContainer;

    public FeedingUpgradeContainer(EntityPlayer player, int containerId, FeedingUpgradeWrapper wrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<FeedingUpgradeWrapper, FeedingUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new FilterLogicContainer<>(() -> getUpgradeWrapper().getFilterLogic(), this, slots::add);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_HUNGER_LEVEL)) {
            setFeedAtHungerLevel(HungerLevel.fromName(data.getString(DATA_HUNGER_LEVEL)));
        } else if (data.hasKey(DATA_FEED_IMMEDIATELY_WHEN_HURT)) {
            setFeedImmediatelyWhenHurt(data.getBoolean(DATA_FEED_IMMEDIATELY_WHEN_HURT));
        } else {
            filterLogicContainer.handleMessage(data);
        }
    }

    public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
        return filterLogicContainer;
    }

    public void setFeedAtHungerLevel(HungerLevel hungerLevel) {
        getUpgradeWrapper().setFeedAtHungerLevel(hungerLevel);
        sendDataToServer(() -> NBTHelper.withString(DATA_HUNGER_LEVEL, hungerLevel.getSerializedName()));
    }

    public HungerLevel getFeedAtHungerLevel() {
        return getUpgradeWrapper().getFeedAtHungerLevel();
    }

    public void setFeedImmediatelyWhenHurt(boolean feedImmediatelyWhenHurt) {
        getUpgradeWrapper().setFeedImmediatelyWhenHurt(feedImmediatelyWhenHurt);
        sendBooleanToServer(DATA_FEED_IMMEDIATELY_WHEN_HURT, feedImmediatelyWhenHurt);
    }

    public boolean shouldFeedImmediatelyWhenHurt() {
        return getUpgradeWrapper().shouldFeedImmediatelyWhenHurt();
    }
}
