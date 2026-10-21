/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;

import java.util.Collections;

/** The first tab beside a storage: opens its settings screen. */
public class StorageSettingsTab extends Tab {
    private final StorageContainerBase container;

    public StorageSettingsTab(Position position, StorageContainerBase container) {
        super(position, TranslationHelper.tooltip(container.getStorageWrapper().getSettingsTooltipKey()),
                onTabIconClicked -> new Button(new Position(position.getX() + 1, position.getY() + 4),
                        new ButtonDefinition(Dimension.SQUARE_16, null, null, new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(16, 96), Dimension.SQUARE_16),
                                Collections::emptyList), onTabIconClicked));
        this.container = container;
    }

    @Override
    protected void onTabIconClicked(int button) {
        container.openSettings();
    }
}
