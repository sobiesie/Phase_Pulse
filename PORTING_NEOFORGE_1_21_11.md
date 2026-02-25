# NeoForge Porting Notes (1.21.11)

This document tracks migration of the project from Fabric to NeoForge on branch `1.21.11-neoforge`.

## Setup Applied

- Replaced Fabric Loom build with NeoForge ModDevGradle.
- Added NeoForge metadata:
  - `src/main/resources/META-INF/neoforge.mods.toml`
  - `[[mixins]]` entry for `phase-pulse.mixins.json`
- Added NeoForge version properties in `gradle.properties`:
  - `neo_version=21.11.37-beta`
  - `neo_version_range=[21.11.37-beta,)`
  - `moddevgradle_version=2.0.140`
- Replaced Fabric mod entry with NeoForge `@Mod` entry point.
- Replaced Fabric config path lookup (`FabricLoader`) with NeoForge path lookup (`FMLPaths.CONFIGDIR`).
- Removed Fabric-only descriptor file:
  - `src/main/resources/fabric.mod.json`

## Current Status

The Yarn/Fabric source remap has been applied enough for a clean NeoForge build.

- `./gradlew clean build` passes.
- Core bootstrap, event polling, and packet paths compile under NeoForge.
- Mixins compile against the NeoForge source set.

## Temporary Compatibility Fallbacks

Some hooks were intentionally left as placeholders to keep the port stable while method internals are validated at runtime:

- `StructureTracker` client structure detection is temporarily disabled.
- `TradingMixin` merchant-offer extraction is temporarily disabled.
- Command/event hook parity from Fabric should be rechecked in dev runtime.

## Next Migration Step

1. Run `runClient` and validate each event path in gameplay.
2. Re-enable structure/trade hooks with confirmed NeoForge method targets.
3. Re-verify every mixin injection signature in logs.
4. Update `CHANGELOG.md` with NeoForge port notes after runtime validation.

## Validation

- `./gradlew clean tasks` -> succeeds with NeoForge run tasks present (`runClient`, `runServer`, `runData`).
- `./gradlew clean build` -> succeeds.
