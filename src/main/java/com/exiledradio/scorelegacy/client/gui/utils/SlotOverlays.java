/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

/** Slot decorations shared by the storage and settings screens: memorized items and colour stripes. */
public final class SlotOverlays {
    private static final int OVERLAY_ALPHA = 80 << 24;

    private SlotOverlays() {}

    /** An empty memory slot shows its item faded out. Coordinates are relative to the GUI. */
    public static void drawMemorizedStack(Minecraft mc, Slot slot, ItemStack memorized) {
        if (memorized.isEmpty() || slot.getHasStack() || slot.xPos < 0) {
            return;
        }
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        mc.getRenderItem().zLevel = 100.0F;
        mc.getRenderItem().renderItemAndEffectIntoGUI(mc.player, memorized, slot.xPos, slot.yPos);
        mc.getRenderItem().zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        // The original's memory overlay: a pale cover that marks the item as "remembered, not there".
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 250);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiHelper.GUI_CONTROLS);
        Gui.drawModalRectWithCustomSizedTexture(slot.xPos, slot.yPos, 77, 0, 16, 16, 256, 256);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    /** Horizontal stripes, one per colour, over the slot. Colours are RGB; an alpha is added. */
    public static void drawColorStripes(Slot slot, List<Integer> colors) {
        if (colors.isEmpty() || slot.xPos < 0) {
            return;
        }
        int stripeHeight = 16 / colors.size();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 260);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        for (int i = 0; i < colors.size(); i++) {
            int color = (colors.get(i) & 0xFFFFFF) | OVERLAY_ALPHA;
            int top = slot.yPos + i * stripeHeight;
            int bottom = i == colors.size() - 1 ? slot.yPos + 16 : top + stripeHeight;
            drawRect(slot.xPos, top, slot.xPos + 16, bottom, color);
        }
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static void drawRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }
}
