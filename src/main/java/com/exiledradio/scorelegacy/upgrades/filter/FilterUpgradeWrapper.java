/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.filter;

import com.exiledradio.scorelegacy.api.IIOFilterUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.IContentsFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.INamedEnum;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class FilterUpgradeWrapper extends UpgradeWrapperBase<FilterUpgradeWrapper, FilterUpgradeItem> implements IContentsFilteredUpgrade, IIOFilterUpgrade {
    private final ContentsFilterLogic filterLogic;

    public FilterUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new ContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), storageWrapper::getInventoryHandler);
    }

    public void setDirection(Direction direction) {
        NBTHelper.setEnum(upgrade, "direction", direction);
        save();
        storageWrapper.refreshInventoryForInputOutput();
    }

    public Direction getDirection() {
        return NBTHelper.getEnum(upgrade, "direction", Direction::fromName, Direction.BOTH);
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Nullable
    @Override
    public FilterLogic getInputFilter() {
        Direction direction = getDirection();
        return direction == Direction.INPUT || direction == Direction.BOTH ? filterLogic : null;
    }

    @Nullable
    @Override
    public FilterLogic getOutputFilter() {
        Direction direction = getDirection();
        return direction == Direction.OUTPUT || direction == Direction.BOTH ? filterLogic : null;
    }

    public enum Direction implements INamedEnum {
        BOTH("both"),
        INPUT("input"),
        OUTPUT("output");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public Direction next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Direction fromName(String name) {
            for (Direction value : values()) {
                if (value.name.equals(name)) {
                    return value;
                }
            }
            return BOTH;
        }
    }
}
