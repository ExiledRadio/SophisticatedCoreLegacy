/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** A button showing one of several states; clicking asks the owner to move to the next. */
public class ToggleButton<T> extends Button {
    private final Map<T, StateData> stateData;
    private final Supplier<T> getState;

    public ToggleButton(Position position, ButtonDefinition.Toggle<T> buttonDefinition, IntConsumer onClick, Supplier<T> getState) {
        super(position, buttonDefinition, onClick);
        stateData = buttonDefinition.getStateData();
        this.getState = getState;
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        StateData data = stateData.get(getState.get());
        if (data == null) {
            return;
        }
        GuiHelper.blit(x, y, data.getTexture());
        if (isMouseOver(mouseX, mouseY)) {
            GuiHelper.setTooltipToRender(data.getTooltip().get());
        }
    }

    public static class StateData {
        private final TextureBlitData texture;
        private final Supplier<List<String>> tooltip;

        public StateData(TextureBlitData texture, Supplier<List<String>> tooltip) {
            this.texture = texture;
            this.tooltip = tooltip;
        }

        public TextureBlitData getTexture() {
            return texture;
        }

        public Supplier<List<String>> getTooltip() {
            return tooltip;
        }
    }
}
