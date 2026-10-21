/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.utils;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class GuiHelper {
    public static final ResourceLocation GUI_CONTROLS = new ResourceLocation(SophisticatedCoreLegacy.MODID, "textures/gui/gui_controls.png");
    public static final ResourceLocation ICONS = new ResourceLocation(SophisticatedCoreLegacy.MODID, "textures/gui/icons.png");
    public static final ResourceLocation SLOTS_BACKGROUND = new ResourceLocation(SophisticatedCoreLegacy.MODID, "textures/gui/slots_background.png");
    public static final TextureBlitData DEFAULT_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(47, 0), Dimension.SQUARE_18);
    public static final TextureBlitData DEFAULT_BUTTON_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(29, 0), Dimension.SQUARE_18);
    public static final TextureBlitData SMALL_BUTTON_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(29, 18), Dimension.SQUARE_12);
    public static final TextureBlitData CRAFTING_RESULT_SLOT = new TextureBlitData(GUI_CONTROLS, new UV(71, 216), new Dimension(26, 26));
    public static final TextureBlitData SMALL_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(41, 18), Dimension.SQUARE_12);

    private static List<String> tooltipToRender = Collections.emptyList();

    private GuiHelper() {}

    public static void blit(int x, int y, TextureBlitData texData) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texData.getTextureName());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(x + texData.getXOffset(), y + texData.getYOffset(), texData.getU(), texData.getV(),
                texData.getWidth(), texData.getHeight(), texData.getTextureWidth(), texData.getTextureHeight());
    }

    private static final TextureBlitData BAR_BACKGROUND_TOP = new TextureBlitData(GUI_CONTROLS, new UV(29, 30), Dimension.SQUARE_18);
    private static final TextureBlitData BAR_BACKGROUND_MIDDLE = new TextureBlitData(GUI_CONTROLS, new UV(29, 48), Dimension.SQUARE_18);
    private static final TextureBlitData BAR_BACKGROUND_BOTTOM = new TextureBlitData(GUI_CONTROLS, new UV(29, 66), Dimension.SQUARE_18);

    /** The frame of a tank or battery: top, as many middles as fit, bottom. */
    public static void renderBarBackground(int x, int y, int height) {
        blit(x, y, BAR_BACKGROUND_TOP);
        int yOffset = 18;
        for (int i = 0; i < (height - 36) / 18; i++) {
            blit(x, y + yOffset, BAR_BACKGROUND_MIDDLE);
            yOffset += 18;
        }
        blit(x, y + yOffset, BAR_BACKGROUND_BOTTOM);
    }

    /** A blit tinted with the given colour (0-1 per channel). */
    public static void blit(int x, int y, TextureBlitData texData, float red, float green, float blue) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texData.getTextureName());
        GlStateManager.color(red, green, blue, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x + texData.getXOffset(), y + texData.getYOffset(), texData.getU(), texData.getV(),
                texData.getWidth(), texData.getHeight(), texData.getTextureWidth(), texData.getTextureHeight());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** A 16 wide column of a fluid's still texture, tiled upwards from the bottom, tinted with its colour. */
    public static void renderTiledFluid(TextureAtlasSprite sprite, int color, int x, int y, int height) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int bottom = y + height;
        int remaining = height;
        while (remaining > 0) {
            int segment = Math.min(16, remaining);
            int top = bottom - segment;
            double minU = sprite.getMinU();
            double maxU = sprite.getMaxU();
            double minV = sprite.getInterpolatedV(16 - segment);
            double maxV = sprite.getMaxV();
            buffer.pos(x, bottom, 100).tex(minU, maxV).endVertex();
            buffer.pos(x + 16, bottom, 100).tex(maxU, maxV).endVertex();
            buffer.pos(x + 16, top, 100).tex(maxU, minV).endVertex();
            buffer.pos(x, top, 100).tex(minU, minV).endVertex();
            bottom = top;
            remaining -= segment;
        }
        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, 256, 256);
    }

    public static void renderSlotsBackground(int x, int y, int slotsInRow, int fullSlotRows, int extraRowSlots) {
        if (fullSlotRows > 0) {
            blit(SLOTS_BACKGROUND, x, y, 0, 0, slotsInRow * 18, fullSlotRows * 18);
        }
        if (extraRowSlots > 0) {
            blit(SLOTS_BACKGROUND, x, y + fullSlotRows * 18, 0, 0, extraRowSlots * 18, 18);
        }
    }

    /** A stretchable dark panel (the tag list background), drawn from its four corners. */
    public static void renderControlBackground(int x, int y, int renderWidth, int renderHeight) {
        int u = 29;
        int v = 146;
        int textureBgWidth = 66;
        int textureBgHeight = 56;
        int halfWidth = renderWidth / 2;
        int halfHeight = renderHeight / 2;
        blit(GUI_CONTROLS, x, y, u, v, halfWidth, halfHeight);
        blit(GUI_CONTROLS, x, y + halfHeight, u, v + textureBgHeight - halfHeight, halfWidth, halfHeight);
        blit(GUI_CONTROLS, x + halfWidth, y, u + textureBgWidth - halfWidth, v, halfWidth, halfHeight);
        blit(GUI_CONTROLS, x + halfWidth, y + halfHeight, u + textureBgWidth - halfWidth, v + textureBgHeight - halfHeight, halfWidth, halfHeight);
    }

    public static void renderItemInGUI(ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        RenderHelper.disableStandardItemLighting();
    }

    public static void setTooltipToRender(List<String> tooltip) {
        tooltipToRender = tooltip;
    }

    /** Draws (and clears) whatever tooltip a widget asked for during this frame. */
    public static void renderTooltip(int mouseX, int mouseY) {
        if (tooltipToRender.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        GuiUtils.drawHoveringText(tooltipToRender, mouseX, mouseY, resolution.getScaledWidth(), resolution.getScaledHeight(), 200, mc.fontRenderer);
        tooltipToRender = Collections.emptyList();
    }

    public static ToggleButton.StateData getButtonStateData(UV uv, Dimension dimension, Position offset, Supplier<List<String>> tooltip) {
        return new ToggleButton.StateData(new TextureBlitData(ICONS, offset, Dimension.SQUARE_256, uv, dimension), tooltip);
    }

    public static ToggleButton.StateData getButtonStateData(UV uv, String tooltipKey, Dimension dimension, Position offset) {
        return getButtonStateData(uv, dimension, offset, TranslationHelper.tooltip(tooltipKey));
    }

    public static ToggleButton.StateData getButtonStateData(UV uv, String tooltipKey, Dimension dimension) {
        return getButtonStateData(uv, tooltipKey, dimension, new Position(0, 0));
    }
}
