/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.config.CookingUpgradeConfig;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** A furnace in a tab: the player fills the input and fuel slots and takes the output. */
public class SmeltingUpgradeWrapper extends UpgradeWrapperBase<SmeltingUpgradeWrapper, SmeltingUpgradeItem> implements ITickableUpgrade, ICookingUpgrade {
    private static final int NOTHING_TO_DO_COOLDOWN = 10;
    private final CookingLogic cookingLogic;

    public SmeltingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        cookingLogic = new CookingLogic(upgrade, upgradeSaveHandler, upgradeItem.getConfig().cookingSpeedMultiplier,
                upgradeItem.getConfig().fuelEfficiencyMultiplier);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) {
            return;
        }
        if (!cookingLogic.tick(world)) {
            setCooldown(world, NOTHING_TO_DO_COOLDOWN);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            cookingLogic.pause();
        }
        super.setEnabled(enabled);
    }

    @Override
    public void onBeforeRemoved() {
        cookingLogic.pause();
    }

    @Override
    public CookingLogic getCookingLogic() {
        return cookingLogic;
    }
}
