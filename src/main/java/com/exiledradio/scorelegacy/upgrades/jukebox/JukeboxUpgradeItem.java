/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.jukebox;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** Plays a music disc from the backpack, following whoever carries it. */
public class JukeboxUpgradeItem extends UpgradeItemBase<JukeboxUpgradeWrapper> {
    public static final UpgradeType<JukeboxUpgradeWrapper> TYPE = new UpgradeType<>(JukeboxUpgradeWrapper::new);

    public JukeboxUpgradeItem(String name, CreativeTabs tab) {
        super(name, tab);
    }

    @Override
    public UpgradeType<JukeboxUpgradeWrapper> getType() {
        return TYPE;
    }
}
