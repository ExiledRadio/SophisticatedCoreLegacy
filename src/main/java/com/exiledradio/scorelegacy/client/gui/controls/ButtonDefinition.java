/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ButtonDefinition {
    private final Dimension dimension;
    @Nullable
    private final TextureBlitData backgroundTexture;
    @Nullable
    private final TextureBlitData hoveredBackgroundTexture;
    @Nullable
    private final TextureBlitData foregroundTexture;
    private final Supplier<List<String>> tooltip;

    public ButtonDefinition(Dimension dimension, @Nullable TextureBlitData backgroundTexture, @Nullable TextureBlitData hoveredBackgroundTexture) {
        this(dimension, backgroundTexture, hoveredBackgroundTexture, null, Collections::emptyList);
    }

    public ButtonDefinition(Dimension dimension, @Nullable TextureBlitData backgroundTexture, @Nullable TextureBlitData hoveredBackgroundTexture,
            @Nullable TextureBlitData foregroundTexture, Supplier<List<String>> tooltip) {
        this.dimension = dimension;
        this.backgroundTexture = backgroundTexture;
        this.hoveredBackgroundTexture = hoveredBackgroundTexture;
        this.foregroundTexture = foregroundTexture;
        this.tooltip = tooltip;
    }

    public Dimension getDimension() {
        return dimension;
    }

    @Nullable
    public TextureBlitData getBackgroundTexture() {
        return backgroundTexture;
    }

    @Nullable
    public TextureBlitData getHoveredBackgroundTexture() {
        return hoveredBackgroundTexture;
    }

    @Nullable
    public TextureBlitData getForegroundTexture() {
        return foregroundTexture;
    }

    public Supplier<List<String>> getTooltip() {
        return tooltip;
    }

    public static class Toggle<T> extends ButtonDefinition {
        private final Map<T, ToggleButton.StateData> stateData;

        public Toggle(Dimension dimension, TextureBlitData backgroundTexture, Map<T, ToggleButton.StateData> stateData, @Nullable TextureBlitData hoveredBackgroundTexture) {
            super(dimension, backgroundTexture, hoveredBackgroundTexture);
            this.stateData = stateData;
        }

        public Map<T, ToggleButton.StateData> getStateData() {
            return stateData;
        }
    }
}
