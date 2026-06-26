# CupidBot

RuneLite fork with a hidden always-on plugin hosting automation scripts. Composite Gradle build, Java 11 target (JDK 17+ to develop).

## Build / validate
- Compile: `./gradlew :client:compileJava`
- Full: `./gradlew buildAll`
- Shaded jar: `./gradlew :client:assemble`
- Tests (opt-in): `:client:runUnitTests`, `:client:runTests`, `:client:runIntegrationTest` (needs running game)

## Non-negotiable rules
- Never instantiate caches/queryables directly — use `CupidBot.getRs2XxxCache().query()` / `.getStream()`. See `runelite-client/src/main/java/net/runelite/client/plugins/cupidbot/api/QUERYABLE_API.md`.
- Never block/sleep on the client thread.
- Never use static sleeps to wait for game state — use `sleepUntil(condition, timeoutMs)`.
- Keep `CupidBotPlugin` hidden/always-on; don't break its config panel wiring.
- Respect existing Checkstyle/Lombok patterns; don't weaken security (telemetry tokens, HTTP clients).
- Minimal logging; no PII or session identifiers.

## Review priority
- **P0:** client crashes, client-thread blocking, login/world-hop breakage, cache invariant corruption, credential/token exposure.
- **P1:** script loop timing, overlay correctness, plugin discovery/config, shaded-jar packaging, build reproducibility.

## Runtime tooling
- `./cupidbot-cli` (JSON output) — see `docs/CUPIDBOT_CLI.md`, HTTP API `docs/AGENT_SERVER.md`, full tool list `docs/AGENT_SCRIPT_TOOLS.md`.
- Agent Server plugin runs on port 8081 by default.
- Offline client-thread lookup: `./cupidbot-cli ct <method>`.
- Test mode: `-Dcupidbot.test.mode=true -Dcupidbot.test.script=<PluginName>` → results in `~/.runelite/test-results/`. Protocol: `docs/AGENTIC_TESTING_LOOP.md`.

## In-game settings
Use the settings search bar — tab indices shift on updates. Verify changes via `./cupidbot-cli varbit <id>`.

## Before touching `cupidbot/util/`
Read `docs/entity-guides/README.md`. Add a gotcha there when you fix an entity-assumption bug.

## Docs maintenance
- Keep root `README.md`, `docs/README.md`, and `docs/INDEX.md` short routing pages.
- Put volatile command details, API examples, endpoint lists, generated inventories, and screenshots in the narrowest owning doc.
- Prefer links to owner docs over copying the same guidance into multiple high-level files.

## Deeper guides
- Script authoring & threading: `runelite-client/.../cupidbot/AGENTS.md`
- State machines (use for 3+ phase scripts): `.../cupidbot/statemachine/AGENTS.md`
- Architecture: `docs/ARCHITECTURE.md`, `docs/decisions/`
- Setup: `docs/development.md`, `docs/installation.md`
