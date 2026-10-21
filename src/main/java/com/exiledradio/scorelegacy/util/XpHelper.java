/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.util;

import net.minecraft.entity.player.EntityPlayer;

/** Experience points and levels, and the 20 mB per point ratio liquid experience uses in 1.12 mods. */
public final class XpHelper {
    private static final int RATIO = 20;

    private XpHelper() {}

    public static float liquidToExperience(int liquid) {
        return (float) liquid / RATIO;
    }

    public static int experienceToLiquid(float xp) {
        return (int) (xp * RATIO);
    }

    public static int getExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level < 16) {
            return level * (12 + level * 2) / 2;
        } else if (level < 31) {
            return (level - 15) * (69 + (level - 15) * 5) / 2 + 315;
        }
        return (level - 30) * (215 + (level - 30) * 9) / 2 + 1395;
    }

    public static int getExperienceLimitOnLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    public static int getLevelForExperience(int experience) {
        int level = 0;
        while (getExperienceForLevel(level) <= experience) {
            level++;
        }
        return level - 1;
    }

    public static double getLevelsForExperience(int experience) {
        int baseLevel = getLevelForExperience(experience);
        int remainingExperience = experience - getExperienceForLevel(baseLevel);
        return baseLevel + ((double) remainingExperience / getExperienceLimitOnLevel(baseLevel));
    }

    /**
     * Progress within the level is a float; truncating it (as the original does) loses a point
     * now and then when experience is moved in steps, so it is rounded.
     */
    public static int getPlayerTotalExperience(EntityPlayer player) {
        return getExperienceForLevel(player.experienceLevel) + Math.round(player.experience * player.xpBarCap());
    }

    /**
     * Sets the player's experience to an exact total. 1.12's addExperience can't take negative
     * amounts, and when it crosses many levels at once its float maths lands a hair short (19
     * levels and a full bar instead of 20), so level and progress are set directly.
     */
    public static void setPlayerTotalExperience(EntityPlayer player, int total) {
        int clamped = Math.max(0, total);
        int level = getLevelForExperience(clamped);
        player.experienceLevel = level;
        player.experience = (float) (clamped - getExperienceForLevel(level)) / player.xpBarCap();
        player.experienceTotal = clamped;
    }
}
