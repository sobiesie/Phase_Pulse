# 1.19.4 Porting Notes

This document tracks the migration from the `1.20.1` branch baseline to `1.19.4`.

## Scope

- Retarget dependency coordinates and metadata for 1.19.4.
- Keep existing event behavior unless 1.19.4 API differences require fixes.
- Capture compile/runtime issues and incremental fixes.

## Baseline Changes Applied

- `gradle.properties`
  - `minecraft_version=1.19.4`
  - `yarn_mappings=1.19.4+build.2`
  - `fabric_api_version=0.87.2+1.19.4`

- `build.gradle`
  - Java release/source/target kept at `17`

- `src/main/resources/fabric.mod.json`
  - `depends.minecraft` updated to `~1.19.4`
  - `depends.java` remains `>=17`

- `README.md`
  - Requirements updated to 1.19.4 values

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
