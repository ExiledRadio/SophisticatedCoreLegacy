/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.controls.Widget;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.upgrades.pump.FluidFilterContainer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;

/** A row of fluid filter slots; click one holding a filled container to set it, empty-handed to clear it. */
public class FluidFilterControl extends Widget {
    private final FluidFilterContainer container;

    public FluidFilterControl(Position position, FluidFilterContainer container) {
        super(position);
        this.container = container;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        GuiHelper.renderSlotsBackground(x, y, container.getNumberOfFluidFilters(), 1, 0);
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < container.getNumberOfFluidFilters(); i++) {
            Fluid fluid = container.getFluid(i);
            if (fluid != null) {
                FluidStack stack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
                TextureAtlasSprite still = minecraft.getTextureMapBlocks().getAtlasSprite(fluid.getStill(stack).toString());
                GuiHelper.renderTiledFluid(still, fluid.getColor(stack), x + i * 18 + 1, y + 1, 16);
            }
        }
        int hovered = getSlotAt(mouseX, mouseY);
        if (hovered >= 0 && container.getFluid(hovered) != null) {
            GuiHelper.setTooltipToRender(Collections.singletonList(new FluidStack(container.getFluid(hovered), 1).getLocalizedName()));
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int index = getSlotAt(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        container.slotClick(index);
        return true;
    }

    private int getSlotAt(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY) || mouseY < y + 1 || mouseY >= y + 17) {
            return -1;
        }
        int index = (mouseX - x) / 18;
        return index < container.getNumberOfFluidFilters() ? index : -1;
    }

    @Override
    public int getWidth() {
        return container.getNumberOfFluidFilters() * 18;
    }

    @Override
    public int getHeight() {
        return 18;
    }
}
