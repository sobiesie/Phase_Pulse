# Repository Guidelines

## Project Structure & Module Organization
This repository is a mod for Minecraft 1.21.11 using Java 21 and Gradle.

- Main code: `src/main/java/com/phasepal/phasepulse/`
- Event logic: `src/main/java/com/phasepal/phasepulse/event/` (subpackages: `player`, `combat`, `world`, `milestone`)
- Networking/config/commands: `network/`, `config/`, `command/`
- Documentation: `README.md`, `EVENTS.md`, `CHANGELOG.md`

## Build, Test, and Development Commands
Use the Gradle wrapper from repo root:

- `./gradlew build` (Windows: `.\gradlew.bat build`): compile, run checks, and produce JARs in `build/libs/`
- `./gradlew runClient`: launch a local Fabric dev client for manual testing
- `./gradlew clean`: remove build outputs when troubleshooting stale artifacts

CI (`.github/workflows/build.yml`) runs `./gradlew build` on pushes and pull requests with JDK 21.

## Coding Style & Naming Conventions
- Java version: 21 (`sourceCompatibility`/`targetCompatibility` set in `build.gradle`)
- Follow existing style in this repo: tab-indented Java files, concise methods, and clear package boundaries
- Class names: `PascalCase` (e.g., `CombatTracker`)
- Methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`
- Keep event names and payload semantics aligned with `EVENTS.md`
- Put new logic in the nearest existing package rather than creating broad utility buckets

## Testing Guidelines
There is currently no `src/test` suite. Validate changes by:

- Running `./gradlew build` before opening a PR
- Running `./gradlew runClient` and exercising impacted flows (e.g., `/pal`, combat/world/player events)
- Verifying emitted event payloads and debounce behavior against `EVENTS.md`

If you add automated tests later, place them under `src/test/java` with `*Test` suffix.

## Commit & Pull Request Guidelines
- Use short, imperative commit subjects, matching project history (`Add ...`, `Fix ...`, `Update ...`, `Prepare ...`)
- Keep commits focused; avoid mixing refactors with behavior changes
- PRs should include: what changed, why, how it was tested, and any event/protocol impact
- Link related issues; include logs or screenshots when UI/client behavior changes are visible
