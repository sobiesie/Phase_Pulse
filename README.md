# Phase_Pulse

A lightweight NeoForge mod for Minecraft 1.21.11 that bridges in-game events to the [Phase Pal](https://phasepal.com) desktop companion app. Sends real-time player and world events via local TCP to enable ambient companion reactions.

## Features

- **Player Events**: Health, hunger, drowning, eating, sleeping, death, hurt, crafting, achievements
- **World Events**: Day/night cycle, weather changes, biome discovery
- **Combat Events**: Combat start/end, hostile mob detection, mob kills
- **Chat Commands**: Send messages directly to Phase Pal with `/pal`

## Requirements

- Minecraft 1.21.11
- NeoForge 21.11.37-beta+
- Java 21

## Installation

1. Install [NeoForge](https://neoforged.net/)
2. Download `phase-pulse-x.x.x.jar` from releases
3. Place the JAR in your `.minecraft/mods` folder
4. Launch Minecraft with the NeoForge profile

## Commands

### `/pal <message>`
Send a message to Phase Pal.

```
/pal Hello there!
```

### `/pal speak <message>`
Send a message for Phase Pal to speak aloud (text-to-speech).

```
/pal speak Good morning!
```

### `/pal`
Show command help.

**Security Features:**
- Input sanitization (removes control characters)
- Rate limiting (configurable cooldown, default 1 second)
- Message length limit (configurable, default 500 characters)
- Can be disabled via config

## Configuration

Config file: `config/phasepulse.json`

```json
{
  "enabled": true,
  "host": "localhost",
  "port": 32145,
  "debounceMs": 250,
  "sendPlayerEvents": true,
  "sendCombatEvents": true,
  "sendBiomeEvents": true,
  "sendWeatherEvents": true,
  "sendChatMessages": true,
  "chatCommandCooldownMs": 1000,
  "maxChatMessageLength": 500,
  "maxPacketsPerSecond": 5,
  "connectionTimeoutMs": 5000,
  "reconnectOnFailure": true,
  "debugLogging": false
}
```

| Option | Description | Default |
|--------|-------------|---------|
| `enabled` | Enable/disable the mod | `true` |
| `host` | Target host (localhost only for security) | `localhost` |
| `port` | TCP port for Phase Pal connection | `32145` |
| `debounceMs` | Minimum ms between events | `250` |
| `sendPlayerEvents` | Send health, hunger, sleep events | `true` |
| `sendCombatEvents` | Send combat start/end, mob events | `true` |
| `sendBiomeEvents` | Send biome discovery events | `true` |
| `sendWeatherEvents` | Send day/night, weather events | `true` |
| `sendChatMessages` | Enable `/pal` command | `true` |
| `chatCommandCooldownMs` | Cooldown between `/pal` commands | `1000` |
| `maxChatMessageLength` | Max characters per message | `500` |
| `debugLogging` | Log all packets to console | `false` |

## Events

See [EVENTS.md](EVENTS.md) for complete event documentation including:
- All event types and their metadata
- JSON message format
- Rate limiting details
- Testing commands

## Building from Source

```bash
# Clone the repository
git clone https://github.com/sobiesie/Phase_Pulse.git
cd Phase_Pulse

# Build the mod
./gradlew build

# Output JAR: build/libs/phase-pulse-x.x.x.jar

# Run in development
./gradlew runClient
```

## Network Protocol

- **Transport**: TCP to `localhost:32145`
- **Format**: JSON lines (one JSON object per line, newline-delimited)
- **Encoding**: UTF-8
- **Max packet size**: 2KB

Example message:
```json
{"event":"user_chat","timestamp":1737241200,"metadata":{"text":"Hello!","speak":false}}
```

## Privacy & Security

- **Local-only**: Only connects to localhost/127.0.0.1
- **No telemetry**: No data sent to external servers
- **No gameplay changes**: Pure event observer, no cheating features
- **Client-side only**: No server integration required

## License

MIT License

---

© 2026 Consistency AI LLC
