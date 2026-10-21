/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

/** Upgrades that count together towards a per-storage limit (stack upgrades, furnace upgrades). */
public final class UpgradeGroup {
    public static final UpgradeGroup NONE = new UpgradeGroup("none", "none", true);
    public static final UpgradeGroup STACK_UPGRADES = new UpgradeGroup("stack_upgrades", "upgrade_group.scorelegacy.stack_upgrades");
    public static final UpgradeGroup COOKING_UPGRADES = new UpgradeGroup("cooking_upgrades", "upgrade_group.scorelegacy.cooking_upgrades");

    private final String name;
    private final String translationKey;
    private final boolean solo;

    public UpgradeGroup(String name, String translationKey) {
        this(name, translationKey, false);
    }

    private UpgradeGroup(String name, String translationKey, boolean solo) {
        this.name = name;
        this.translationKey = translationKey;
        this.solo = solo;
    }

    public String getName() {
        return name;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    /** An upgrade in no group: only its own limit applies. */
    public boolean isSolo() {
        return solo;
    }
}
