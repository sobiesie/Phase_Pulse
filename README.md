# Phase_Pulse

A lightweight Fabric mod for Minecraft 1.20.1 that bridges in-game events to the [Phase Pal](https://phasepal.com) desktop companion app. Sends real-time player and world events via local TCP to enable ambient companion reactions.

## Features

- **Player Events**: Health, hunger, drowning, eating, sleeping, death, hurt, crafting, achievements
- **World Events**: Day/night cycle, weather changes, biome discovery
- **Combat Events**: Combat start/end, hostile mob detection, mob kills
- **Chat Commands**: Send messages directly to Phase Pal with `/pal`

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.14.21+
- Fabric API 0.92.2+1.20.1
- Java 17

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download `phase-pulse-x.x.x.jar` from releases
4. Place both JARs in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

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
git clone https://github.com/phasepal/phase-pulse.git
cd phase-pulse

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
