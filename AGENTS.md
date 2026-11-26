# AGENTS.md

> **Note:** This file was written specifically for Claude (Claude Code), so
> some wording may not fit other agents exactly — e.g. Claude-specific tool
> names, skills, or features. Interpret those in terms of your own equivalents.
>
> The master copy of the global rules these instructions build on lives at
> `{USER HOME}/.claude/CLAUDE.md` (e.g. `C:\Users\<user>\.claude\CLAUDE.md`).
> Read it as well; rules there apply unless this file overrides them.

DragonSlime is a fork of Slimefun5 (itself a fork of Slimefun4): a Paper/Bukkit
plugin that ships as a **single "universal" jar** meant to load on every server
from MC 1.8.8 up to 26.x. Almost every unusual thing in the build and the code
exists to keep that one jar working across that whole range.

## Commands

Run from the repo root in PowerShell. Gradle's foojay resolver auto-provisions
the JDKs (8 for main code, 25 for tests).

```powershell
.\gradlew.bat build                     # shadowJar (+ tests); output: core\build\libs\Slimefun-<version>.jar
.\gradlew.bat build -x test             # what CI runs
.\gradlew.bat :core:shadowJar           # just the plugin jar
.\gradlew.bat :core:test                # all whitelisted tests
.\gradlew.bat :core:test --tests "*BootSmokeTest"   # one test class
```

