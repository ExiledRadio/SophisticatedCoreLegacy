/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import com.exiledradio.scorelegacy.client.gui.controls.ItemButton;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;

/** The settings tab of one upgrade. Its container's slots live off screen until it opens. */
public abstract class UpgradeSettingsTab<C extends UpgradeContainerBase<?, ?>> extends SettingsTabBase {
    private final C upgradeContainer;

    protected UpgradeSettingsTab(C upgradeContainer, Position position, StorageScreenBase screen, String upgradeName) {
        super(position, screen, TranslationHelper.translUpgradeKey(upgradeName), TranslationHelper.tooltip(TranslationHelper.translUpgradeKey(upgradeName) + ".tooltip"),
                onTabIconClicked -> new ItemButton(new Position(position.getX() + 1, position.getY() + 4), onTabIconClicked, upgradeContainer::getUpgradeStack));
        this.upgradeContainer = upgradeContainer;
        moveSlotsOutOfView();
    }

    protected C getContainer() {
        return upgradeContainer;
    }

    protected abstract void moveSlotsToTab();

    protected void moveSlotsOutOfView() {
        getContainer().getSlots().forEach(slot -> slot.xPos = StorageContainerBase.DISABLED_SLOT_X_POS);
    }

    @Override
    protected void onTabOpen() {
        super.onTabOpen();
        moveSlotsToTab();
    }

    @Override
    protected void onTabClose() {
        super.onTabClose();
        moveSlotsOutOfView();
    }

    @Override
    protected void setOpen(boolean isOpen) {
        upgradeContainer.setIsOpen(isOpen);
        super.setOpen(isOpen);
    }

    public void onAfterInit() {
        if (upgradeContainer.isOpen()) {
            setOpen(true);
        }
    }
}
