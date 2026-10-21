/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeInventoryPartBase;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.TankClickMessage;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.ArrayList;
import java.util.List;

/** The tank drawn over two inventory columns; click it with a bucket to fill or empty it. */
public class TankInventoryPart extends UpgradeInventoryPartBase<TankUpgradeContainer> {
    private static final TextureBlitData OVERLAY = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(47, 30), new Dimension(16, 18));
    private final Position pos;
    private final int height;
    private final StorageScreenBase screen;

    public TankInventoryPart(int upgradeSlot, TankUpgradeContainer container, Position pos, int height, StorageScreenBase screen) {
        super(upgradeSlot, container);
        this.pos = pos;
        this.height = height;
        this.screen = screen;
    }

    private int getTankLeft() {
        return screen.getGuiLeft() + pos.getX() + 9;
    }

    private int getTop() {
        return screen.getGuiTop() + pos.getY();
    }

    @Override
    public void render(int mouseX, int mouseY) {
        GuiHelper.renderBarBackground(getTankLeft(), getTop(), height);
        renderFluid();
        for (int yOffset = 0; yOffset + 18 <= height; yOffset += 18) {
            GuiHelper.blit(getTankLeft() + 1, getTop() + yOffset, OVERLAY);
        }
        renderTooltip(mouseX, mouseY);
    }

    private void renderFluid() {
        FluidStack contents = container.getContents();
        int capacity = container.getTankCapacity();
        if (contents == null || contents.amount <= 0 || capacity <= 0) {
            return;
        }
        Fluid fluid = contents.getFluid();
        int displayLevel = (int) ((height - 2) * Math.min(1f, (float) contents.amount / capacity));
        if (displayLevel <= 0) {
            return;
        }
        TextureAtlasSprite still = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill(contents).toString());
        GuiHelper.renderTiledFluid(still, fluid.getColor(contents), getTankLeft() + 1, getTop() + 1 + height - 2 - displayLevel, displayLevel);
    }

    private boolean isMouseOverTank(int mouseX, int mouseY) {
        return mouseX >= getTankLeft() + 1 && mouseX < getTankLeft() + 17 && mouseY >= getTop() + 1 && mouseY < getTop() + height - 1;
    }

    private void renderTooltip(int mouseX, int mouseY) {
        if (!isMouseOverTank(mouseX, mouseY)) {
            return;
        }
        FluidStack contents = container.getContents();
        List<String> tooltip = new ArrayList<>();
        if (contents != null) {
            tooltip.add(contents.getLocalizedName());
        }
        tooltip.add(I18n.format(TranslationHelper.translUpgradeKey("tank.contents_tooltip"),
                String.format("%,d", contents == null ? 0 : contents.amount), String.format("%,d", container.getTankCapacity())));
        GuiHelper.setTooltipToRender(tooltip);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!isMouseOverTank(mouseX, mouseY)) {
            return false;
        }
        ItemStack cursorStack = Minecraft.getMinecraft().player.inventory.getItemStack();
        if (cursorStack.getCount() != 1 || !cursorStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return false;
        }
        CorePacketHandler.CHANNEL.sendToServer(new TankClickMessage(upgradeSlot));
        return true;
    }
}
