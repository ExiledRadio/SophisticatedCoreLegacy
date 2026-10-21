/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.xppump;

import com.exiledradio.scorelegacy.config.XpPumpUpgradeConfig;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.inventory.StorageFluidHandler;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.NBTHelper;
import com.exiledradio.scorelegacy.util.XpHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Keeps the player at a chosen level: above it, experience is pumped into the tank; below it
 * (in output mode), it is pumped back out. Also repairs Mending gear from the tank, and has
 * buttons to store or take a number of levels, or everything, at once.
 */
public class XpPumpUpgradeWrapper extends UpgradeWrapperBase<XpPumpUpgradeWrapper, XpPumpUpgradeItem> implements ITickableUpgrade {
    private static final int DEFAULT_LEVEL = 10;
    private static final int COOLDOWN = 5;
    private static final int ALL_LEVELS = 10000;
    private static final int PLAYER_SEARCH_RANGE = 3;

    public XpPumpUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if ((entity != null && !(entity instanceof EntityPlayer)) || isInCooldown(world)) {
            return;
        }
        if (entity == null) {
            AxisAlignedBB searchBox = new AxisAlignedBB(pos).grow(PLAYER_SEARCH_RANGE);
            for (EntityPlayer player : world.playerEntities) {
                if (searchBox.contains(player.getPositionVector())) {
                    interactWithPlayer(player);
                    mendItems(player);
                }
            }
        } else {
            interactWithPlayer((EntityPlayer) entity);
            mendItems((EntityPlayer) entity);
        }
        setCooldown(world, COOLDOWN);
    }

    private void mendItems(EntityPlayer player) {
        StorageFluidHandler fluidHandler = storageWrapper.getFluidHandler();
        if (!upgradeItem.getConfig().mendingOn || !shouldMendItems() || fluidHandler == null) {
            return;
        }
        ItemStack stack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, player);
        if (stack.isEmpty() || !stack.isItemDamaged()) {
            return;
        }
        float repairRatio = stack.getItem().getXpRepairRatio(stack);
        if (repairRatio <= 0) {
            return;
        }
        float xpToTryDrain = Math.min(upgradeItem.getConfig().maxXpPointsPerMending, stack.getItemDamage() / repairRatio);
        if (xpToTryDrain <= 0) {
            return;
        }
        int drained = fluidHandler.drainExperience(XpHelper.experienceToLiquid(xpToTryDrain), true, false);
        int durabilityToRepair = (int) (XpHelper.liquidToExperience(drained) * repairRatio);
        stack.setItemDamage(Math.max(0, stack.getItemDamage() - durabilityToRepair));
    }

    private void interactWithPlayer(EntityPlayer player) {
        StorageFluidHandler fluidHandler = storageWrapper.getFluidHandler();
        AutomationDirection direction = getDirection();
        if (fluidHandler == null || direction == AutomationDirection.OFF) {
            return;
        }
        int level = getLevel();
        if (direction == AutomationDirection.INPUT) {
            if (level < player.experienceLevel || (level == player.experienceLevel && player.experience > 0)) {
                tryFillTankWithPlayerExperience(player, fluidHandler, level, false);
            }
        } else if (level > player.experienceLevel) {
            tryGivePlayerExperienceFromTank(player, fluidHandler, level, false);
        }
    }

    private static void tryGivePlayerExperienceFromTank(EntityPlayer player, StorageFluidHandler fluidHandler, int stopAtLevel, boolean ignoreInOutLimit) {
        int maxXpPointsToGive = XpHelper.getExperienceForLevel(stopAtLevel) - XpHelper.getPlayerTotalExperience(player);
        if (maxXpPointsToGive <= 0) {
            return;
        }
        // Whole points only: what a tank holds beyond a multiple of 20 mB stays in it.
        int available = fluidHandler.drainExperience(XpHelper.experienceToLiquid(maxXpPointsToGive), false, ignoreInOutLimit);
        int points = (int) XpHelper.liquidToExperience(available);
        if (points > 0) {
            fluidHandler.drainExperience(XpHelper.experienceToLiquid(points), true, ignoreInOutLimit);
            player.addScore(points);
            XpHelper.setPlayerTotalExperience(player, XpHelper.getPlayerTotalExperience(player) + points);
        }
    }

    private static void tryFillTankWithPlayerExperience(EntityPlayer player, StorageFluidHandler fluidHandler, int stopAtLevel, boolean ignoreInOutLimit) {
        int total = XpHelper.getPlayerTotalExperience(player);
        int maxXpPointsToTake = total - XpHelper.getExperienceForLevel(stopAtLevel);
        if (maxXpPointsToTake <= 0) {
            return;
        }
        // Whole points only, so no experience is created or lost in the conversion.
        int filled = fluidHandler.fillExperience(XpHelper.experienceToLiquid(maxXpPointsToTake), false, ignoreInOutLimit);
        int points = (int) XpHelper.liquidToExperience(filled);
        if (points > 0) {
            fluidHandler.fillExperience(XpHelper.experienceToLiquid(points), true, ignoreInOutLimit);
            XpHelper.setPlayerTotalExperience(player, total - points);
        }
    }

    public void takeLevelsFromPlayer(EntityPlayer player) {
        StorageFluidHandler fluidHandler = storageWrapper.getFluidHandler();
        if (fluidHandler != null) {
            tryFillTankWithPlayerExperience(player, fluidHandler, Math.max(player.experienceLevel - getLevelsToStore(), 0), true);
        }
    }

    public void takeAllExperienceFromPlayer(EntityPlayer player) {
        StorageFluidHandler fluidHandler = storageWrapper.getFluidHandler();
        if (fluidHandler != null) {
            tryFillTankWithPlayerExperience(player, fluidHandler, 0, true);
        }
    }

    public void giveLevelsToPlayer(EntityPlayer player) {
        StorageFluidHandler fluidHandler = storageWrapper.getFluidHandler();
        if (fluidHandler != null) {
            tryGivePlayerExperienceFromTank(player, fluidHandler, player.experienceLevel + getLevelsToTake(), true);
        }
    }

    public void giveAllExperienceToPlayer(EntityPlayer player) {
        StorageFluidHandler fluidHandler = storageWrapper.getFluidHandler();
        if (fluidHandler != null) {
            tryGivePlayerExperienceFromTank(player, fluidHandler, ALL_LEVELS, true);
        }
    }

    public AutomationDirection getDirection() {
        return NBTHelper.getEnum(upgrade, "direction", AutomationDirection::fromName, AutomationDirection.INPUT);
    }

    public void setDirection(AutomationDirection direction) {
        NBTHelper.setEnum(upgrade, "direction", direction);
        save();
    }

    public int getLevel() {
        return NBTHelper.getInt(upgrade, "level", DEFAULT_LEVEL);
    }

    public void setLevel(int level) {
        NBTHelper.setInt(upgrade, "level", level);
        save();
    }

    public int getLevelsToStore() {
        return NBTHelper.getInt(upgrade, "levelsToStore", 1);
    }

    public void setLevelsToStore(int levelsToStore) {
        NBTHelper.setInt(upgrade, "levelsToStore", levelsToStore);
        save();
    }

    public int getLevelsToTake() {
        return NBTHelper.getInt(upgrade, "levelsToTake", 1);
    }

    public void setLevelsToTake(int levelsToTake) {
        NBTHelper.setInt(upgrade, "levelsToTake", levelsToTake);
        save();
    }

    public boolean shouldMendItems() {
        return NBTHelper.getBoolean(upgrade, "mendItems", true);
    }

    public void setMendItems(boolean mendItems) {
        NBTHelper.setBoolean(upgrade, "mendItems", mendItems);
        save();
    }
}
