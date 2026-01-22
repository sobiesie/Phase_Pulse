# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Phase_Pulse** is a lightweight Fabric mod for Minecraft 1.21.1 that bridges in-game events to the Phase Pal desktop companion app. The mod sends real-time player and world events via local TCP communication to enable ambient companionship reactions.

**Key Constraints:**
- Local-only communication (localhost/LAN, never cloud)
- Event-driven, stateless architecture
- Low performance overhead (<0.5% CPU)
- No gameplay modification or cheating features
- Client-side only, no server integration

## Development Commands

### Build and Run
```bash
# Build the mod
./gradlew build

# Run Minecraft client with the mod loaded (for testing)
./gradlew runClient

# Run data generation
./gradlew runDatagen

# Generate IDE configurations
./gradlew eclipse  # For Eclipse
./gradlew idea     # For IntelliJ IDEA

# Clean build artifacts
./gradlew clean
```

### Testing
```bash
# Run all tests
./gradlew test

# Run the mod in development environment
./gradlew runClient
```

### Publishing
```bash
# Build the distributable JAR
./gradlew build

# The output JAR will be in: build/libs/phase-pulse-1.0.0.jar
```

## Architecture

### Entry Points

The mod has three distinct entry points defined in `fabric.mod.json`:

1. **Main (Server/Common)**: `PhasePulse.java` - Initializes common/server-side logic
2. **Client**: `PhasePulseClient.java` - Initializes client-specific features
3. **Data Generation**: `PhasePulseDataGenerator.java` - Generates mod data (recipes, loot tables, etc.)

All event detection and communication should happen client-side since this is a client-focused companion mod.

### Package Structure

- `com.phasepal.phasepulse` - Main mod classes and entry points
- `com.phasepal.phasepulse.mixin` - Mixin injections for hooking into Minecraft internals

### Event System Design

The mod needs to implement event hooks for:

**Player State Events:**
- Health/hunger monitoring (low thresholds)
- Drowning detection (air bubbles)
- Eating, sleeping, waking actions

**World/Environment Events:**
- Day/night transitions
- Weather changes
- Biome discovery

**Combat Events:**
- Combat start/end detection
- Nearby hostile mob detection

**Implementation approach:**
- Use Fabric API event callbacks where available
- Use Mixins only when necessary (for events not exposed by Fabric API)
- All events should trigger state changes, not tick-based polling
- Debounce events to prevent spam (max 1 packet per 200-300ms)

### Communication Layer

**Network Architecture:**
- Local TCP connection to `localhost:32145` (configurable)
- Async sender thread (never block game thread)
- JSON message format with schema versioning
- Rate limiting: max 1 packet per 200-300ms

**Message Schema:**
```json
{
  "event": "event_name",
  "timestamp": 1234567890,
  "metadata": {
    "key": "value"
  }
}
```

### Configuration

Config file location: `config/phasepulse.json`

Expected structure:
```json
{
  "enabled": true,
  "port": 32145,
  "sendCombatEvents": true,
  "sendBiomeEvents": true,
  "sendRandomEvents": true
}
```

## Fabric-Specific Guidelines

### Using Mixins

- Keep mixins minimal and focused
- Document what vanilla method you're injecting into
- Use `@Inject` at appropriate injection points (HEAD, TAIL, RETURN)
- Mixin classes go in `com.phasepal.phasepulse.mixin`
- Register all mixins in `phase-pulse.mixins.json`

### Using Fabric API

Prefer Fabric API events over mixins when available:
- `ServerTickEvents` for server/world tick monitoring
- `ClientTickEvents` for client-side tick monitoring
- `EntityEvents` for entity-related events
- `PlayerBlockBreakEvents` for block interaction
- Use event registration in the appropriate initializer (client vs common)

## Technical Requirements

- **Java Version**: 21
- **Minecraft Version**: 1.21.1
- **Yarn Mappings**: 1.21.1+build.3
- **Fabric Loader**: 0.16.5+
- **Fabric API**: 0.102.0+1.21.1
- **Loom Version**: 1.7-SNAPSHOT

## Performance Considerations

- No blocking network calls in game thread
- Use async/separate thread for all TCP communication
- No world scanning beyond necessary radius checks
- Zero allocations in main tick loop
- Debounce all events to prevent packet spam
- Maximum packet size: 2KB

## Project Roadmap (from PRD.md)

**v1.0 Features:**
- Player state events (health, hunger, drowning, eating, sleeping)
- Weather and day/night cycle events
- Combat start/end detection
- Biome discovery
- JSON TCP socket communication
- Basic configuration file

**Future Considerations:**
- Random flavor events (rate-limited personality additions)
- UDP fallback transport
- Packet compression
- Forge port (v2.0, only if high demand)
