/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import net.minecraft.item.ItemStack;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class ItemButton extends ButtonBase {
    private final Supplier<ItemStack> stack;

    public ItemButton(Position position, IntConsumer onClick, Supplier<ItemStack> stack) {
        super(position, Dimension.SQUARE_16, onClick);
        this.stack = stack;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        //noop
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        GuiHelper.renderItemInGUI(stack.get(), x, y);
    }
}
