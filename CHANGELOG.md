# Changelog

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
| `HostileMobDetector.java` | Added `areHostilesNearby()` static method |
| `EventRegistry.java` | Added HurtListener registration |
| `DamageMixin.java` | Simplified (client detection moved to HurtListener) |
