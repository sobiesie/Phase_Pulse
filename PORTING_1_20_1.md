# 1.20.1 Porting Notes

This document tracks the migration from the `1.20.4` branch baseline to `1.20.1`.

## Scope

- Retarget dependency coordinates and metadata for 1.20.1.
- Keep existing event behavior unchanged unless 1.20.1 API differences require fixes.
- Record any compile/runtime mixin issues found during launch testing.

## Baseline Changes Applied

- `gradle.properties`
  - `minecraft_version=1.20.1`
  - `yarn_mappings=1.20.1+build.9`
  - `fabric_api_version=0.92.5+1.20.1`

- `build.gradle`
  - Java release/source/target set to `17`

- `src/main/resources/fabric.mod.json`
  - `depends.minecraft` updated to `~1.20.1`
  - `depends.java` remains `>=17`

- `src/main/resources/phase-pulse.mixins.json`
  - `compatibilityLevel` set to `JAVA_17`

- `README.md`
  - Requirements updated to 1.20.1 values

## Validation Checklist

- Run `./gradlew clean build`
- Run `./gradlew runClient`
- If mixin apply errors appear, patch the reported descriptor/signature and rerun.
- Re-test core events:
  - `player_hurt`
  - `player_death`
  - `animal_tamed`
  - `rare_item_found`
  - `harmful_effect`

## Issues Fixed During Port

### Compile error: `AdvancementEntry` missing on 1.20.1

Issue:
- `net.minecraft.advancement.AdvancementEntry` does not exist in 1.20.1 mappings used by this branch.
- `AdvancementListener` and `AdvancementMixin` had direct imports/usages that blocked compilation.

Fix:
- Removed compile-time dependency on `AdvancementEntry`.
- Refactored advancement extraction to reflection-only logic:
  - Detect advancement-like toast field by type shape (`id()` / `getId()`).
  - Extract advancement identifier dynamically.

Files:
- `src/main/java/com/phasepal/phasepulse/event/player/AdvancementListener.java`
- `src/main/java/com/phasepal/phasepulse/mixin/AdvancementMixin.java`
