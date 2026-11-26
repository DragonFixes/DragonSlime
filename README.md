# DragonSlime

[![GitHub Stars](https://img.shields.io/github/stars/DragonFixes/DragonSlime?style=social)](https://github.com/DragonFixes/DragonSlime)
[![GNU General Public License v3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![bStats](https://bStats.org/signatures/bukkit/Slimefun%205.svg)](https://bStats.org/plugin/bukkit/Slimefun%205/31272)

**DragonSlime** is a fork of [Slimefun 5](https://github.com/Slimefun5/Slimefun5) (itself a fork of [Slimefun 4](https://github.com/Slimefun/Slimefun4)): a Spigot/Paper plugin that gives your server a modpack-like feel with over 500 new items and recipes. One universal jar runs on every server from **Minecraft 1.8.8 to 26.x**.<br><br>Backpacks, jetpacks, magical wands, nuclear reactors: every player decides how deep to dive into **magic** or **tech**, with no client mods needed.

## Requirements
- Java 8 or newer
- Spigot or Paper 1.8.8 – 26.x

## Features

| | |
|---|---|
| 🧪 **500+ items and recipes** | Tools, armor, food, machines, generators, and magical gear. |
| ⚙️ **Multiblock machines** | Build structures in the world and use them as crafting stations. |
| ⚡ **Energy networks** | Generators, capacitors, and machines connected into one power grid. |
| 📦 **Cargo transport** | Move and sort items between inventories automatically. |
| 📖 **In-game guide** | Browse every item, recipe, and research without leaving the game. |
| 🧩 **Addon support** | Dozens of community addons add machines, crops, dimensions, and more. |
| 🌍 **One jar, every version** | A single build loads on servers from 1.8.8 up to 26.x. |
| 🗄️ **Flexible storage** | Block and player data in files, H2, or MySQL. |

## Bundled addons

DragonSlime ships with these addons built from this repository, ready to drop into your `plugins` folder:

| Addon | What it adds |
|---|---|
| **InfinityExpansion** | Infinity-tier items, machines, and resource generators. |
| **LiteXpansion** | IndustrialCraft-inspired electric machines and tools. |
| **Networks** | Digital storage networks, crafting grids, and item transport. |
| **SlimefunAdvancements** | An advancement and achievement system. |
| **SMG** (SimpleMaterialGenerators) | Simple generators for cobblestone, sand, glass, and more. |
| **Supreme** | High-tier machines, magic, and generic-machine tooling. |

Many more community addons work too. The dev-server scripts can clone and build them for you (see [Building](#building)).

## Building

Clone the repository and run the Gradle wrapper. The toolchain resolver downloads the JDKs it needs.

```bash
./gradlew build -x test     # plugin jar + bundled addons, copied to deploy/
./gradlew :core:shadowJar   # just the plugin jar: core/build/libs/
./gradlew :core:test        # run the test suite
```

Spin up a local test server on any supported version:

```bash
./gradlew :core:runServer -PmcVersion=1.20.6 -PbundledAddons
```

## Developer API

Addons build against the Slimefun 5 API, which DragonSlime keeps compatible. You can depend on it with [github-gradle](https://github.com/intisy/github-gradle) in your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.intisy.github-gradle") version "1.8.2.1"
}

dependencies {
    "githubCompileOnly"("Slimefun5:Slimefun5:v5.3.3")
}
```

## Wiki

[Read more on the Slimefun Wiki...](https://github.com/Slimefun5/Wiki/wiki/Slimefun5)

## Credits

DragonSlime stands on the work of many people. Thank you to everyone below.

### Slimefun

| Project | Authors | License |
|---|---|---|
| [Slimefun 4](https://github.com/Slimefun/Slimefun4) | TheBusyBiscuit and the Slimefun community | [GPL-3.0](https://github.com/Slimefun/Slimefun4/blob/HEAD/LICENSE) |
| [Slimefun 5](https://github.com/Slimefun5/Slimefun5) | The Slimefun 5 contributors | [GPL-3.0](https://github.com/Slimefun5/Slimefun5/blob/HEAD/LICENSE) |

### Bundled addons

Each addon lives under [`addons/`](addons) with its full history and its own `LICENSE` file.

| Addon | Original author(s) | Upstream | License |
|---|---|---|---|
| InfinityExpansion | Mooy1 | [Riley31415/InfinityExpansion](https://github.com/Riley31415/InfinityExpansion) | [GPL-3.0](addons/InfinityExpansion/LICENSE) |
| LiteXpansion | J3fftw | [Slimefun-Addon-Community/LiteXpansion](https://github.com/Slimefun-Addon-Community/LiteXpansion) | [GPL-3.0](addons/LiteXpansion/LICENSE) |
| Networks | Sefiraat | [Sefiraat/Networks](https://github.com/Sefiraat/Networks) | [GPL-3.0](addons/Networks/LICENSE) |
| SlimefunAdvancements | char321 | [char3210/SlimefunAdvancements](https://github.com/char3210/SlimefunAdvancements) | [GPL-3.0](addons/SlimefunAdvancements/LICENSE) |
| SMG | waleks | [waleks647/SMG](https://github.com/waleks647/SMG) | [GPL-3.0](addons/SMG/LICENSE) |
| Supreme | RelativoBR, Especttra, WilianSantosBR, Mynothauro | [Slimefun-Addon-Community/Supreme](https://github.com/Slimefun-Addon-Community/Supreme) | [GPL-3.0](addons/Supreme/LICENSE) |

The in-repo copies are based on the [Slimefun5](https://github.com/Slimefun5) forks of these addons.

### Libraries

Shaded into the plugin jar (relocated under `io.github.thebusybiscuit.slimefun5.libraries`):

| Library | License |
|---|---|
| [dough](https://github.com/Slimefun/dough) | MIT |
| [PaperLib](https://github.com/PaperMC/PaperLib) | MIT |
| [XSeries](https://github.com/CryptoMorin/XSeries) | MIT |
| [Apache Commons Lang 2](https://commons.apache.org/proper/commons-lang/) | Apache-2.0 |

The [H2](https://h2database.com) and [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) database drivers are not bundled. They are downloaded at runtime only when a database backend is enabled, under their own licenses.

## Discord

You can find Slimefun's community on Discord! Click the badge below to join the server for suggestions/questions or other discussions about this plugin.

<p align="center">
  <a href="https://discord.gg/CbBYZBEWdR">
    <img src="https://discordapp.com/api/guilds/1520841893045338302/widget.png?style=banner2" alt="Discord"/>
  </a>
</p>

## License

This project is open-source and licensed under the GNU General Public License v3.0.
