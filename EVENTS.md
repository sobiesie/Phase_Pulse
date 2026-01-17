# Phase_Pulse Event Documentation

This document describes all events sent by the Phase_Pulse mod to the Phase Pal companion app via TCP socket on `localhost:32145`.

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

### `eating`
Triggered when player consumes food or drinks a potion.

**Trigger conditions:**
- Player finishes eating/drinking an item
- Debounced: 250ms between eating events

**Example:**
```json
{
  "event": "eating",
  "timestamp": 1737241187,
  "metadata": {
    "item": "minecraft:bread"
  }
}
```

**Metadata fields:**
- `item` (string): The item consumed (item ID format)

**Common items:**
- `minecraft:bread`
- `minecraft:cooked_beef`
- `minecraft:golden_apple`
- `minecraft:potion`
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
| `eating` | Player | item |
| `player_sleep` | Player | world_time |
| `player_wake` | Player | world_time |
| `day_start` | World | world_time |
| `night_start` | World | world_time |
| `weather_clear` | World | _(none)_ |
| `weather_rain` | World | _(none)_ |
| `weather_thunder` | World | _(none)_ |
| `biome_discovery` | World | biome |
| `combat_start` | Combat | _(none)_ |
| `combat_end` | Combat | duration_seconds |
| `hostile_mob_nearby` | Combat | mob_type, count |

---

## Testing Events

Use these in-game commands/actions to trigger events for testing:

```bash
# Player events
/effect give @s minecraft:instant_damage 1 10  # Trigger low_health
/effect give @s minecraft:hunger 30 255        # Trigger low_hunger
# Eat bread or any food                        # Trigger eating
# Sleep in bed                                  # Trigger player_sleep/wake

# World events
/time set 13000                                # Trigger night_start
/time set 0                                    # Trigger day_start
/weather rain                                  # Trigger weather_rain
/weather thunder                               # Trigger weather_thunder
/weather clear                                 # Trigger weather_clear

# Combat events
# Hit a zombie or take damage                  # Trigger combat_start
# Wait 5 seconds                               # Trigger combat_end
/summon zombie ~ ~ ~                          # Trigger hostile_mob_nearby
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
