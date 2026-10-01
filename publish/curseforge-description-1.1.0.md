# Enchant Upgrades

Choose your enchantments and build them up one level at a time. Enchant Upgrades replaces the enchanting table's random rolls with upgrades paid for using materials and experience levels.

## Requirements

| Minecraft | Loaders | Java |
|---|---|---|
| 1.20.1 | Forge 47.4.10+ / Fabric | 17 |
| 1.21.1 | Fabric / NeoForge 21.1.252+ | 21 |
| 1.21.4 | Fabric / NeoForge 21.4.158+ | 21 |

Choose the file for both your Minecraft version and loader. Fabric requires Fabric API for the same Minecraft version. Install on the server and every client for multiplayer. Forge and NeoForge require no additional library mods.

## Using the table

Right-click an enchanting table. Put equipment or a book in the gear slot and the required material in the material slot. Choose an enchantment from the list and click **Upgrade**.

The screen shows the current and next levels, material quantity, XP level cost, and required bookshelf power. Inserting a material selects a matching enchantment automatically. Use the page arrows or scroll over the list to see more options, and Shift-click to transfer items.

If an upgrade is blocked, hover over its row or the Upgrade button to see the reason. Hover over the material icon to see the required material's name.

## Features

- All **37 non-curse vanilla enchantments** on 1.20.1 and **40 on 1.21.x**, including Density, Breach, and Wind Burst.
- Equipment, regular books, and enchanted books can be upgraded.
- Upgrades preserve damage, names, and other enchantments.
- Vanilla enchantment incompatibilities and maximum levels are enforced.
- Each higher level costs more materials and experience. Higher tiers also need bookshelves.
- Every upgrade is checked on the server. Failed upgrades consume nothing.
- Closing the screen returns equipment and unused materials; items that do not fit are dropped.
- Japanese and English interfaces. Vanilla item and enchantment names follow your game's language.
- Creative mode waives material and XP costs; bookshelf, compatibility, and maximum-level requirements remain.

Villager trades, loot, anvils, and grindstones continue to work normally. The replacement applies to the enchanting table.

## Example: Sharpness

XP costs are **experience levels**, not individual experience points. Each row is the cost paid for that step.

| Upgrade | Blaze rods | XP levels | Bookshelf power |
|---|---:|---:|---:|
| None to I | 1 | 3 | 0 |
| I to II | 2 | 6 | 3 |
| II to III | 3 | 12 | 6 |
| III to IV | 4 | 21 | 9 |
| IV to V | 5 | 33 | 12 |

Mending requires two echo shards, 30 experience levels, and 15 bookshelf power. Bookshelves need the normal enchanting-table arrangement and a clear gap to the table. Forge and NeoForge count modded bookshelf power; Fabric counts providers tagged minecraft:enchantment_power_provider. The limit is 15.

## Data-pack customization

Override data/enchant_upgrades/recipes/sharpness.json on 1.20.1 (data pack format 15), or data/enchant_upgrades/enchantment_upgrades/sharpness.json on 1.21.1 (format 48) and 1.21.4 (format 61).

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

For target level L, the material cost is `base_material + material_step * (L - 1)`, the XP level cost is `base_levels + level_step * L * (L - 1) / 2`, and the bookshelf requirement is `min(15, base_shelves + shelf_step * (L - 1))`.

Material costs must fit in one stack. Recipes may use item tags. To support another mod's enchantment, add a recipe referencing its registered enchantment ID. Extra enchantments are not added automatically. Use `/reload` or reopen the world to apply changes.

## Installation and support

Place the matching enchant_upgrades-LOADER-MINECRAFT-1.1.0.jar in your profile's mods folder. For Fabric, install Fabric API as well. Install the same version on the server and clients.

- [English documentation](https://github.com/hani2-UC/enchant-upgrades/blob/codex/publish-enchant-upgrades/README.en.md)
- [Japanese documentation and full material list](https://github.com/hani2-UC/enchant-upgrades/blob/codex/publish-enchant-upgrades/README.md)
- [Source code](https://github.com/hani2-UC/enchant-upgrades)
- [Issue tracker](https://github.com/hani2-UC/enchant-upgrades/issues)
- [Actual in-game screenshot, Japanese interface](https://github.com/hani2-UC/enchant-upgrades/blob/codex/publish-enchant-upgrades/docs/enchant-upgrades-ja.png)

Original mod code is licensed under MIT. Version 1.1.0 passes eight Minecraft GameTests per 1.20.1 build and ten per 1.21.x build. Japanese client tests confirmed actual upgrades on each added loader/version. Forge's client test was performed on 1.0.0, with its GameTests repeated for 1.1.0. Other mod combinations have not been tested.

Development disclosure: the implementation, translations, and listing copy were created with generative AI from the author's requested design. The preview is an actual Minecraft screenshot, not a generated depiction. The project icon uses Minecraft's vanilla enchanted-book texture with the mod name; the game texture belongs to Mojang/Microsoft. The mod does not call an AI service during gameplay.

