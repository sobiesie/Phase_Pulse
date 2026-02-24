# Phase_Pulse Event Documentation

This document describes all events sent by the Phase_Pulse mod to the Phase Pal companion app via TCP socket on `localhost:32145`.

## Recent Changes

### January 2026 - Client-Side Implementation Fixes

**Bug Fixes:**
- Fixed `DamageMixin` targeting server-side method signature that doesn't exist on client (issue: `player_hurt` events not firing)
  - **Problem:** Mixin was targeting `damage(ServerWorld, DamageSource, float)` which is server-side only
  - **Fix:** Changed to target `damage(DamageSource, float)` for client-side compatibility
  - **Location:** `DamageMixin.java:19-20`
  - **Note:** If damage events still don't fire, verify the method signature doesn't include `ServerWorld` parameter and uses `isClient()` not `isClient`

- Fixed `AdvancementMixin` targeting server-side `PlayerAdvancementTracker` class
  - **Problem:** Mixin was targeting server-side advancement tracking, not available on client
  - **Fix:** Completely rewrote to hook into client-side `ToastManager` and `AdvancementToast` notifications
  - **Implementation:** Uses reflection to extract advancement data from toast notifications
  - **Location:** `AdvancementMixin.java`, `AdvancementListener.java`

**Code Cleanup:**
- Removed `ExampleMixin` template code (targeted server-side `MinecraftServer.loadWorld`)
- Cleaned up unused server-side imports from `CombatTracker` (`ServerEntityEvents`, `ServerPlayerEntity`)
- Removed `ExampleMixin` from mixin registry (`phase-pulse.mixins.json`)

**Feature Changes:**
- ~~**Disabled `block_placed` event**~~ - Re-enabled as of February 2026

**Impact:**
- All mixins now correctly target client-side code only
- `player_hurt` events now fire correctly (fall damage, mob attacks, etc.)
- `achievement_earned` events now work client-side via toast detection
- `combat_start` and `combat_end` events now trigger properly

---

## JSON Message Format

All events follow this standard JSON schema:

```json
{
  "event": "event_name",
  "timestamp": 1234567890,
  "metadata": {
    "key": "value"
  }
}
```

**Fields:**
- `event` (string): The event type/name
- `timestamp` (number): Unix timestamp in seconds
- `metadata` (object): Event-specific data (may be empty `{}`)

---

## Player State Events

### `low_health`
Triggered when player's health drops below 30% (6.0 HP out of 20.0).

**Trigger conditions:**
- Health percentage < 30%
- Debounced: Re-triggers every 10 seconds while health remains low

**Example:**
```json
{
  "event": "low_health",
  "timestamp": 1737241184,
  "metadata": {
    "health": 5.5,
    "maxHealth": 20.0,
    "healthPercent": 28
  }
}
```

**Metadata fields:**
- `health` (number): Current health points (rounded to 1 decimal)
- `maxHealth` (number): Maximum health points
- `healthPercent` (number): Health percentage (0-100, rounded)

---

### `low_hunger`
Triggered when player's hunger drops below 3 bars (6 hunger points out of 20).

**Trigger conditions:**
- Hunger level < 6
- Debounced: Re-triggers every 10 seconds while hunger remains low

**Example:**
```json
{
  "event": "low_hunger",
  "timestamp": 1737241185,
  "metadata": {
    "hunger": 4,
    "saturation": 2.5
  }
}
```

**Metadata fields:**
- `hunger` (number): Current hunger level (0-20)
- `saturation` (number): Current saturation level (rounded to 1 decimal)

---

### `drowning`
Triggered when player is underwater and running out of air.

**Trigger conditions:**
- Air level < 100 (less than 5 seconds of air remaining)
- Air level < max air
- Debounced: Re-triggers every 5 seconds while drowning

**Example:**
```json
{
  "event": "drowning",
  "timestamp": 1737241186,
  "metadata": {
    "air": 80,
    "maxAir": 300
  }
}
```

**Metadata fields:**
- `air` (number): Current air level
- `maxAir` (number): Maximum air capacity (usually 300)

---

### `item_consumed`
Triggered when player consumes food or drinks a potion.

**Trigger conditions:**
- Player finishes eating/drinking an item
- Debounced: 250ms between consumption events

**Example:**
```json
{
  "event": "item_consumed",
  "timestamp": 1737241187,
  "metadata": {
    "item": "bread"
  }
}
```

