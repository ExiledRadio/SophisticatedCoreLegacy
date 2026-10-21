/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import net.minecraft.util.ResourceLocation;

/** 9 columns up to 81 slots, 12 columns above that. */
public class StorageBackgroundProperties {
    public static final StorageBackgroundProperties REGULAR = new StorageBackgroundProperties(9, 0, "textures/gui/backpack_background_9.png");
    public static final StorageBackgroundProperties WIDE = new StorageBackgroundProperties(12, 27, "textures/gui/backpack_background_12.png");

    private final int slotsOnLine;
    private final int playerInventoryXOffset;
    private final ResourceLocation texture;

    private StorageBackgroundProperties(int slotsOnLine, int playerInventoryXOffset, String texture) {
        this.slotsOnLine = slotsOnLine;
        this.playerInventoryXOffset = playerInventoryXOffset;
        this.texture = new ResourceLocation(SophisticatedCoreLegacy.MODID, texture);
    }

    public int getSlotsOnLine() {
        return slotsOnLine;
    }

    public int getPlayerInventoryXOffset() {
        return playerInventoryXOffset;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
