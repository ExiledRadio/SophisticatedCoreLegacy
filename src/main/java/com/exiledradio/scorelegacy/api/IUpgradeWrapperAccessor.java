/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import java.util.List;

/** How the upgrade handler finds wrappers; Inception swaps in one that includes nested backpacks. */
public interface IUpgradeWrapperAccessor {
    <T> List<T> getWrappersThatImplement(Class<T> upgradeClass);

    <T> List<T> getWrappersThatImplementFromMainBackpack(Class<T> upgradeClass);

    void clearCache();

    default void onBeforeDeconstruct() {
        //noop
    }
}