**Metadata fields:**
- `item` (string): The item consumed (item ID without `minecraft:` prefix)

**Common items:**
- `bread`
- `cooked_beef`
- `golden_apple`
- `potion`
- etc.

---

### `player_sleep`
Triggered when player enters a bed and starts sleeping.

**Trigger conditions:**
- Player lies down in bed
- Debounced: 250ms between sleep events

**Example:**
```json
{
  "event": "player_sleep",
  "timestamp": 1737241188,
  "metadata": {
    "world_time": 13000
  }
}
```

**Metadata fields:**
- `world_time` (number): Current world time (0-24000 cycle)

**World time reference:**
- 0 = Dawn
- 6000 = Noon
- 12000 = Dusk
- 18000 = Midnight

---

### `player_wake`
Triggered when player exits a bed (wakes up or gets out of bed).

**Trigger conditions:**
- Player stops sleeping
- Debounced: 250ms between wake events

**Example:**
```json
{
  "event": "player_wake",
  "timestamp": 1737241189,
  "metadata": {
    "world_time": 0
  }
}
```

**Metadata fields:**
- `world_time` (number): Current world time after waking

---

### `sleep_failed`
Triggered when player attempts to sleep but cannot.

**Trigger conditions:**
- Player interacts with bed but sleep is denied
- Debounced: 3 seconds between sleep failure events

**Example:**
```json
{
  "event": "sleep_failed",
  "timestamp": 1737241189,
  "metadata": {
    "reason": "monsters_nearby",
    "monsters_nearby": true
  }
}
```

**Metadata fields:**
- `reason` (string): Why sleep failed
- `monsters_nearby` (boolean): True if specifically due to hostile mobs

**Reason values:**
- `monsters_nearby` - Hostile mobs within range (NOT_SAFE)
- `not_possible_now` - Daytime or other timing issue
- `not_possible_here` - Invalid bed location
- `too_far_away` - Player too far from bed
- `obstructed` - Bed is blocked
- `other_problem` - Other failure reason

**Use case:** Display fun messages like "Can't sleep with company around..." when `monsters_nearby` is true.

---

### `player_hurt`
Triggered when player takes any damage.

**Trigger conditions:**
- Player receives damage from any source
- Debounced: 1 second between hurt events

**Example:**
```json
{
  "event": "player_hurt",
  "timestamp": 1737241190,
  "metadata": {
    "damage": 3.5,
    "source": "mob"
  }
}
```

**Metadata fields:**
- `damage` (number): Damage amount (rounded to 1 decimal)
- `source` (string): Damage source type (e.g., "mob", "fall", "fire", "player", etc.)

---

### `player_death`
Triggered when player dies.

**Trigger conditions:**
- Player health reaches 0
- Debounced: 250ms between death events

**Example:**
```json
{
  "event": "player_death",
  "timestamp": 1737241191,
  "metadata": {
    "cause": "zombie"
  }
}
```

**Metadata fields:**
- `cause` (string): Death cause/damage source

---

### `harmful_effect`
Triggered when player receives a harmful status effect.

**Trigger conditions:**
- Player gains a harmful status effect (Poison, Wither, etc.)
- Scanned every 20 ticks (1 second)
- Debounced: 30 seconds per effect type

**Example:**
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

**Metadata fields:**
- `effect` (string): The effect type identifier
- `amplifier` (number): Effect level (0 = level I, 1 = level II, etc.)
- `duration_seconds` (number): Remaining duration in seconds
- `is_damaging` (boolean): True if the effect causes direct damage (poison, wither)

