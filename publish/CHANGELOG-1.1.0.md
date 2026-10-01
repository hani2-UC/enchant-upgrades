# Enchant Upgrades 1.1.0

Minecraft 1.20.1: Forge and Fabric. Minecraft 1.21.1 and 1.21.4: Fabric and NeoForge.

- Add five new loader/version builds while keeping Forge 1.20.1 support.
- Add Density, Breach, and Wind Burst upgrades on Minecraft 1.21.x, covering all 40 non-curse vanilla enchantments.
- Keep material costs, experience level costs, bookshelf checks, book upgrades, and Japanese/English interfaces across all builds.
- Synchronize the server's upgrade settings when opening the menu on 1.21.x; reject stale menus after a data reload.
- Use `data/<namespace>/enchantment_upgrades/*.json` for 1.21.x data-pack customization. Minecraft 1.20.1 keeps `data/<namespace>/recipes/*.json`.

Choose the JAR matching both your Minecraft version and loader. Fabric builds also require Fabric API. Install the mod on the server and all clients.

Validation: all six builds succeeded; eight GameTests passed per 1.20.1 build and ten per 1.21.x build. Japanese client tests confirmed real table opening and upgrades for all added builds. Forge's client test was performed on 1.0.0 and its GameTests were repeated for 1.1.0. Other mod combinations remain untested.