Dev server (`run-paper`, server dir `core\run\<mcVersion>\`, git-ignored):

```powershell
.\scripts\run.ps1                       # interactive menu: MC version + addons + branches; remembers last choice in build\.last-run.json
.\scripts\run-headless.ps1 -McVersion 26.3                     # boot core only, log to build\headless-<ver>.log, then stop
.\scripts\run-headless.ps1 -McVersion 1.8.8 -AllAddons           # boot with every known addon @ experimental
.\scripts\run-headless.ps1 -McVersion 26.3 -Addons "Slimefun5/InfinityExpansion@experimental"
.\gradlew.bat :core:runServer -PmcVersion=1.20.6 -Paddons=networks,infinityexpansion
```

Bundled addons and deploy:

```powershell
.\gradlew.bat deploy                    # core + every addon under addons\ -> deploy\ (also run by root `build`)
.\gradlew.bat :addons:Networks:shadowJar  # one addon
.\scripts\run-headless.ps1 -McVersion 26.3 -BundledAddons      # boot core + the in-repo addons
.\gradlew.bat :core:runServer -PbundledAddons
```

Use `run-headless.ps1` to verify a change boots cleanly: it waits for Paper's
`Done (` line plus a grace period, captures addon enable errors, then kills the
process tree. `run.ps1` exists because the Gradle daemon has no console, so an
in-build menu cannot read input.

`runServer` / `cloneAndBuildAddons` properties:

| Property | Effect |
|---|---|
| `-PmcVersion=<ver>` | Server version (default `26.3`); the server JVM is picked to match (8/16/17/21/25). |
| `-Paddons=<list>` | Comma list of `Owner/Repo[@branch]`, bare manifest ids (`networks`), or `all`. Ids are topo-sorted by manifest dependencies. |
| `-PskipAddons` | Core only. |
| `-PlocalAddons` | Build `core\build\addons-src\<Repo>` working copies as-is (no fetch/reset), to iterate on addon source. |
| `-PkeepPlugins` | Reuse jars already in the plugins folder; skip addon builds. |
| `-PstrictAddons` | Fail the build if any addon fails (otherwise a summary is printed and the boot continues). |
| `-PnoVia` | Don't install the Java-8 builds of ViaVersion/ViaBackwards/ViaRewind. |
| `-PbundledAddons` | Run `:deploy` and install the `addons\` jars from `deploy\` into the plugins folder (replacing older copies). |
| `-PdumpItems` | On boot, write `plugins/Slimefun/untranslated-items.yml` and `menus-baseline.yml` to audit translation coverage across loaded addons. |

Addons are cloned into `core\build\addons-src\` (throwaway clones, hard-reset to
origin each run; default branch is `experimental`), patched to compile against
the freshly built core jar, and copied into the run server's plugins folder.
The addon list comes from `https://raw.githubusercontent.com/Slimefun5/manifest/main/addons.json`,
falling back to the bundled `core\src\main\resources\addons.json`.

## Bundled addons (`addons/`)

InfinityExpansion, LiteXpansion, Networks, SlimefunAdvancements, SMG and Supreme
live in `addons/<Name>` as ordinary files, imported with full history via
`git subtree add` from `Slimefun5/<Name>` `development` (not submodules). They
are Gradle subprojects (`:addons:<Name>`, listed in `settings.gradle.kts`):

- Each applies `addons/addon.gradle`, which compiles it against **this build's
  core shadow jar** (Java 8, `spigot-api` 1.16.5), excludes core classes and
  `SlimefunMetrics.jar` from its shadow jar, and stamps `slimefun-api:` with the
  core version.
- Versions come from `addonVersion` in each addon's `gradle.properties` (repo
  tags belong to core). Bump it there when releasing an addon.
- Networks compiles against the in-tree InfinityExpansion shadow jar.
- The root `deploy` task syncs the core jar and all addon jars into `deploy/`
  (git-ignored); root `build` runs it.
- To pull later upstream addon work: `git subtree pull --prefix=addons/<Name>
  https://github.com/Slimefun5/<Name>.git development`, then re-check the
  addon's `build.gradle.kts` still applies `addons/addon.gradle`.

## Build constraints that shape the code

- **Main code is Java 8 bytecode, compiled against `spigot-api:1.8.8`.** No
  Java 9+ language features or APIs (no `var`, `List.of`, records, switch
  expressions, `String.isBlank`, etc.). Any post-1.8 Bukkit/Paper API must go
  through reflection or the compat layer.
- **`stubs/`** holds compile-only stand-ins for newer `org.bukkit`, Paper and
  Adventure types (`NamespacedKey`, `BlockData`, `Particle`, `Component`, ...).
  They are not shaded; the server's real classes are used at runtime. Calling a
  stub type on a server that lacks it throws, so guard such calls by version.
- **Netty is pinned to 4.0.23** (what MC 1.8 ships) so 4.1-only APIs fail to compile.
- `WorldEditIntegration` and `PlaceholderAPIIntegration` are excluded from
  compilation (they subclass Java 17 types).
- The shadowJar relocates dough, PaperLib, commons-lang and XSeries under
  `io.github.thebusybiscuit.slimefun5.libraries.*`, then **binary-patches
  XSeries' `XMaterial$Data` version regex** so it parses `26.x`. The build fails
  rather than ship an unpatched jar; if it can't replace the jar, a running
  server is usually holding it — stop the server and rebuild.
- H2/MySQL JDBC drivers are **not** shaded; `StorageDriverLoader` loads or
  downloads them at runtime. H2 is pinned to 2.1.214 (last Java 8 release).
- `plugin.yml` declares `api-version: '1.13'` on purpose (the lowest
  flattening-era value, so 1.13+ servers accept it; pre-1.13 ignores it).
- Version comes from git tags via the shared
  `Slimefun5/gradle` `version.gradle` snippet (applied over the network);
  jars are named `<Name>-<version>-UNOFFICIAL.jar`.

## Tests

- Tests compile and run on **Java 25 against MockBukkit (mock MC 1.21) and
  `paper-api 1.21.11`**, not against the 1.8.8 API + stubs used by main code.
  The test classpath deliberately does not extend `compileOnly`.
- **Tests are whitelisted.** Only classes listed in `core/build.gradle.kts` are
  compiled and run; most of the ~150 upstream tests predate the Java 8 port and
  don't compile. A new test class must be added to **both** the
  `compileTestJava` `include(...)` list and the `test` `include(...)` list, or
  it silently never runs.
- `forkEvery = 1`: each test class gets its own JVM because
  `SlimefunItemSetup.setup()` has a JVM-static "registered once" guard.
- `BootSmokeTest` boots the whole plugin under MockBukkit and registers every
  item, catching registration/lore errors at build time.
- JDBC tests run against `jdbc:h2:mem` (MySQL dialect via `MODE=MySQL`).

## Code layout

Main class: `io.github.thebusybiscuit.slimefun5.implementation.Slimefun`.

- `slimefun5.api` — public addon API (items, events, recipes, researches, networks, GPS).
- `slimefun5.core` — services, guide, commands, multiblocks, machines, networks,
  `balance` (per-item balance config), `debug`.
- `slimefun5.implementation` — concrete items, listeners, tasks, guide UI,
  `setup` (item/research registration).
- `slimefun5.utils.compatibility` — **the cross-version layer**: `*Compat`
  classes and `Versioned*` enums wrap APIs that differ between 1.8 and 26.x
  (materials, particles, sounds, PDC, item meta, potions, registries, hands).
  `compatibility.packet` does packet-level item/window-title translation via a
  Netty `ChannelDuplexHandler` and reflection (`PacketReflect`). Reach for these
  before calling a newer Bukkit API directly; reflective handles are resolved
  once and cached, not per call.
- `slimefun5.libraries.keys` — a `NamespacedKey`/`Keyed` implementation usable on 1.8.
- `slimefun5.storage` — block/player data behind `BlockStorageBackend`, with
  `legacy` (file), `jdbc` (H2/MySQL) backends and a `migration` service between them.
- `io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem` — a deliberately
  minimal shim so third-party plugins built against the old `slimefun4` package
  can still identify items. Extend it only when a real plugin needs a member.
- `me.mrCookieSlime.*` — legacy CS-CoreLib API kept for old addons (BlockMenu,
  inventories, item handlers).

Resources: `languages/` (translations, synced via Crowdin), `wiki/` + `wiki.json`
(in-game wiki pages), `tags/`, `biome-maps/`, `item-models.yml`, `addons.json`
(bundled manifest snapshot), and `SlimefunMetrics.jar` (a bundled metrics module).

## Branches and workflow

- This checkout's branch is `main` (remote `DragonFixes/DragonSlime`). Upstream
  Slimefun5 works on `experimental` and promotes to `stable` via
  `.github/workflows/promote.yml` on `v*` / `promote-*` tags.
- Addon repos (`Slimefun5/*`) carry the fork's work on their `experimental`
  branch; their `stable`/default branch is older.
- CI (`.github/workflows/ci.yaml`) runs `build -x test` on Java 25 through the
  shared `intisy/workflows` workflows; tests are not run in CI, so run
  `:core:test` locally.
- `README.md` is generated, never edited by hand. `.github/workflows/readme.yaml`
  rebuilds it on pushes to `main` (commits titled `docs: auto-generate README.md`)
  with `intisy/workflows`' `generate-readme.ts`, from `.github/docs-config.yml`
  (title, description, badges, requirements, section order) plus `CONTENT.md`
  (features, bundled addons, building, developer API, credits). Edit those two,
  then regenerate locally to preview (needs Node 24+):
  ```powershell
  $b64 = (gh api repos/intisy/workflows/contents/.github/actions/generate-readme/generate-readme.ts --jq .content) -join ''
  [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($b64)) | Set-Content "$env:TEMP\gen-readme.ts"
  node "$env:TEMP\gen-readme.ts" --repository DragonFixes/DragonSlime --offline
  ```
  When adding or removing a bundled addon, update its rows in `CONTENT.md`'s
  "Bundled addons" and "Credits" tables.