**Tracked effects:**
| Effect | is_damaging | Description |
|--------|-------------|-------------|
| `poison` | true | Continuous damage (won't kill) |
| `wither` | true | Continuous damage (can kill) |
| `hunger` | false | Drains food faster |
| `mining_fatigue` | false | Slower mining speed |
| `weakness` | false | Reduced melee damage |
| `blindness` | false | Vision severely limited |
| `nausea` | false | Screen wobbles |
| `slowness` | false | Reduced movement speed |
| `levitation` | false | Uncontrollable floating |
| `unluck` | false | Worse loot from chests |
| `darkness` | false | Pulsing darkness effect |
| `infested` | false | Silverfish spawn on hurt (1.21+) |
| `oozing` | false | Slimes spawn on death (1.21+) |
| `weaving` | false | Cobwebs spawn on death (1.21+) |
| `wind_charged` | false | Wind burst on death (1.21+) |

**Use case:** Display messages like "Feeling a bit green..." when `effect` is "poison".

---

### `inventory_organizing`
Triggered when player spends significant time in an inventory/container screen.

**Trigger conditions:**
- Player closes an inventory screen after spending 20+ seconds in it
- Debounced: 2 minutes between organizing events

**Example:**
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

**Metadata fields:**
- `screen_type` (string): The type of inventory screen
- `duration_seconds` (number): How long they spent organizing

**Screen types:**
| Type | Description |
|------|-------------|
| `inventory` | Player inventory (E key) |
| `creative` | Creative mode inventory |
| `chest` | Chest, double chest, barrel, ender chest |
| `shulker_box` | Shulker box |
| `dispenser` | Dispenser, dropper |
| `hopper` | Hopper |
| `furnace` | Furnace |
| `blast_furnace` | Blast furnace |
| `smoker` | Smoker |
| `brewing_stand` | Brewing stand |
| `enchanting_table` | Enchanting table |
| `anvil` | Anvil |
| `smithing_table` | Smithing table |
| `grindstone` | Grindstone |
| `loom` | Loom |
| `cartography_table` | Cartography table |
| `stonecutter` | Stonecutter |
| `crafting_table` | Crafting table |
| `villager` | Villager trading |
| `beacon` | Beacon |

**Use case:** Display messages like "Marie Kondo would be proud." or "Chest Tetris champion over here."

---

### `block_placed`
Triggered when player places a block.

**Trigger conditions:**
- Player successfully places a block item
- Debounced: 100ms between placements

**Example:**
```json
{
  "event": "block_placed",
  "timestamp": 1737241192,
  "metadata": {
    "block": "stone"
  }
}
```

**Metadata fields:**
- `block` (string): The block type placed

---

### `block_broken`
Triggered when player breaks a block.

**Trigger conditions:**
- Player successfully breaks a block
- Debounced: 100ms between breaks

**Example:**
```json
{
  "event": "block_broken",
  "timestamp": 1737241193,
  "metadata": {
    "block": "Block{minecraft:oak_log}"
  }
}
```

**Metadata fields:**
- `block` (string): The block type broken

---

### `item_crafted`
Triggered when player crafts an item.

**Trigger conditions:**
- Player takes item from crafting output slot
- Debounced: 500ms per item type

**Example:**
```json
{
  "event": "item_crafted",
  "timestamp": 1737241194,
  "metadata": {
    "item": "minecraft:stick",
    "count": 4
  }
}
```

**Metadata fields:**
- `item` (string): The item crafted
- `count` (number): Quantity crafted

---

### `item_smelted`
Triggered when player takes output from a furnace, blast furnace, or smoker.

**Trigger conditions:**
- Player takes item from furnace output slot
- Debounced: 500ms per item type

**Example:**
```json
{
  "event": "item_smelted",
  "timestamp": 1737241194,
  "metadata": {
    "item": "iron_ingot",
    "count": 8
  }
}
```

**Metadata fields:**
- `item` (string): The smelted item
- `count` (number): Quantity taken

**Common smelted items:**
- `iron_ingot`, `gold_ingot`, `copper_ingot`
- `cooked_beef`, `cooked_porkchop`, `cooked_chicken`
- `glass`, `stone`, `smooth_stone`
- `charcoal`, `brick`, `nether_brick`

---

### `item_enchanted`
Triggered when player successfully enchants an item at an enchanting table.

**Trigger conditions:**
- Player clicks an enchantment option and it succeeds
- Debounced: 500ms

**Example:**
```json
{
  "event": "item_enchanted",
  "timestamp": 1737241195,
  "metadata": {
    "level_cost": 30
  }
}
```

**Metadata fields:**
- `level_cost` (number): The XP level cost of the enchantment (1-30)

**Use cases:**
- First enchantment milestone
- High-level enchantment tracking (level 30 enchants)
- Enchanting activity detection

---

### `anvil_used`
Triggered when player takes output from an anvil (repair, rename, or enchantment combining).

**Trigger conditions:**
- Player takes item from anvil output slot
- Debounced: 500ms

**Example:**
```json
{
  "event": "anvil_used",
  "timestamp": 1737241196,
  "metadata": {
    "result_item": "diamond_sword"
  }
}
```

**Metadata fields:**
- `result_item` (string): The resulting item from the anvil operation

**Use cases:**
- Tool maintenance tracking
- Named item creation
- Enchantment combining milestones

---

### `achievement_earned`
Triggered when player earns an advancement (achievement).

**Trigger conditions:**
- Player completes all criteria for an advancement
- Only triggers for **gameplay achievements** (filters out recipe unlocks)
- Debounced per achievement: Won't re-trigger for same advancement

**Example:**
```json
{
  "event": "achievement_earned",
  "timestamp": 1737241195,
  "metadata": {
    "achievement": "minecraft:story/mine_stone"
  }
}
```

**Metadata fields:**
- `achievement` (string): The advancement identifier

**Common achievements:**
- `minecraft:story/mine_stone` - Stone Age
- `minecraft:story/upgrade_tools` - Getting an Upgrade
- `minecraft:story/smelt_iron` - Acquire Hardware
- `minecraft:story/obtain_armor` - Suit Up
- `minecraft:story/mine_diamond` - Diamonds!
- `minecraft:adventure/root` - Adventure
- `minecraft:nether/root` - We Need to Go Deeper
- etc.

**Note:** Recipe advancements (e.g., `minecraft:recipes/...`) are **filtered out** as they auto-unlock and are not meaningful achievements.

---

## World Events

### `day_start`
Triggered when the world transitions from night to day.

**Trigger conditions:**
- World time transitions into range 0-1000
- Was previously night (time >= 12000)
- Debounced: 250ms between day start events

**Example:**
```json
{
  "event": "day_start",
  "timestamp": 1737241190,
  "metadata": {
    "world_time": 500
  }
}
```

**Metadata fields:**
- `world_time` (number): Current world time at day start

---

### `night_start`
Triggered when the world transitions from day to night.

**Trigger conditions:**
- World time transitions into range 12000-13000
- Was previously day (time < 12000)
- Debounced: 250ms between night start events

**Example:**
```json
{
  "event": "night_start",
  "timestamp": 1737241191,
  "metadata": {
    "world_time": 12500
  }
}
```

**Metadata fields:**
- `world_time` (number): Current world time at night start

---

### `weather_clear`
Triggered when weather changes to clear (no rain/thunder).

**Trigger conditions:**
- Weather state changes to clear
- Debounced: 250ms between weather events

**Example:**
```json
{
  "event": "weather_clear",
  "timestamp": 1737241192,
  "metadata": {}
}
```

**Metadata fields:** None

---

### `weather_rain`
Triggered when it starts raining (but not thundering).

**Trigger conditions:**
- Weather state changes to rain
- Debounced: 250ms between weather events

**Example:**
```json
{
  "event": "weather_rain",
  "timestamp": 1737241193,
  "metadata": {}
}
```

**Metadata fields:** None

---

### `weather_thunder`
Triggered when a thunderstorm starts.

**Trigger conditions:**
- Weather state changes to thunder
- Debounced: 250ms between weather events

**Example:**
```json
{
  "event": "weather_thunder",
  "timestamp": 1737241194,
  "metadata": {}
}
```

**Metadata fields:** None

---

### `biome_discovery`
Triggered when player enters a biome they haven't been to before (in this session).

**Trigger conditions:**
- Player enters a new biome (not previously discovered)
- Sampled every 20 ticks (1 second)
- Debounced per biome: Won't re-trigger for same biome

**Example:**
```json
{
  "event": "biome_discovery",
  "timestamp": 1737241195,
  "metadata": {
    "biome": "minecraft:forest"
  }
}
```

**Metadata fields:**
- `biome` (string): The biome identifier

**Common biomes:**
- `minecraft:plains`
- `minecraft:forest`
- `minecraft:desert`
- `minecraft:taiga`
- `minecraft:mountains`
- `minecraft:ocean`
- `minecraft:swamp`
- `minecraft:jungle`
- etc.

---

### `biome_changed`
Triggered every time player changes biome (different from biome_discovery).

**Trigger conditions:**
- Player moves from one biome to another
- Sampled every 20 ticks (1 second)
- Debounced: 250ms between biome changes

**Example:**
```json
{
  "event": "biome_changed",
  "timestamp": 1737241196,
  "metadata": {
    "from": "minecraft:plains",
    "to": "minecraft:forest"
  }
}
```

**Metadata fields:**
- `from` (string): The biome the player left
- `to` (string): The biome the player entered

---

### `structure_discovery`
Triggered when player enters a structure for the first time (per session).

**Trigger conditions:**
- Player enters a recognized structure
- First time entering this structure type in current session
- Sampled every 60 ticks (3 seconds)

**Example:**
```json
{
  "event": "structure_discovery",
  "timestamp": 1737241200,
  "metadata": {
    "structure": "village",
    "is_dangerous": false
  }
}
```

**Metadata fields:**
- `structure` (string): The structure type
- `is_dangerous` (boolean): True if structure contains significant threats

**Structure types:**

| Structure | is_dangerous | Description |
|-----------|--------------|-------------|
| `village` | false | Village (all variants) |
| `desert_temple` | false | Desert pyramid |
| `jungle_temple` | false | Jungle pyramid |
| `witch_hut` | false | Swamp hut |
| `igloo` | false | Igloo |
| `ocean_monument` | **true** | Ocean monument (guardians) |
| `ocean_ruins` | false | Underwater ruins |
| `shipwreck` | false | Shipwreck |
| `buried_treasure` | false | Buried treasure |
| `nether_fortress` | **true** | Nether fortress (blazes, wither skeletons) |
| `bastion_remnant` | **true** | Bastion remnant (piglins) |
| `nether_fossil` | false | Nether fossil |
| `ruined_portal` | false | Ruined portal (all variants) |
| `end_city` | false | End city |
| `stronghold` | **true** | Stronghold (silverfish, end portal) |
| `woodland_mansion` | **true** | Woodland mansion (illagers) |
| `mineshaft` | false | Mineshaft (cave spiders) |
| `pillager_outpost` | false | Pillager outpost |
| `ancient_city` | **true** | Ancient city (warden) |
| `trail_ruins` | false | Trail ruins |
| `trial_chambers` | **true** | Trial chambers (1.21+) |

**Use cases:**
- Village: "A village! Friendly faces ahead."
- Ocean monument: "Ocean monument... ready for a swim?"
- Ancient city: "Ancient city. Silent and deadly."
- Stronghold: "Stronghold! The End awaits."

---

## Combat Events

### `combat_start`
Triggered when player takes damage, entering combat state.

**Trigger conditions:**
- Player takes damage (amount > 0)
- Not already in combat
- Debounced: 250ms between combat start events

**Example:**
```json
{
  "event": "combat_start",
  "timestamp": 1737241196,
  "metadata": {}
}
```

**Metadata fields:** None

---

### `combat_end`
Triggered when player hasn't taken damage for 5 seconds, exiting combat state.

**Trigger conditions:**
- 5 seconds elapsed since last damage
- Was previously in combat
- Debounced: 250ms between combat end events

**Example:**
```json
{
  "event": "combat_end",
  "timestamp": 1737241201,
  "metadata": {
    "duration_seconds": 12
  }
}
```

**Metadata fields:**
- `duration_seconds` (number): How long the combat lasted (in seconds)

---

### `mob_killed`
Triggered when player kills a mob.

**Trigger conditions:**
- Player deals killing blow to an entity
- Debounced: 500ms per mob type

**Example:**
```json
{
  "event": "mob_killed",
  "timestamp": 1737241202,
  "metadata": {
    "mob_type": "EntityType{minecraft:zombie}"
  }
}
```

**Metadata fields:**
- `mob_type` (string): The type of mob killed

**Common hostile mobs:**
- `EntityType{minecraft:zombie}`
- `EntityType{minecraft:skeleton}`
- `EntityType{minecraft:creeper}`
- `EntityType{minecraft:spider}`
- `EntityType{minecraft:enderman}`

**Common passive mobs:**
- `EntityType{minecraft:cow}`
- `EntityType{minecraft:pig}`
- `EntityType{minecraft:sheep}`
- `EntityType{minecraft:chicken}`

---

### `hostile_mob_nearby`
Triggered when hostile mobs are detected within 16 blocks of the player.

**Trigger conditions:**
- One or more hostile entities within 16-block radius
- Scanned every 40 ticks (2 seconds)
- Debounced: 10 seconds between notifications

**Example:**
```json
{
  "event": "hostile_mob_nearby",
  "timestamp": 1737241202,
  "metadata": {
    "mob_type": "EntityType{minecraft:zombie}",
    "count": 3
  }
}
```

**Metadata fields:**
- `mob_type` (string): The type of the first hostile mob detected
- `count` (number): Total number of hostile mobs nearby

**Common hostile mobs:**
- `EntityType{minecraft:zombie}`
- `EntityType{minecraft:skeleton}`
- `EntityType{minecraft:creeper}`
- `EntityType{minecraft:spider}`
- `EntityType{minecraft:enderman}`
- `EntityType{minecraft:witch}`
- etc.

---

## Milestone Events

### `item_obtained`
Triggered when player picks up a milestone item for the first time.

**Trigger conditions:**
- Player acquires a milestone item in inventory
- Scanned every 10 ticks (0.5 seconds)
- Debounced: 60 seconds per item type

**Example:**
```json
{
  "event": "item_obtained",
  "timestamp": 1737241200,
  "metadata": {
    "item": "elytra"
  }
}
```

**Metadata fields:**
- `item` (string): The milestone item obtained

**Milestone items:**

| Item | Significance |
|------|--------------|
| `elytra` | End-game flight capability |
| `nether_star` | Wither boss defeated |
| `dragon_egg` | Ender Dragon defeated |
| `beacon` | Major infrastructure achievement |
| `totem_of_undying` | Raid victory or mansion exploration |

**Use cases:**
- Boss defeat confirmation
- End-game progression tracking
- Major achievement celebrations

---

### `first_night_survived`
Triggered once when the player survives their first night without sleeping.

**Trigger conditions:**
- Player is in the overworld
- Night begins (time >= 13000)
- Dawn arrives (time 0-1000) without player sleeping
- Player is alive at dawn
- Debounced: 24 hours (effectively once per world)

**Example:**
```json
{
  "event": "first_night_survived",
  "timestamp": 1737241200,
  "metadata": {}
}
```

**Metadata fields:** None

**State machine:**
1. `IDLE` - Waiting for night to start
2. `NIGHT_STARTED` - Night has begun, tracking sleep status
3. `COMPLETED` - Milestone achieved, no more tracking

**Use cases:**
- Early game milestone recognition
- Survival skill acknowledgment
- "First night" achievement trigger

---

## Rare / Special Events

### `rare_item_found`
Triggered when player picks up a rare or notable item.

**Trigger conditions:**
- Player acquires a tracked rare item in inventory
- Scanned every 10 ticks (0.5 seconds)
- Debounced: 30 seconds per item type

**Example:**
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

**Metadata fields:**
- `item` (string): The specific item ID
- `category` (string): Item category for grouping
- `count` (number): How many were picked up
- `is_very_rare` (boolean): True for exceptionally rare items

**Tracked items:**

| Category | Items | is_very_rare |
|----------|-------|--------------|
| `music_disc` | All 19 music discs | false |
| `totem_of_undying` | Totem of Undying | **true** |
| `trident` | Trident | **true** |
| `enchanted_book` | Enchanted Book | false |
| `elytra` | Elytra | **true** |
| `dragon_egg` | Dragon Egg | **true** |
| `nether_star` | Nether Star | **true** |
| `heart_of_the_sea` | Heart of the Sea | **true** |
| `enchanted_golden_apple` | Enchanted Golden Apple | **true** |
| `dragon_head` | Dragon Head | **true** |
| `beacon` | Beacon | **true** |
| `conduit` | Conduit | false |
| `netherite_ingot` | Netherite Ingot | **true** |
| `netherite_gear` | All netherite tools/armor | false |
| `ancient_debris` | Ancient Debris | false |
| `diamond` | Diamond | false |
| `emerald` | Emerald | false |
| `shulker_shell` | Shulker Shell | false |
| `wither_skeleton_skull` | Wither Skeleton Skull | false |
| `sniffer_egg` | Sniffer Egg | false |
| And more... | | |

**Use cases:**
- `music_disc`: "Music disc! Jukebox time!"
- `totem_of_undying`: "Totem of Undying! That's huge!"
- `trident`: "TRIDENT! That's rare!"
- `enchanted_book`: "Enchanted book! What's it got?"
- `elytra`: "ELYTRA! You can fly now!"

---

### `animal_tamed`
Triggered when player successfully tames an animal.

**Trigger conditions:**
- Player tames a tameable animal
- Debounced: 10 seconds per animal type

**Example:**
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

**Metadata fields:**
- `animal` (string): The animal type
- `trait` (string): What the animal is useful for

**Supported animals:**

| Animal | Trait | Description |
|--------|-------|-------------|
| `wolf` | `companion` | Combat companion |
| `cat` | `utility` | Creeper repellent, brings gifts |
| `parrot` | `companion` | Shoulder buddy, mimics sounds |
| `horse` | `mount` | Fast travel |
| `donkey` | `mount` | Storage mount |
| `mule` | `mount` | Storage mount |
| `llama` | `utility` | Caravan, storage |
| `camel` | `mount` | Two-player mount |
| `axolotl` | `companion` | Aquatic helper |

**Use cases:**
- `wolf`: "Wolf tamed! New friend!"
- `cat`: "Cat tamed! Creeper repellent!"
- `horse`: "Horse tamed! Speed unlocked!"
- `parrot`: "Parrot tamed! Shoulder buddy!"
- `llama`: "Llama tamed! Caravan time!"

---

## Event Rate Limiting

All events are subject to rate limiting to prevent spam:

- **Global debounce:** 250ms minimum between any packets (configurable)
- **Per-event debounce:** Some events have custom debounce periods:
  - `low_health`, `low_hunger`: 10 seconds
  - `drowning`: 5 seconds
  - `hostile_mob_nearby`: 10 seconds
  - Most others: 250ms (config default)

**Configuration:** Edit `config/phasepulse.json` to adjust:
```json
{
  "debounceMs": 250,
  "maxPacketsPerSecond": 5
}
```

---

## Network Behavior

**Connection:**
- Connects to `localhost:32145` (configurable in `config/phasepulse.json`)
- Auto-reconnects on connection failure (if `reconnectOnFailure: true`)
- Non-blocking async network thread (never blocks game)

**Message Format:**
- Each message is a single JSON line followed by newline (`\n`)
- UTF-8 encoding
- Maximum packet size: 2KB

**Example TCP stream:**
```
{"event":"player_sleep","timestamp":1737241188,"metadata":{"world_time":13000}}
{"event":"player_wake","timestamp":1737241189,"metadata":{"world_time":0}}
{"event":"eating","timestamp":1737241187,"metadata":{"item":"minecraft:bread"}}
```

---

## Disabling Events

You can disable categories of events in `config/phasepulse.json`:

```json
{
  "sendPlayerEvents": true,    // health, hunger, drowning, eating, sleep
  "sendCombatEvents": true,     // combat start/end, hostile mobs
  "sendBiomeEvents": true,      // biome discovery
  "sendWeatherEvents": true     // day/night, weather changes
}
```

Set any to `false` to disable that category.

---

## Complete Event List

| Event Name | Category | Metadata Fields |
|------------|----------|-----------------|
| `low_health` | Player | health, maxHealth, healthPercent |
| `low_hunger` | Player | hunger, saturation |
| `drowning` | Player | air, maxAir |
| `item_consumed` | Player | item |
| `player_sleep` | Player | world_time |
| `player_wake` | Player | world_time |
| `sleep_failed` | Player | reason, monsters_nearby |
| `player_hurt` | Player | damage, source |
| `player_death` | Player | cause |
| `harmful_effect` | Player | effect, amplifier, duration_seconds, is_damaging |
| `inventory_organizing` | Player | screen_type, duration_seconds |
| `block_placed` | Player | block |
| `block_broken` | Player | block |
| `item_crafted` | Player | item, count |
| `item_smelted` | Player | item, count |
| `item_enchanted` | Player | level_cost |
| `anvil_used` | Player | result_item |
| `achievement_earned` | Player | achievement |
| `day_start` | World | world_time |
| `night_start` | World | world_time |
| `weather_clear` | World | _(none)_ |
| `weather_rain` | World | _(none)_ |
| `weather_thunder` | World | _(none)_ |
| `biome_discovery` | World | biome |
| `biome_changed` | World | from, to |
| `structure_discovery` | World | structure, is_dangerous |
| `combat_start` | Combat | _(none)_ |
| `combat_end` | Combat | duration_seconds |
| `mob_killed` | Combat | mob_type |
| `hostile_mob_nearby` | Combat | mob_type, count |
| `item_obtained` | Milestone | item |
| `first_night_survived` | Milestone | _(none)_ |
| `rare_item_found` | Rare | item, category, count, is_very_rare |
| `animal_tamed` | Rare | animal, trait |
| `animal_bred` | Rare | animal |
| `villager_trade` | Player | item, count, experience |
| `dimension_changed` | World | from, to |
| `fishing_catch` | Player | item, count |
| `totem_used` | Combat | _(none)_ |
| `potion_brewed` | Player | potion, count |

---

## New Activity Events

### `animal_bred`
Triggered when two animals are successfully bred.

**Trigger conditions:**
- Player feeds breeding items to two animals of the same species and a baby is born
- Debounced: 10 seconds per animal type

**Example:**
```json
{
  "event": "animal_bred",
  "timestamp": 1737241205,
  "metadata": {
    "animal": "cow"
  }
}
```

**Metadata fields:**
- `animal` (string): The animal type bred

---

### `villager_trade`
Triggered when a player completes a trade with a villager.

**Trigger conditions:**
- Player takes the result item from a villager trade interface
- Debounced: 500ms between trades

**Example:**
```json
{
  "event": "villager_trade",
  "timestamp": 1737241210,
  "metadata": {
    "item": "emerald",
    "count": 1,
    "experience": 2
  }
}
```

**Metadata fields:**
- `item` (string): The item received from the trade
- `count` (number): Quantity received
- `experience` (number): Merchant experience gained from the trade

---

### `dimension_changed`
Triggered when the player travels between dimensions.

**Trigger conditions:**
- Player moves between Overworld, Nether, or End
- Debounced: 5 seconds

**Example:**
```json
{
  "event": "dimension_changed",
  "timestamp": 1737241215,
  "metadata": {
    "from": "minecraft:overworld",
    "to": "minecraft:the_nether"
  }
}
```

**Metadata fields:**
- `from` (string): The dimension the player left
- `to` (string): The dimension the player entered

---

### `fishing_catch`
Triggered when a player catches an item through fishing.

**Trigger conditions:**
- Player reels in the fishing rod when a fish is on the line
- Debounced: 1 second

**Example:**
```json
{
  "event": "fishing_catch",
  "timestamp": 1737241220,
  "metadata": {
    "item": "cod",
    "count": 1
  }
}
```

**Metadata fields:**
- `item` (string): The item caught
- `count` (number): Quantity caught

---

### `totem_used`
Triggered when a Totem of Undying is consumed.

**Trigger conditions:**
- Player health reaches 0 while holding a Totem of Undying

**Example:**
```json
{
  "event": "totem_used",
  "timestamp": 1737241225,
  "metadata": {}
}
```

**Metadata fields:** None

---

### `potion_brewed`
Triggered when a player completes brewing a potion.

**Trigger conditions:**
- Player closes a brewing stand interface with finished potions in the output slots
- Debounced: 500ms

**Example:**
```json
{
  "event": "potion_brewed",
  "timestamp": 1737241230,
  "metadata": {
    "potion": "potion",
    "count": 3
  }
}
```

**Metadata fields:**
- `potion` (string): The type of potion brewed
- `count` (number): Quantity brewed

## Testing Events

Use these in-game commands/actions to trigger events for testing:

```bash
# Player events
/effect give @s minecraft:instant_damage 1 10  # Trigger low_health and player_hurt
/effect give @s minecraft:hunger 30 255        # Trigger low_hunger
# Eat bread or any food                        # Trigger item_consumed
# Sleep in bed                                  # Trigger player_sleep/wake
# Take any damage                               # Trigger player_hurt
# Die from any cause                            # Trigger player_death
# Place any block                               # Trigger block_placed
# Break any block                               # Trigger block_broken
# Craft any item                                # Trigger item_crafted
# Smelt iron ore in furnace                    # Trigger item_smelted
# Enchant item at enchanting table             # Trigger item_enchanted
# Rename/repair item in anvil                  # Trigger anvil_used
# Complete advancement criteria                 # Trigger achievement_earned

# Milestone events
# Pick up elytra (creative: /give @s elytra)   # Trigger item_obtained
# Survive night without sleeping               # Trigger first_night_survived

# World events
/time set 13000                                # Trigger night_start
/time set 0                                    # Trigger day_start
/weather rain                                  # Trigger weather_rain
/weather thunder                               # Trigger weather_thunder
/weather clear                                 # Trigger weather_clear
# Walk from one biome to another               # Trigger biome_changed and biome_discovery

# Combat events
# Hit a zombie or take damage                  # Trigger combat_start and player_hurt
# Wait 5 seconds                               # Trigger combat_end
/summon zombie ~ ~ ~                          # Trigger hostile_mob_nearby
# Kill any mob                                  # Trigger mob_killed
```

---

## Debug Logging

Enable verbose packet logging in `config/phasepulse.json`:

```json
{
  "debugLogging": true
}
```

This will log every packet sent to the Minecraft console, useful for development and debugging.
