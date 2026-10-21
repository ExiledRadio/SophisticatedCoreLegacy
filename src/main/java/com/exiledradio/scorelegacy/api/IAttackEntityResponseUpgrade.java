/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.entity.player.EntityPlayer;

/** Reacts to the carrier attacking an entity; true stops other backpacks from reacting. */
public interface IAttackEntityResponseUpgrade {
    boolean onAttackEntity(EntityPlayer player);
}
