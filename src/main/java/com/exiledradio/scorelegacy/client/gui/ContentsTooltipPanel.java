/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Shift contents panel drawn under an item's tooltip (backpacks, packed storages, shulker
 * boxes): upgrades, then the contents merged by kind. The summary holds "upgrades" (stacks,
 * optional "switchEnabled"), "items" (stacks with "realCount"), optional "multiplier", "energy"
 * and "tanks"; texts come from {@code keyPrefix} + "upgrades", "inventory", "empty"...
 */
public final class ContentsTooltipPanel {
    private static final int STACKS_ON_LINE = 9;
    private static final int STACK_WIDTH = 18;
    private static final int BACKGROUND = 0xF0100010;
    private static final int BORDER_TOP = 0x505000FF;
    private static final int BORDER_BOTTOM = 0x5028007F;

    private ContentsTooltipPanel() {}

    /** Under the tooltip, or above it when that would leave the screen; kept inside the screen sideways. */
    public static void draw(Minecraft mc, FontRenderer font, int tooltipX, int tooltipY, int tooltipHeight, @Nullable NBTTagCompound summary, String keyPrefix) {
        List<String> infoLines = new ArrayList<>();
        List<ItemStack> upgrades = new ArrayList<>();
        List<Boolean> upgradeSwitches = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        if (summary != null) {
            NBTTagList upgradeList = summary.getTagList("upgrades", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < upgradeList.tagCount(); i++) {
                NBTTagCompound entry = upgradeList.getCompoundTagAt(i);
                upgrades.add(new ItemStack(entry));
                upgradeSwitches.add(entry.hasKey("switchEnabled") ? entry.getBoolean("switchEnabled") : null);
            }
            NBTTagList itemList = summary.getTagList("items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < itemList.tagCount(); i++) {
                NBTTagCompound entry = itemList.getCompoundTagAt(i);
                ItemStack item = new ItemStack(entry);
                item.setCount(entry.getInteger("realCount"));
                items.add(item);
            }
            int multiplier = summary.getInteger("multiplier");
            if (multiplier > 1) {
                infoLines.add(TextFormatting.GREEN + I18n.format(keyPrefix + "stack_multiplier", TextFormatting.WHITE + Integer.toString(multiplier)));
            }
            if (summary.hasKey("energy")) {
                infoLines.add(TextFormatting.RED + I18n.format(keyPrefix + "energy", TextFormatting.WHITE + abbreviate(summary.getInteger("energy")) + TextFormatting.RED));
            }
            NBTTagList tanks = summary.getTagList("tanks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tanks.tagCount(); i++) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(tanks.getCompoundTagAt(i));
                infoLines.add(fluid == null ? TextFormatting.BLUE + I18n.format(keyPrefix + "fluid_empty")
                        : I18n.format(keyPrefix + "fluid", TextFormatting.WHITE + abbreviate(fluid.amount), TextFormatting.BLUE + fluid.getLocalizedName()));
            }
        }

        int width = 0;
        for (String line : infoLines) {
            width = Math.max(width, font.getStringWidth(line));
        }
        int upgradesWidth = 0;
        for (Boolean upgradeSwitch : upgradeSwitches) {
            upgradesWidth += STACK_WIDTH + (upgradeSwitch != null ? 4 : 0);
        }
        width = Math.max(width, upgradesWidth);
        width = Math.max(width, Math.min(items.size(), STACKS_ON_LINE) * STACK_WIDTH);
        boolean empty = upgrades.isEmpty() && items.isEmpty();
        String emptyLine = TextFormatting.YELLOW + I18n.format(keyPrefix + "empty");
        if (empty) {
            width = Math.max(width, font.getStringWidth(emptyLine));
        }
        int height = infoLines.size() * 10 + (upgrades.isEmpty() ? 0 : 32) + (items.isEmpty() ? 0 : 12 + ((items.size() + STACKS_ON_LINE - 1) / STACKS_ON_LINE) * 20)
                + (empty ? 10 : 0);

        ScaledResolution resolution = new ScaledResolution(mc);
        int left = Math.max(4, Math.min(tooltipX, resolution.getScaledWidth() - width - 4));
        int top = tooltipY + tooltipHeight + 8;
        if (top + height + 4 > resolution.getScaledHeight()) {
            top = Math.max(4, tooltipY - height - 12);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 400);
        GlStateManager.disableDepth();
        drawBackground(left, top, width, height);

        int y = top;
        for (String line : infoLines) {
            font.drawStringWithShadow(line, left, y, -1);
            y += 10;
        }
        if (empty) {
            font.drawStringWithShadow(emptyLine, left, y, -1);
        }
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        if (!upgrades.isEmpty()) {
            font.drawStringWithShadow(TextFormatting.YELLOW + I18n.format(keyPrefix + "upgrades"), left, y, -1);
            y += 12;
            int x = left;
            for (int i = 0; i < upgrades.size(); i++) {
                Boolean upgradeSwitch = upgradeSwitches.get(i);
                if (upgradeSwitch != null) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(GuiHelper.ICONS);
                    Gui.drawModalRectWithCustomSizedTexture(x, y + 3, upgradeSwitch ? 4 : 0, 128, 4, 10, 256, 256);
                    x += 4;
                }
                mc.getRenderItem().renderItemAndEffectIntoGUI(upgrades.get(i), x, y);
                x += STACK_WIDTH;
            }
            y += 20;
        }
        if (!items.isEmpty()) {
            font.drawStringWithShadow(TextFormatting.YELLOW + I18n.format(keyPrefix + "inventory"), left, y, -1);
            y += 12;
            for (int i = 0; i < items.size(); i++) {
                int x = left + (i % STACKS_ON_LINE) * STACK_WIDTH;
                int rowY = y + (i / STACKS_ON_LINE) * 20;
                ItemStack item = items.get(i);
                mc.getRenderItem().renderItemAndEffectIntoGUI(item, x, rowY);
                mc.getRenderItem().renderItemOverlayIntoGUI(font, item, x, rowY, item.getCount() > 1 ? abbreviate(item.getCount()) : "");
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private static void drawBackground(int left, int top, int width, int height) {
        int z = 0;
        GuiUtils.drawGradientRect(z, left - 3, top - 4, left + width + 3, top - 3, BACKGROUND, BACKGROUND);
        GuiUtils.drawGradientRect(z, left - 3, top + height + 3, left + width + 3, top + height + 4, BACKGROUND, BACKGROUND);
        GuiUtils.drawGradientRect(z, left - 3, top - 3, left + width + 3, top + height + 3, BACKGROUND, BACKGROUND);
        GuiUtils.drawGradientRect(z, left - 4, top - 3, left - 3, top + height + 3, BACKGROUND, BACKGROUND);
        GuiUtils.drawGradientRect(z, left + width + 3, top - 3, left + width + 4, top + height + 3, BACKGROUND, BACKGROUND);
        GuiUtils.drawGradientRect(z, left - 3, top - 3 + 1, left - 3 + 1, top + height + 3 - 1, BORDER_TOP, BORDER_BOTTOM);
        GuiUtils.drawGradientRect(z, left + width + 2, top - 3 + 1, left + width + 3, top + height + 3 - 1, BORDER_TOP, BORDER_BOTTOM);
        GuiUtils.drawGradientRect(z, left - 3, top - 3, left + width + 3, top - 3 + 1, BORDER_TOP, BORDER_TOP);
        GuiUtils.drawGradientRect(z, left - 3, top + height + 2, left + width + 3, top + height + 3, BORDER_BOTTOM, BORDER_BOTTOM);
    }

    /** 1234 -> 1.2k, 1234567 -> 1.2m. */
    public static String abbreviate(int count) {
        if (count < 1000) {
            return Integer.toString(count);
        }
        if (count < 1000000) {
            return trim(count / 1000.0) + "k";
        }
        return trim(count / 1000000.0) + "m";
    }

    private static String trim(double value) {
        String s = String.format("%.1f", value);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }
}
