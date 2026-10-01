# Enchant Upgrades

[日本語](README.md) · [Download the latest release](https://github.com/hani2-UC/enchant-upgrades/releases/latest) · [Report an issue](https://github.com/hani2-UC/enchant-upgrades/issues)

Replace the enchanting table's random rolls with **chosen enchantments upgraded one level at a time using materials and experience levels**.

For **Minecraft Java Edition 1.20.1, 1.21.1 and 1.21.4**.

| Minecraft | Loaders | Tested loader/API versions | Java |
|---|---|---|---|
| 1.20.1 | Forge / Fabric | Forge 47.4.10 / Fabric Loader 0.16.14 + API 0.92.6 | 17 |
| 1.21.1 | Fabric / NeoForge | Fabric Loader 0.16.14 + API 0.116.17 / NeoForge 21.1.252 | 21 |
| 1.21.4 | Fabric / NeoForge | Fabric Loader 0.16.14 + API 0.119.4 / NeoForge 21.4.158 | 21 |

Fabric requires Fabric API for the matching Minecraft version. Forge and NeoForge need no additional library mods.
The mod includes Japanese and English interfaces. Other Minecraft languages use the English interface, while vanilla item and enchantment names follow the game's language.

## Installation

1. Install a loader for your Minecraft version from the table above.
2. Download one matching `enchant_upgrades-<loader>-<minecraft>-1.1.0.jar` from [Releases](https://github.com/hani2-UC/enchant-upgrades/releases/latest) and place it in your profile's `mods` folder.
3. For Fabric, also install Fabric API. Launch the profile and right-click an enchanting table.

Install the same mod version on the server and every client for multiplayer. Choose a JAR matching both the Minecraft version and loader. This mod is for Java Edition.

## How it works

Put one piece of equipment or a book in the gear slot, then put the required material in the material slot. Select an enchantment and click **Upgrade**. The material you insert automatically selects a matching enchantment; you can choose another option from the list.

The interface shows the current and next enchantment levels, material quantity, experience level cost, and required bookshelf power. Hover over the material icon to see its name, or over an unavailable upgrade to see why it is blocked. Use the arrows or scroll over the list to change pages. Shift-click transfers gear and materials.

- Supports all 37 non-curse vanilla enchantments on 1.20.1 and all 40 on 1.21.x, including the three mace enchantments.
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

Rare effects have higher initial costs. For example, Mending requires two echo shards, 30 experience levels, and 15 bookshelf power. Density starts at two iron ingots / five levels, Breach at two breeze rods / eight levels / three shelves, and Wind Burst at four breeze rods / 20 levels / 12 shelves.

Bookshelves use the normal placement rules and need clear space between them and the table. Forge and NeoForge count modded bookshelf power; Fabric counts providers in the `minecraft:enchantment_power_provider` tag. The limit is 15.

![Japanese interface after upgrading a sword to Sharpness I](docs/enchant-upgrades-ja.png)

## Customize with data packs

Override JSON files using the appropriate path and data pack format:

| Minecraft | Path | Data pack format |
|---|---|---:|
| 1.20.1 | `data/enchant_upgrades/recipes/sharpness.json` | 15 |
| 1.21.1 | `data/enchant_upgrades/enchantment_upgrades/sharpness.json` | 48 |
| 1.21.4 | `data/enchant_upgrades/enchantment_upgrades/sharpness.json` | 61 |

On 1.21.x, upgrades use a dedicated data directory, and `type` is optional. The Sweeping Edge enchantment ID is `minecraft:sweeping_edge` on 1.21.x and `minecraft:sweeping` on 1.20.1.

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

Use either `{ "item": "minecraft:blaze_rod" }` or `{ "tag": "namespace:tag" }` for the material. Version 1.20.1 also accepts vanilla Ingredient arrays. Costs must fit in one stack and stay below 64 materials and 32767 experience levels.

To support another mod's enchantment, add a JSON file with its registered enchantment ID. Modded enchantments are not added automatically. Apply changes with `/reload` or by reopening the world. On 1.21.x, the server sends the settings when the menu opens; menus opened before a reload close and must be reopened.

## Development and validation

On Windows, `./Build-Mod.ps1` builds all six variants into `dist/`. Set `JAVA17_HOME` and `JAVA21_HOME`, or use the detected local JDKs in this workspace. Filter with `-Loader Fabric -Minecraft 1.21.4`; add `-Test` to run GameTests.

Projects: root for Forge, `fabric/` for Fabric 1.20.1, `fabric-modern/` for Fabric 1.21.x, and `neoforge/` for NeoForge. Modern builds take `-PmcVersion=1.21.1` or `-PmcVersion=1.21.4`. Use the wrapper inside `fabric-modern/` for that project.

`runGameTestServer` (Forge/NeoForge) or `runGametest` (Fabric) checks resource consumption, conflicts and limits, live bookshelf checks, enchanted books, invalid requests and reach, item transfers and returns, and network serialization. Modern versions also test mace enchantments and rejection of stale settings.

`runSmokeClient` creates a disposable development world, opens the table, upgrades a sword through the actual client/server connection, saves a screenshot, and exits. Test classes and the empty test structure are excluded from the release JAR.

Version 1.1.0 builds all six variants and passes eight GameTests per 1.20.1 variant and ten per 1.21.x variant. Japanese client smoke tests confirmed menu opening and Sharpness I upgrades through the real connection for all added builds. The Forge client test was performed on 1.0.0; its eight GameTests were repeated after the shared-code refactor for 1.1.0. Compatibility with other mods has not been tested.

Original mod code is licensed under MIT; see `LICENSE.md`. Forge MDK notices are preserved in `LICENSE.txt`.
