/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.controller;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

/** Something the storage tool can link to a controller from a distance: a storage or a storage link (the original's ILinkable). */
public interface ILinkable extends IControllerBoundable {
    default void linkToController(BlockPos controllerPos) {
        if (!canBeLinked() || isLinked()) {
            return;
        }
        World world = getStorageBlockWorld();
        if (world.isRemote || IControllerBoundable.getLoadedController(world, controllerPos) == null) {
            return;
        }
        boolean hadControllerPos = getControllerPos() != null;
        if (!hadControllerPos) {
            setControllerPos(controllerPos);
        }
        runOnController(controller -> {
            if (!controller.addLinkedBlock(getStorageBlockPos()) && !hadControllerPos) {
                removeControllerPos();
            }
        });
    }

    default void unlinkFromController() {
        World world = getStorageBlockWorld();
        if (!world.isRemote && isLinked()) {
            runOnController(controller -> controller.removeLinkedBlock(getStorageBlockPos()));
            removeControllerPos();
            setNotLinked();
        }
    }

    default void setNotLinked() {
    }

    /** Positions the network continues from once this is linked (a storage link: the block it sits on). */
    Set<BlockPos> getConnectablePositions();

    /** Whether this block itself joins the network when linked (storages yes, storage links no). */
    boolean connectLinkedSelf();

    boolean isLinked();

    default boolean canBeLinked() {
        return true;
    }
}
