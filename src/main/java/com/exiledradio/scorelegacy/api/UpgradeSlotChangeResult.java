/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import java.util.Collections;
import java.util.Set;

/** Whether an upgrade may be added, removed or swapped, and if not, why and which slots block it. */
public final class UpgradeSlotChangeResult {
    public static final UpgradeSlotChangeResult SUCCESS = new UpgradeSlotChangeResult(true, "", new Object[0], Collections.emptySet(), Collections.emptySet());

    private final boolean successful;
    private final String errorKey;
    private final Object[] errorParams;
    private final Set<Integer> errorUpgradeSlots;
    private final Set<Integer> errorInventorySlots;

    private UpgradeSlotChangeResult(boolean successful, String errorKey, Object[] errorParams, Set<Integer> errorUpgradeSlots, Set<Integer> errorInventorySlots) {
        this.successful = successful;
        this.errorKey = errorKey;
        this.errorParams = errorParams;
        this.errorUpgradeSlots = errorUpgradeSlots;
        this.errorInventorySlots = errorInventorySlots;
    }

    /** @param errorKey the part after "gui.scorelegacy.error." */
    public static UpgradeSlotChangeResult fail(String errorKey, Set<Integer> errorUpgradeSlots, Set<Integer> errorInventorySlots, Object... errorParams) {
        return new UpgradeSlotChangeResult(false, errorKey, errorParams, errorUpgradeSlots, errorInventorySlots);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public Object[] getErrorParams() {
        return errorParams;
    }

    public Set<Integer> getErrorUpgradeSlots() {
        return errorUpgradeSlots;
    }

    public Set<Integer> getErrorInventorySlots() {
        return errorInventorySlots;
    }
}
