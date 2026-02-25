# 1.19.2 Porting Notes

This document tracks the migration from the `1.19.4` branch baseline to `1.19.2`.

## Scope

- Retarget dependency coordinates and metadata for 1.19.2.
- Keep existing event behavior unless 1.19.2 API differences require fixes.
- Capture compile/runtime issues and incremental fixes.

## Baseline Changes Applied

- `gradle.properties`
  - `minecraft_version=1.19.2`
  - `yarn_mappings=1.19.2+build.28`
  - `fabric_api_version=0.77.0+1.19.2`

- `build.gradle`
  - Java release/source/target kept at `17`

- `src/main/resources/fabric.mod.json`
  - `depends.minecraft` updated to `~1.19.2`
  - `depends.java` remains `>=17`

- `README.md`
  - Requirements updated to 1.19.2 values

## Validation Checklist

- Run `./gradlew clean build`
- Run `./gradlew runClient`
- If mixin/apply errors appear, patch descriptors/signatures and rerun.
- Re-test core events:
  - `player_hurt`
  - `player_death`
  - `animal_tamed`
  - `rare_item_found`
  - `achievement_earned`

## Issues Fixed During Port

### Compile errors: `net.minecraft.registry.*` not found

Issue:
- 1.19.2 uses older registry packages/APIs, so imports like `net.minecraft.registry.Registries`,
  `net.minecraft.registry.RegistryKeys`, and `net.minecraft.registry.entry.RegistryEntry` fail.

Fix:
- Switched affected classes to 1.19.2 registry APIs:
  - `net.minecraft.util.registry.Registry`
  - `net.minecraft.util.registry.RegistryEntry`
- Replaced `Registries.*` usage with `Registry.*`.
- Simplified structure ID lookup in `StructureTracker` to use `Registry.STRUCTURE.getId(...)`.

Files:
- `event/player/StatusEffectListener.java`
- `event/player/RareItemListener.java`
- `event/player/BreedingListener.java`
- `event/player/BrewingListener.java`
- `event/player/FishingListener.java`
- `event/player/TamingListener.java`
- `event/player/TradingListener.java`
- `event/world/BiomeTracker.java`
- `event/world/StructureTracker.java`
- `mixin/HorseTamingMixin.java`

### Remap warning: `ScreenHandler.onClosed` not found

Issue:
- Loom reported `Cannot remap onClosed ... ScreenHandler` on 1.19.2.

Fix:
- Updated `BrewingMixin` injection target from `onClosed` to `close`.

Files:
- `mixin/BrewingMixin.java`
