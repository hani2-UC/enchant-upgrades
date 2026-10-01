# 1.1.0 validation — 2026-10-01

| Minecraft | Loader | GameTests | Japanese client upgrade |
|---|---|---:|---|
| 1.20.1 | Forge 47.4.10 | 8/8 | Confirmed on 1.0.0; GameTests repeated after the 1.1.0 refactor |
| 1.20.1 | Fabric Loader 0.16.14 / API 0.92.6 | 8/8 | Confirmed |
| 1.21.1 | Fabric Loader 0.16.14 / API 0.116.17 | 10/10 | Confirmed |
| 1.21.1 | NeoForge 21.1.252 | 10/10 | Confirmed |
| 1.21.4 | Fabric Loader 0.16.14 / API 0.119.4 | 10/10 | Confirmed |
| 1.21.4 | NeoForge 21.4.158 | 10/10 | Confirmed |

GameTests run inside Minecraft dedicated test servers. They cover costs and preservation, failed-upgrade atomicity, conflicts and maximum levels, live bookshelf changes, stored enchantments on books, invalid indices/players/reach, transfers and returns, and network serialization. Modern tests also cover all 40 recipes, mace enchantments, and stale settings.

Client smoke tests create a new survival world, invoke the loader's enchanting-table interaction, insert a diamond sword and 32 blaze rods, send the normal client menu button packet, wait for synchronized Sharpness I, save a screenshot, and exit. Screenshots show 31 remaining rods and 47 experience levels after the first upgrade.

Validation used Java 17 for 1.20.1 and Java 21 for 1.21.x on Windows. Compatibility with other mods has not been tested. GitHub Actions runs builds and GameTests for the same six variants; client smoke tests run locally.
