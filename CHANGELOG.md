# Changelog

## 2026-02-04 - World Metadata for Events

### Automatic World Context

All events now include world metadata for better tracking across different worlds and sessions.

**New metadata fields on every event:**
- `world_name` - Level name (singleplayer) or server name (multiplayer)
- `world_type` - `"singleplayer"` or `"multiplayer"`
- `dimension` - `"overworld"`, `"the_nether"`, or `"the_end"`

**Example output:**
```json
{
  "event": "biome_discovery",
  "timestamp": 1234567890,
  "metadata": {
    "biome": "minecraft:forest",
    "world_name": "My World",
    "world_type": "singleplayer",
    "dimension": "overworld"
  }
}
```

**Files Changed:**
| File | Change |
|------|--------|
| `NetworkManager.java` | Added `injectWorldMetadata()` to automatically add world context to all events |

---

## 2026-02-04 - Client-Side Event Detection Migration

### Server-to-Client Migration

**Problem:** Several event listeners were using server-side Fabric API hooks (`ServerLivingEntityEvents.AFTER_DEATH`, `PlayerBlockBreakEvents.AFTER`) which don't fire reliably for client-side mods.

**Solution:** Migrated to client-side detection patterns:

#### MobKilledListener
- Tracks entities attacked by player via `player.getAttacking()`
- Monitors nearby entities for death state transitions
- Adds `is_hostile` and `is_animal` metadata to events
- Uses 16-block tracking range with 10-second timeout

#### DeathListener
- Monitors `player.isDead()` state transition each tick
- Uses `getRecentDamageSource()` for death cause

#### BlockBrokenListener
- New `BlockBreakMixin` hooks into `ClientPlayerInteractionManager.breakBlock`
- Listener converted to static method called from mixin

### HealthMonitor Death Suppression

**Problem:** When player dies (health = 0), `low_health` events were still being sent, which was misleading since `player_death` handles death.

**Solution:** Added check to suppress low health events when `health <= 0`.

### Files Changed

| File | Change |
|------|--------|
| `MobKilledListener.java` | Rewritten for client-side entity tracking |
| `DeathListener.java` | Rewritten for client-side death detection |
| `BlockBrokenListener.java` | Converted to static method for mixin |
| `BlockBreakMixin.java` | New mixin for block break detection |
| `EventRegistry.java` | Updated tick handlers and removed server registrations |
| `HealthMonitor.java` | Suppress events when health <= 0 |
| `phase-pulse.mixins.json` | Added BlockBreakMixin |

---

## 2026-02-03 - Hostile Mob Detection Fix

### Cave Mob False Positives Fix

**Problem:** Users reported receiving hostile mob alerts (e.g., creepers) when mobs were in caves below or above them, even though those mobs posed no immediate threat.

**Root Cause:** `HostileMobDetector` used a uniform 16-block detection radius in all directions, including vertical. This meant mobs 16 blocks below in a cave would trigger alerts.

**Solution:** Added separate vertical detection radius:

- Horizontal detection: 16 blocks (unchanged)
- Vertical detection: 4 blocks (reduced from 16)

**Result:** Mob alerts now focus on mobs at similar elevation to the player, avoiding false positives from cave systems.

---

## 2026-01-19 - Event Detection Fixes

### Biome Boundary Spam Fix

**Problem:** When standing on a biome boundary (e.g., between `grove` and `stony_shore`), small movements like jumping or digging caused rapid flip-flopping biome_changed events - sometimes 18+ events in 2 minutes.

**Solution:** Added hysteresis and cooldown to `BiomeTracker.java`:

- **Hysteresis (HYSTERESIS_COUNT = 10):** Player must be in the new biome for 10 consecutive checks (10 seconds at 1-second intervals) before a biome change is confirmed. Boundary flickering resets the counter.
- **Cooldown (BIOME_CHANGE_COOLDOWN_MS = 5000):** Minimum 5 seconds between `biome_changed` events.

**Result:** Only sustained biome transitions trigger events. Quick boundary crossings are ignored.

---

### Fall Damage Detection Fix

**Problem:** `player_hurt` events were not being sent when taking fall damage.

**Root Cause:** `DamageMixin` targeted `damage(ServerWorld, ...)` - a server-only method in Minecraft 1.21+. The condition `player.getEntityWorld().isClient()` was always false because that method never runs on the client.

**Solution:** Rewrote `HurtListener.java` to use client-side detection:

- Monitors `player.hurtTime` every client tick
- When `hurtTime` transitions from 0 to >0 (red flash animation starts), damage was taken
- Tracks health delta between ticks to estimate damage amount
- Registered in `EventRegistry` alongside other client tick monitors

**Files Changed:**
- `HurtListener.java` - Complete rewrite from static utility to tick-based monitor
- `EventRegistry.java` - Added HurtListener instantiation and tick registration
- `DamageMixin.java` - Simplified to stub (server-side hook unused for client mod)

---

### Combat False Positives Fix

**Problem:** Fall damage and other environmental damage incorrectly triggered `combat_start`/`combat_end` events.

**Root Cause:** `HurtListener` called `combatTracker.onPlayerDamaged()` for ALL damage types.

**Solution:** Added hostile mob proximity check:

- Added `HostileMobDetector.areHostilesNearby()` static method (8-block radius)
- `HurtListener` only triggers combat when hostile mobs are within range
- Environmental damage (fall, drowning, fire, etc.) no longer starts combat

**Result:**
- `player_hurt` fires for ALL damage types
- `combat_start` only fires when damage is taken with hostiles nearby

---

## Summary of Modified Files

| File | Change |
|------|--------|
| `BiomeTracker.java` | Added hysteresis + cooldown for boundary spam prevention |
| `HurtListener.java` | Rewritten to use client-side hurtTime monitoring |
| `HostileMobDetector.java` | Added `areHostilesNearby()` static method; limited vertical detection to 4 blocks |
| `EventRegistry.java` | Added HurtListener registration |
| `DamageMixin.java` | Simplified (client detection moved to HurtListener) |
