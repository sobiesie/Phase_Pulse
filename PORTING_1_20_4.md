# 1.20.4 Porting Notes

This document tracks technical issues and fixes applied while porting Phase_Pulse from 1.21.11 to 1.20.4.

## Scope

- Keep behavior as close as possible to the existing 1.21 branch.
- Remove or safely degrade 1.21-only APIs/content.
- Make the dev client launch cleanly on 1.20.4.

## Environment Targets

- `minecraft_version`: `1.20.4`
- `yarn_mappings`: `1.20.4+build.3`
- `fabric_api_version`: `0.97.3+1.20.4`
- Java target: `17`

## Issues Encountered and Fixes

### 1) Mapping resolution failure

Issue:
- `Could not find net.fabricmc:yarn:1.20.4+build.4`

Fix:
- Changed mappings to `1.20.4+build.3` in `gradle.properties`.

---

### 2) Compile errors from 1.21-only status effects

Issue:
- Missing symbols in `StatusEffects`: `INFESTED`, `OOZING`, `WEAVING`, `WIND_CHARGED`
- Type mismatch around `getEffectType()` and `RegistryEntry<StatusEffect>`
- `getIdAsString()` not available on the 1.20.4 path used here

Fix:
- Refactored `StatusEffectListener` to use `StatusEffect` values available in 1.20.4.
- Kept optional 1.21 effect names as plain strings (no direct constants).
- Switched effect ID lookup to `Registries.STATUS_EFFECT.getId(effect)`.

Files:
- `src/main/java/com/phasepal/phasepulse/event/player/StatusEffectListener.java`

---

### 3) Compile errors from 1.21-only item constants

Issue:
- Missing `Items.MUSIC_DISC_CREATOR`
- Missing `Items.MUSIC_DISC_CREATOR_MUSIC_BOX`
- Missing `Items.MUSIC_DISC_PRECIPICE`
- Missing `Items.TURTLE_SCUTE`
- `Identifier.of(itemId)` signature mismatch for target version

Fix:
- Refactored `RareItemListener` to track by item ID strings instead of hard item constants.
- Removed direct usage of `Identifier.of(itemId)` for reverse lookup.

Files:
- `src/main/java/com/phasepal/phasepulse/event/player/RareItemListener.java`

---

### 4) Mod dependency mismatch at runtime

Issue:
- Fabric Loader rejected mod because `fabric.mod.json` still required `minecraft ~1.21.11`.

Fix:
- Updated `fabric.mod.json` dependencies:
  - `minecraft: ~1.20.4`
  - `java: >=17`

Files:
- `src/main/resources/fabric.mod.json`

---

### 5) Mixin descriptor mismatch: `DamageMixin`

Issue:
- Injection expected `damage(DamageSource, float)` but mixin used `damage(ServerWorld, DamageSource, float)`.

Fix:
- Updated injector method signature to remove `ServerWorld` parameter.

Files:
- `src/main/java/com/phasepal/phasepulse/mixin/DamageMixin.java`

---

### 6) Mixin descriptor mismatch: `TamingMixin`

Issue:
- Injection expected `setOwner(PlayerEntity)` but mixin used `setOwner(LivingEntity)`.

Fix:
- Updated injector parameter type to `PlayerEntity`.

Files:
- `src/main/java/com/phasepal/phasepulse/mixin/TamingMixin.java`

## Validation Checklist

- `./gradlew clean build`
- `./gradlew runClient`
- Confirm no mixin apply errors during launch.
- Sanity-check core events:
  - `player_hurt`
  - `player_death`
  - `animal_tamed`
  - `rare_item_found`
  - `harmful_effect`

## Follow-up (Optional)

- Update `README.md` requirements text to match 1.20.4 branch values.
- Add a dedicated compatibility section in `EVENTS.md` for version-specific events.
