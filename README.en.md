# Enchant Upgrades

[日本語](README.md) · [Download the latest release](https://github.com/hani2-UC/enchant-upgrades/releases/latest) · [Report an issue](https://github.com/hani2-UC/enchant-upgrades/issues)

Replace the enchanting table's random rolls with **chosen enchantments upgraded one level at a time using materials and experience levels**.

For **Minecraft Java Edition 1.20.1, Forge 47.4.10 or newer within the 47.x series, and Java 17**. No additional library mods are required.
The mod includes Japanese and English interfaces. Other Minecraft languages use the English interface, while vanilla item and enchantment names follow the game's language.

## Installation

1. Install [Forge for Minecraft 1.20.1](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html).
2. Download `enchant_upgrades-1.0.0.jar` from [Releases](https://github.com/hani2-UC/enchant-upgrades/releases/latest) and place it in your Minecraft profile's `mods` folder.
3. Launch the Forge profile and right-click an enchanting table.

Install the same mod version on the server and every client for multiplayer. This release is for Forge, and is not compatible with Fabric, NeoForge, or Bedrock Edition.

## How it works

Put one piece of equipment or a book in the gear slot, then put the required material in the material slot. Select an enchantment and click **Upgrade**. The material you insert automatically selects a matching enchantment; you can choose another option from the list.

The interface shows the current and next enchantment levels, material quantity, experience level cost, and required bookshelf power. Hover over the material icon to see its name, or over an unavailable upgrade to see why it is blocked. Use the arrows or scroll over the list to change pages. Shift-click transfers gear and materials.

- Supports all 37 non-curse vanilla enchantments, including Mending, Soul Speed, and Swift Sneak.
- Enforces vanilla maximum levels and enchantment incompatibilities.
- Supports regular and enchanted books as well as already enchanted equipment.
- Preserves item names, damage, and existing enchantments.
- Returns remaining materials and equipment when the menu closes. Overflow items are dropped.
- Validates every upgrade on the server. Failed upgrades consume nothing.
- Creative players do not consume materials or XP, but still need the required bookshelves and valid enchantment combinations.
- Villager trades, loot, anvils, and grindstones retain their normal behavior.

## Example: Sharpness

Costs are paid for each step. XP means **experience levels**, not experience points.

| Upgrade | Blaze rods | Experience levels | Bookshelf power |
|---|---:|---:|---:|
| None → I | 1 | 3 | 0 |
| I → II | 2 | 6 | 3 |
| II → III | 3 | 12 | 6 |
| III → IV | 4 | 21 | 9 |
| IV → V | 5 | 33 | 12 |

Rare effects have higher initial costs. For example, Mending requires two echo shards, 30 experience levels, and 15 bookshelf power. Bookshelves use the normal enchanting table placement rules and need clear space between them and the table. Modded bookshelf power is also counted, up to 15.

![Japanese interface after upgrading a sword to Sharpness I](docs/enchant-upgrades-ja.png)

## Customize with data packs

Override recipe files such as `data/enchant_upgrades/recipes/sharpness.json`. Use data pack format 15.

```json
{
  "type": "enchant_upgrades:upgrade",
  "enchantment": "minecraft:sharpness",
  "material": { "item": "minecraft:blaze_rod" },
  "base_material": 1,
  "material_step": 1,
  "base_levels": 3,
  "level_step": 3,
  "base_shelves": 0,
  "shelf_step": 3
}
```

For target level L:

- Materials: `base_material + material_step * (L - 1)`
- Experience levels: `base_levels + level_step * L * (L - 1) / 2`
- Bookshelf power: `min(15, base_shelves + shelf_step * (L - 1))`

The material field accepts the vanilla Ingredient format, including item tags. The material cost must fit in a single stack; for items with smaller stack limits, keep all upgrade costs within that limit. Costs above 64 materials or 32767 experience levels are rejected when recipes load.

To support another mod's enchantment, add a recipe with its registered enchantment ID. Modded enchantments are not added automatically. Reload recipes with `/reload` or by reopening the world; the server synchronizes them to clients.

## Development and validation

With JDK 17, run `gradlew.bat build` on Windows or `sh gradlew build` on other systems. The distributable JAR is in `build/libs/`. On the original workspace, `Build-Mod.ps1` can also use its local JDK and copy the result to `dist/`.

`runGameTestServer` runs eight Minecraft GameTests covering resource consumption, conflicts and limits, live bookshelf checks, enchanted books, invalid requests and reach, item transfers and returns, and recipe network serialization.

`runSmokeClient` creates a disposable development world, opens the table, upgrades a sword through the actual client/server connection, saves a screenshot, and exits. Test classes and the empty test structure are excluded from the release JAR.

Version 1.0.0 passed all eight GameTests and a Japanese client smoke test on Forge 47.4.10 / Java 17. Compatibility with other mods has not been tested.

Original mod code is licensed under MIT; see `LICENSE.md`. Forge MDK notices are preserved in `LICENSE.txt`.
