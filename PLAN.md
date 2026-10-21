# Sophisticated Core Legacy: notes

The shared library of Sophisticated Backpacks Legacy (`../SophisticatedBackpacksLegacy`) and Sophisticated Storage Legacy (`../SophisticatedStorageLegacy`). Split out of Backpacks Legacy on 2026-10-16, after its Phase 4. Licence and sources are in the README.

## What lives here

| Package | Content |
|---|---|
| `api` | `IStorageWrapper` (one storage: inventory, upgrades, colours), `IUpgradeItem`, `UpgradeType`, the upgrade response interfaces |
| `inventory` | `StorageContents` (the single live view of one storage's contents, with a `Policy` the owning mod supplies), `InventoryHandler`, `UpgradeHandler`, `StorageFluidHandler`, `FilteredItemHandler` |
| `common.gui` | `StorageContainerBase` (abstract: subclasses say how the storage is reached, its title and how to reopen it), upgrade containers, filter slots |
| `client.gui` | `StorageScreenBase`, tabs, controls, filter controls, `CoreUpgradeTabs` (shared settings tabs) |
| `upgrades` | Pickup, filter, magnet, feeding, compacting, void, stack, crafting, smelting / auto-smelting, jukebox, tank, battery, pump, XP pump |
| `config` | One config category class per upgrade. Each storage mod keeps its own instances in its own `@Config`, so every mod has its own settings |
| `network` | Core's own channel: container data, high slot counts, tank clicks, jukebox, settings sync, JEI ghost slots |
| `settings` | Settings categories (main, no-sort, memory, item display), saved under "settings" in the contents NBT; main settings also per player |
| `util` | `SortBy`, `InventorySorter` |
| `compat.jei` | `CoreJeiPlugin`; storage mods register their container classes in `StorageContainerClasses` for recipe transfer |
| `init.ModFluids` | Liquid experience (`xpjuice`) |

Core has **no items of its own**. As in the original mods, each storage mod registers its own upgrade items built from these classes (`UpgradeItemBase` takes the registering mod's namespace), its own container types and its own tabs.

## Building and using it from the other mods

```
gradlew build copyToDependents
```

- `build/libs/SophisticatedCoreLegacy-<version>.jar` is the release jar (SRG names) for real instances.
- `copyToDependents` puts `SophisticatedCoreLegacy-<version>-dev.jar` (MCP names) into the other two workspaces' `deps/`. They depend on it with `compile name: "SophisticatedCoreLegacy-${core_version}-dev"`.
- **Not `deobfCompile`:** their `flatDir` repo matches jars by file name, so ForgeGradle's deobfuscated copy is ignored and javac gets the SRG jar ("does not override abstract method func_75145_c"). The Baubles `deobfProvided` dependency in Backpacks has the same problem, which is why it has to be copied into `eclipse/mods` by hand.
- Never rebuild Core while a dependent's runClient/runServer is running.

## Moving to Core later

Things still to bring over from Sophisticated Core 1.18 (`../SC118Reference`, pin `42394d4`) when Storage needs them: the controller / storage-link network (`controller/`). Settings, memory, no-sort, item display, sorting and JEI support moved over on 2026-10-16 (Backpacks Phase 5/6).

## Known one-time effect on existing worlds

`xpjuice` was registered by `sbackpackslegacy` before the split. A world saved before then logs once: "The fluid sbackpackslegacy:xpjuice specified as default is not present - it will be reverted to default scorelegacy:xpjuice". Tanks store the fluid by name, so no fluid is lost.

## Changes for Storage Legacy (2026-10-16, Storage Phase S3)

- **Item display, several slots:** `ItemDisplaySettingsCategory` keeps a list of shown slots with a rotation each, up to a limit (Storage barrels 4, as in the original; Backpacks 1). With a limit of 1, selecting another slot moves the display (Backpacks' old behaviour); with more, a full selection ignores new slots, like the original. An empty shown slot shows its memorised item. NBT is `slots` (int array) + `rotations`; the old single `slot`/`rotation` is migrated on load. Backpacks' 4-argument constructor still works unchanged.
- **Settings wording per mod:** `IStorageWrapper.getSettingsTitleKey / getSettingsTooltipKey / getBackToStorageTooltipKey / getStorageContextButtonKey`. The defaults are Core's backpack wording; Storage returns its own lang keys ("Settings", "Back To Storage").

- **Base stack multiplier (Storage S5):** `StorageContents.Policy.getBaseStackSizeMultiplier()` (default 1) multiplies the slot limit; stack upgrades' remove/swap checks use the inventory's real multiplier. Fixed: the limit is also recomputed when the upgrade handler is created, because the settings can build the inventory first.
- **Layout hooks (Storage S5):** `StorageContainerBase` / `SettingsContainerBase.getStorageSlotPosition(slot)` and `getStorageInventoryHeight()`; `StorageScreenBase` / `SettingsScreenBase.drawStorageSlotsBackground()`; `StorageScreenBase.shouldShowSortButtons()`; `SettingsContainerBase.isSettingsCategoryDisabled(name)`. Defaults keep the old layout (Backpacks unchanged).
- **`util/CountAbbreviator`** ported from the original Core.
- **Storage S6:** `controller` package (IControllerBoundable, IControllableStorage, ILinkable, ControllerTileEntityBase: network search, linking, routing in the original's order with a per-tick lazy index and failed-insert cache); inventory parts (`IInventoryPart`, `api/IInventoryPartUpgrade`): InventoryHandler sends a part's slots to it on the server thread, exposes `getRawStack/setRawStack/getBaseStackLimit/getBaseSlotLimit`, and sorting skips part slots; `RecipeHelper.getUncompactingResult`; filter messages carry the filter's tag key when an upgrade has two filters; `IStorageWrapper.getMatchContentsButtonKey` (Storage says "Match Storage Contents"); the storage slot writes back the stack it handed out on onSlotChanged.
- **2026-10-17 fixes:** `IStorageWrapper.getMainSettingsTabKey/getMainSettingsTabIcon` (main settings tab wording and icon per mod); `ItemDisplaySettingsCategory.getShownSlotItems/followItems`, called around `InventorySorter.sort`, so a display follows its item through a sort (a deviation); middle-click sort in `StorageScreenBase`; `RecipeHelper.addAlwaysCompactedSource/isAlwaysCompacted` (config lists from each mod, trailing `*` wildcard) used by the compacting upgrade.
- **Storage S7 (2026-10-17):** `api/UpgradeGroup`, `api/UpgradeLimits` (per-storage-type upgrade limits; IUpgradeItem's add/swap checks call it, overrides call it first), `IStorageWrapper.getStorageType`; `client/gui/ContentsTooltipPanel` (the Shift contents panel, shared by Backpacks and Storage); `client/CoreKeyBindings.SORT` (rebindable, middle mouse); `VoidUpgradeItem` takes a "void anything" supplier and `config/VoidUpgradeConfig` (standalone class: Forge @Config rejects subclassed categories); upgrade error params that are translation keys are translated on the client.
- **2026-10-18 fix (user report):** shift-clicking a stack-upgraded slot put the whole slot into one player slot when it held more than the item's max stack size (6 unstackable compasses as one stack, 40 ender pearls as one). Vanilla `mergeItemStack` caps an empty-slot placement only at the slot's limit; `StorageContainerBase` overrides it to also cap at the item's max stack size (checked: 40 pearls -> 16+16+8, 6 swords -> 6 x 1, 200 cobblestone -> 64x3+8). Backpacks' tool swapper weapon swap also took a whole multi-weapon slot; now one.
