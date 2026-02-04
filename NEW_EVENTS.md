# New Events Summary

This document summarizes all new events added to Phase_Pulse.

---

## 1. `sleep_failed`

Triggered when player attempts to sleep but cannot.

```json
{
  "event": "sleep_failed",
  "timestamp": 1737241200,
  "metadata": {
    "reason": "monsters_nearby",
    "monsters_nearby": true
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `reason` | string | Why sleep failed |
| `monsters_nearby` | boolean | True if due to hostile mobs |

**Reason values:**
- `monsters_nearby` - Hostile mobs nearby
- `not_possible_now` - Daytime
- `not_possible_here` - Invalid location
- `too_far_away` - Too far from bed
- `obstructed` - Bed blocked
- `other_problem` - Other issue

**Example messages:**
- "Can't sleep with company around…"
- "Monsters nearby. Sleep denied."
- "Clear the area first."

---

## 2. `harmful_effect`

Triggered when player receives a harmful status effect.

```json
{
  "event": "harmful_effect",
  "timestamp": 1737241200,
  "metadata": {
    "effect": "poison",
    "amplifier": 0,
    "duration_seconds": 30,
    "is_damaging": true
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `effect` | string | Effect type |
| `amplifier` | number | Level (0 = I, 1 = II) |
| `duration_seconds` | number | Time remaining |
| `is_damaging` | boolean | True if causes HP loss |

**Tracked effects:**

| Effect | is_damaging |
|--------|-------------|
| `poison` | true |
| `wither` | true |
| `hunger` | false |
| `mining_fatigue` | false |
| `weakness` | false |
| `blindness` | false |
| `nausea` | false |
| `slowness` | false |
| `levitation` | false |
| `darkness` | false |
| `infested` | false |
| `oozing` | false |
| `weaving` | false |
| `wind_charged` | false |

**Example messages:**
- Poison: "Feeling a bit green..."
- Wither: "The darkness consumes you..."
- Blindness: "Can't see a thing!"

---

## 3. `inventory_organizing`

Triggered when player spends 20+ seconds in an inventory screen.

```json
{
  "event": "inventory_organizing",
  "timestamp": 1737241200,
  "metadata": {
    "screen_type": "chest",
    "duration_seconds": 45
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `screen_type` | string | Type of inventory |
| `duration_seconds` | number | Time spent |

**Screen types:**
`inventory`, `creative`, `chest`, `shulker_box`, `dispenser`, `hopper`, `furnace`, `blast_furnace`, `smoker`, `brewing_stand`, `enchanting_table`, `anvil`, `smithing_table`, `grindstone`, `loom`, `cartography_table`, `stonecutter`, `crafting_table`, `villager`, `beacon`

**Example messages:**
- "Inventory check? Smart."
- "Marie Kondo would be proud."
- "Chest Tetris champion over here."
- "Order from chaos."

---

## 4. `structure_discovery`

Triggered when player enters a structure for the first time.

```json
{
  "event": "structure_discovery",
  "timestamp": 1737241200,
  "metadata": {
    "structure": "ancient_city",
    "is_dangerous": true
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `structure` | string | Structure type |
| `is_dangerous` | boolean | True if contains threats |

**Structures:**

| Structure | is_dangerous | Message |
|-----------|--------------|---------|
| `village` | false | "A village! Friendly faces ahead." |
| `desert_temple` | false | "Temple spotted. Watch for traps." |
| `jungle_temple` | false | "Loot inside! And danger." |
| `witch_hut` | false | "Witch hut nearby..." |
| `igloo` | false | "Igloo! Cozy." |
| `ocean_monument` | **true** | "Ocean monument… ready for a swim?" |
| `ocean_ruins` | false | "Ocean ruins! Treasure?" |
| `shipwreck` | false | "Shipwreck spotted!" |
| `buried_treasure` | false | "X marks the spot!" |
| `nether_fortress` | **true** | "Nether fortress. Blaze time." |
| `bastion_remnant` | **true** | "Bastion! Piglins ahead." |
| `ruined_portal` | false | "Ruined portal found." |
| `end_city` | false | "End city! Elytra hunt!" |
| `stronghold` | **true** | "Stronghold! The End awaits." |
| `woodland_mansion` | **true** | "Woodland mansion. Spooky." |
| `mineshaft` | false | "Mineshaft! Watch for cave spiders." |
| `pillager_outpost` | false | "Pillager outpost nearby." |
| `ancient_city` | **true** | "Ancient city. Silent and deadly." |
| `trail_ruins` | false | "Trail ruins! Archaeology time!" |
| `trial_chambers` | **true** | "Trial chambers! Good luck." |

---

## 5. `rare_item_found`

Triggered when player picks up a rare item.

```json
{
  "event": "rare_item_found",
  "timestamp": 1737241200,
  "metadata": {
    "item": "totem_of_undying",
    "category": "totem_of_undying",
    "count": 1,
    "is_very_rare": true
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `item` | string | Specific item ID |
| `category` | string | Item category |
| `count` | number | Amount picked up |
| `is_very_rare` | boolean | Exceptionally rare |

**Very rare items (`is_very_rare: true`):**

| Item | Message |
|------|---------|
| `totem_of_undying` | "Totem of Undying! That's huge!" |
| `trident` | "TRIDENT! That's rare!" |
| `elytra` | "ELYTRA! You can fly now!" |
| `dragon_egg` | "THE Dragon Egg!" |
| `nether_star` | "Nether Star! Beacon time!" |
| `heart_of_the_sea` | "Heart of the Sea!" |
| `enchanted_golden_apple` | "Notch apple! Jackpot!" |
| `beacon` | "Beacon acquired!" |
| `netherite_ingot` | "Netherite! Top tier." |
| `dragon_head` | "Dragon head trophy!" |

**Notable items:**

| Item | Message |
|------|---------|
| `music_disc` | "Music disc! Jukebox time!" |
| `enchanted_book` | "Enchanted book! What's it got?" |
| `diamond` | "Diamonds!" |
| `ancient_debris` | "Ancient debris! Netherite incoming." |
| `shulker_shell` | "Shulker shell! Portable storage." |

---

## 6. `animal_tamed`

Triggered when player tames an animal.

```json
{
  "event": "animal_tamed",
  "timestamp": 1737241200,
  "metadata": {
    "animal": "wolf",
    "trait": "companion"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `animal` | string | Animal type |
| `trait` | string | What it's useful for |

**Animals:**

| Animal | Trait | Message |
|--------|-------|---------|
| `wolf` | `companion` | "Wolf tamed! New friend!" |
| `cat` | `utility` | "Cat tamed! Creeper repellent!" |
| `parrot` | `companion` | "Parrot tamed! Shoulder buddy!" |
| `horse` | `mount` | "Horse tamed! Speed unlocked!" |
| `donkey` | `mount` | "Donkey tamed! Mobile storage!" |
| `mule` | `mount` | "Mule tamed!" |
| `llama` | `utility` | "Llama tamed! Caravan time!" |
| `camel` | `mount` | "Camel tamed! Duo riding!" |
| `axolotl` | `companion` | "Axolotl buddy!" |

---

## Quick Reference

| Event | Category | Key Fields |
|-------|----------|------------|
| `sleep_failed` | Player | `reason`, `monsters_nearby` |
| `harmful_effect` | Player | `effect`, `is_damaging` |
| `inventory_organizing` | Player | `screen_type`, `duration_seconds` |
| `structure_discovery` | World | `structure`, `is_dangerous` |
| `rare_item_found` | Rare | `item`, `category`, `is_very_rare` |
| `animal_tamed` | Rare | `animal`, `trait` |

---

## Debounce Times

| Event | Cooldown |
|-------|----------|
| `sleep_failed` | 3 seconds |
| `harmful_effect` | 30 seconds per effect |
| `inventory_organizing` | 2 minutes |
| `structure_discovery` | Once per structure type |
| `rare_item_found` | 30 seconds per item |
| `animal_tamed` | 10 seconds per animal |
