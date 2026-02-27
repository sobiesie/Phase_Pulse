Below is a **clean, professional, product-ready PRD** for your Minecraft → Phase Pal communication mod, **Phase_Pulse**, written at the quality level you'd hand to engineers or collaborators.

It includes:

* Product Overview
* Goals & Non-Goals
* Core Features
* Event Schema
* Architecture
* Performance Constraints
* Security Model
* Test Plan
* Roadmap

Everything is scoped tightly to **Fabric-first**, lightweight, low-maintenance communication.

---

# **PRD — Phase_Pulse (Minecraft → Phase Pal Bridge Mod)**

## **1. Product Name**

**Phase_Pulse**
*A lightweight, Fabric-based Minecraft bridge mod that sends in-game signals to the local Phase Pal desktop companion.*

---

# **2. Product Summary**

Phase_Pulse is a minimal, always-safe, always-local Minecraft Fabric mod that sends real-time player and world events to the Phase Pal desktop app running on the same computer or LAN.

It enables Phase Pal to:

* Understand key gameplay moments
* React emotionally (not instructively)
* Provide ambient companionship
* Offer context-aware comments (e.g., “Good night, sleep tight!” when the player sleeps)

All communication is:

* Local-only
* Stateless
* Event-driven
* Low overhead
* Opt-in

---

# **3. Goals**

### **3.1 Primary Goals**

1. **Deliver real-time game context to Phase Pal**

    * Player state
    * Combat signals
    * Exploration + movement cues
    * Environmental changes

2. **Enable “ambient reactions”**
   Phase Pal should feel naturally aware, not like a backseat gamer.

3. **Maintain extremely low performance cost**
   No tick lag, no packet spam, no rendering hooks.

4. **Simple installation, minimal configuration**
   Drop-in Fabric mod; no server config required.

---

# **4. Non-Goals**

These are **explicitly out of scope** for v1:

* No cheating, automation, or gameplay enhancement
* No reading or modifying inventories
* No world modification
* No modpack integration logic
* No deep RPC or bidirectional behavior
* No UI inside Minecraft (minimal config only)
* No cloud communication, ever (local only)
* No long continuous data streaming (only events)

---

# **5. Core Features & Event List**

## **5.1 Player State Events**

| Event          | Description             | Example Phase Pal Reaction     |
| -------------- | ----------------------- | ------------------------------ |
| **Low Health** | HP < 30%                | “Careful—your health is low.”  |
| **Low Hunger** | Hunger < 3 bars         | “You should grab a bite soon!” |
| **Drowning**   | Air bubbles < threshold | “Get to the surface!”          |
| **Eating**     | Player consumes item    | “Yum! That should help.”       |
| **Sleeping**   | Player enters bed       | “Good night, sleep tight!”     |
| **Waking Up**  | Player exits bed        | “Morning! Let’s keep going.”   |

---

## **5.2 World & Environment Events**

| Event               | Description              | Reaction                  |
| ------------------- | ------------------------ | ------------------------- |
| **Day Start**       | Day 0 → Day 1 transition | “Another beautiful day!”  |
| **Night Start**     | Sunset → Night           | “Night falls… stay safe.” |
| **Weather Change**  | Rain, thunder, clear     | “Looks like rain.”        |
| **Biome Discovery** | Player enters new biome  | “Ooh, new biome!”         |

---

## **5.3 Combat & Danger Events**

| Event                  | Description               | Reaction                      |
| ---------------------- | ------------------------- | ----------------------------- |
| **Combat Start**       | Player hit or hits entity | “You’ve got company!”         |
| **Combat End**         | No damage for X seconds   | “All clear.”                  |
| **Nearby Hostile Mob** | Mob within radius         | “Something’s lurking nearby…” |

---

## **5.4 Exploration & Movement Events**

| Event                | Description                                  |
| -------------------- | -------------------------------------------- |
| **Player Falls**     | High fall/damage warning                     |
| **Enters Structure** | Villages, temples, etc. (if accessible)      |
| **Depth Change**     | Going underground → “Heading into the deep…” |

---

## **5.5 Random Flavor Events**

Triggered infrequently to add personality:

* “You’ve been mining for a while—still doing okay?”
* “That was a nice view.”
* “This feels cozy.”

**Rate limit:** max once per 7–12 minutes.

---

# **6. Communication Architecture**

## **6.1 Transport**

* **Local TCP** (primary) on `localhost:32145`
* **UDP fallback** (optional future)

## **6.2 Message Format**

Lightweight JSON packets, e.g.:

```json
{
  "event": "player_sleep",
  "timestamp": 1737241184,
  "metadata": {
    "player": "Ekene",
    "world_time": 13000
  }
}
```

### Sending rules:

* Only send on **state change**, not per tick
* Debounce events where needed (combat, mobs)
* Packet size < 2 KB
* Send at most 1 packet per 200–300ms

---

# **7. Performance Requirements**

* CPU usage: **<0.5% on mid-tier hardware**
* No tick-blocking calls
* All networking done on a dedicated async thread
* Zero allocations during the main tick loop
* No world scanning beyond radius checks
* Compatible with large modpacks (lightweight hooks)

---

# **8. Security Model**

### **Strong requirements:**

* **Only communicates to localhost or LAN**
* No external IP connections
* No data persistence or logging by default
* No sensitive info sent (only events, never coordinates unless needed)

### **Optional toggles**

* “Send biome names” = ON/OFF
* “Send hostile mob alerts” = ON/OFF

---

# **9. Config File**

Located at:

```
config/phasepulse.json
```

Example:

```json
{
  "enabled": true,
  "port": 32145,
  "sendCombatEvents": true,
  "sendBiomeEvents": true,
  "sendRandomEvents": true
}
```

---

# **10. Test Plan**

## Unit tests

* Event triggers fire correctly (sleeping, eating, weather)
* Debounce logic prevents spam
* JSON packets validate schema

## Integration tests

* Fabric mod running with Phase Pal connected
* Multiple events firing in sequence
* Test mod in:

    * Singleplayer
    * Multiplayer (client-side only)

## Performance tests

* Long play sessions (1–3 hours)
* Heavy combat areas
* Large modpacks

---

# **11. Release Plan**

### **v1.0 (Fabric-only)**

* Player state events
* Weather + day/night
* Combat start/end
* Biome discovery
* Sleeping/eating/drowning
* JSON socket communication
* Basic config

### **v1.1**

* Random flavor events tuning
* Add debouncing for hostile mob detection
* Packet compression (optional)

### **v2.0 (Forge port)**

Only if demand is high.

---

# **12. Example Event-to-Phrase Mapping**

To ensure Phase Pal’s default personality feels safe & ambient:

| Event           | Example line                            |
| --------------- | --------------------------------------- |
| Sleeping        | “Good night, sleep tight!”              |
| Waking          | “Morning! Ready for another adventure?” |
| Biome discovery | “Hey, this place looks new!”            |
| Low health      | “Careful… you’re looking hurt.”         |
| Combat start    | “Heads up!”                             |
| Weather change  | “Looks like rain’s coming in.”          |
| Eating          | “That should help.”                     |

Phase Pal’s model will embellish these naturally.

---

# **13. Engineering Implementation Priority**

1. Event hooks (Fabric API)
2. Async network sender
3. JSON schema + version header
4. Player state detection
5. World/weather hooks
6. Combat + danger signals
7. Biome detector
8. Random event engine
9. Config file
10. QA + profiling

---

