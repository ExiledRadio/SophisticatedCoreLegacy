/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.xppump;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class XpPumpUpgradeContainer extends UpgradeContainerBase<XpPumpUpgradeWrapper, XpPumpUpgradeContainer> {
    private static final String DATA_LEVEL = "level";
    private static final String DATA_DIRECTION = "direction";
    private static final String DATA_ACTION = "action";
    private static final String ACTION_TAKE_LEVELS = "take";
    private static final String ACTION_STORE_LEVELS_FROM_PLAYER = "store";
    private static final String ACTION_STORE_ALL_PLAYERS_EXPERIENCE = "storeAll";
    private static final String ACTION_TAKE_ALL_LEVELS = "takeAll";
    private static final String DATA_LEVELS_TO_STORE = "levelsToStore";
    private static final String DATA_LEVELS_TO_TAKE = "levelsToTake";
    private static final String DATA_MEND_ITEMS = "mendItems";

    public XpPumpUpgradeContainer(EntityPlayer player, int containerId, XpPumpUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                  UpgradeContainerType<XpPumpUpgradeWrapper, XpPumpUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
    }

    public void setDirection(AutomationDirection direction) {
        getUpgradeWrapper().setDirection(direction);
        sendDataToServer(() -> NBTHelper.withString(DATA_DIRECTION, direction.getSerializedName()));
    }

    public AutomationDirection getDirection() {
        return getUpgradeWrapper().getDirection();
    }

    public int getLevel() {
        return getUpgradeWrapper().getLevel();
    }

    public void setLevel(int level) {
        if (level < 0) {
            return;
        }
        getUpgradeWrapper().setLevel(level);
        sendDataToServer(() -> NBTHelper.withInt(DATA_LEVEL, level));
    }

    public int getLevelsToStore() {
        return getUpgradeWrapper().getLevelsToStore();
    }

    public void setLevelsToStore(int levelsToStore) {
        if (levelsToStore < 1) {
            return;
        }
        getUpgradeWrapper().setLevelsToStore(levelsToStore);
        sendDataToServer(() -> NBTHelper.withInt(DATA_LEVELS_TO_STORE, levelsToStore));
    }

    public int getLevelsToTake() {
        return getUpgradeWrapper().getLevelsToTake();
    }

    public void setLevelsToTake(int levelsToTake) {
        if (levelsToTake < 1) {
            return;
        }
        getUpgradeWrapper().setLevelsToTake(levelsToTake);
        sendDataToServer(() -> NBTHelper.withInt(DATA_LEVELS_TO_TAKE, levelsToTake));
    }

    public boolean shouldMendItems() {
        return getUpgradeWrapper().shouldMendItems();
    }

    public void setMendItems(boolean mendItems) {
        getUpgradeWrapper().setMendItems(mendItems);
        sendBooleanToServer(DATA_MEND_ITEMS, mendItems);
    }

    public void takeLevels() {
        sendDataToServer(() -> NBTHelper.withString(DATA_ACTION, ACTION_TAKE_LEVELS));
    }

    public void storeLevels() {
        sendDataToServer(() -> NBTHelper.withString(DATA_ACTION, ACTION_STORE_LEVELS_FROM_PLAYER));
    }

    public void storeAllExperience() {
        sendDataToServer(() -> NBTHelper.withString(DATA_ACTION, ACTION_STORE_ALL_PLAYERS_EXPERIENCE));
    }

    public void takeAllExperience() {
        sendDataToServer(() -> NBTHelper.withString(DATA_ACTION, ACTION_TAKE_ALL_LEVELS));
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_DIRECTION)) {
            setDirection(AutomationDirection.fromName(data.getString(DATA_DIRECTION)));
        } else if (data.hasKey(DATA_LEVEL)) {
            setLevel(data.getInteger(DATA_LEVEL));
        } else if (data.hasKey(DATA_LEVELS_TO_STORE)) {
            setLevelsToStore(data.getInteger(DATA_LEVELS_TO_STORE));
        } else if (data.hasKey(DATA_LEVELS_TO_TAKE)) {
            setLevelsToTake(data.getInteger(DATA_LEVELS_TO_TAKE));
        } else if (data.hasKey(DATA_MEND_ITEMS)) {
            setMendItems(data.getBoolean(DATA_MEND_ITEMS));
        } else if (data.hasKey(DATA_ACTION)) {
            switch (data.getString(DATA_ACTION)) {
                case ACTION_TAKE_LEVELS:
                    getUpgradeWrapper().giveLevelsToPlayer(player);
                    break;
                case ACTION_STORE_LEVELS_FROM_PLAYER:
                    getUpgradeWrapper().takeLevelsFromPlayer(player);
                    break;
                case ACTION_TAKE_ALL_LEVELS:
                    getUpgradeWrapper().giveAllExperienceToPlayer(player);
                    break;
                case ACTION_STORE_ALL_PLAYERS_EXPERIENCE:
                    getUpgradeWrapper().takeAllExperienceFromPlayer(player);
                    break;
                default:
                    //noop
            }
        }
    }
}
