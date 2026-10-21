# Changelog

## 1.1.0 (2026-10-20)

Adds what the new Storage and Backpacks features need; both now require 1.1.0.

- **Compat materials:** detects copper, netherite and the crimson and warped woods that other mods add (by ore dictionary, plus the netherite and Nether wood mods that register none), and loads each mod's recipes for them only when they exist.
- **Stack upgrades** can divide as well as multiply (stack downgrades: a share of every item's normal stack, at least one) and go up to the omega tier.
- **Infinity upgrades** (from the original 1.20): every item in the storage becomes endless; it must be the storage's only upgrade, and the admin one can only be taken out by operators. Endless slots show "∞".
- **Display side** (from the original 1.20): storages that allow it (double chests) can show their display items on the front or on either end; a button in the item display settings picks the face.
- Per-slot settings (memory, no-sort, item display) can be copied from one storage to another with the slots moved, and removed from a slot onwards (for double chests).
- A storage can say it keeps no contents of its own (one half of a double chest, a Storage I/O block): a controller keeps it in the network but doesn't count or fill it.

## 1.0.0 (2026-10-17)

First release. Sophisticated Core Legacy is the shared library behind **Sophisticated Backpacks Legacy** and **Sophisticated Storage Legacy**, an unofficial 1.12.2 port of P3pp3rF1y's Sophisticated Core (GPLv3, `1.18.x` branch at `42394d4`). It adds no items of its own; install it with either or both mods.

Requires Minecraft 1.12.2 and Forge 14.23.5.2847 or newer (Cleanroom works too). Optional: JEI or HEI.

What it provides:
- The storage GUI and container: inventory and upgrade slots, upgrade tabs, sort buttons, the settings screen (main, no-sort, memory and item display categories), and slots that hold more than 64 when a storage has stack upgrades.
- Every shared upgrade: pickup, filter, magnet, feeding, compacting, void, smelting and auto-smelting, crafting, jukebox, tank, battery, pump and XP pump, and stack upgrades, with basic and advanced versions where the original has them.
- JEI support: exclusion areas, drag from JEI into filter and memory slots, recipe transfer into the crafting upgrade.

Changes from the original:
- **Sort key:** "Sort Storage" (middle mouse by default, rebindable) sorts the storage when used over its slots.
- **Item display:** the items a storage shows follow them when it is sorted (the original keeps the slot, so sorting changed what was shown).
- **Compacting:** each mod's config can list items that compact even though no recipe crafts them back (NoTreePunching's rocks by default).
- **Per-storage upgrade limits** (used by Storage's config).
- **Contents tooltip:** a shared Shift tooltip showing a backpack's, shulker box's or packed storage's contents.
- **Inventory Tweaks** is turned off for these screens, because its sorting can't handle stacks over 64.
