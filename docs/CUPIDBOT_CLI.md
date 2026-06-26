# CupidBot CLI

`cupidbot-cli` is a command-line tool that lets AI agents (and developers) interact with a running CupidBot client in real time. It communicates with the embedded Agent Server plugin over HTTP on localhost, providing access to the game's widget system, inventory, NPCs, objects, banking, walking, dialogues, and more.

The CLI is located at the repository root: `./cupidbot-cli`

## Prerequisites

1. The CupidBot client must be running
2. The **Agent Server** plugin must be enabled in the plugin list
3. `curl` must be available on the system (standard on Linux/macOS)
4. A POSIX shell (`bash`) — the CLI is a bash script. On Windows, run it from **WSL** or **Git Bash**. macOS users with the default `/bin/bash` 3.2 are supported (the script avoids Bash 4+ features like namerefs).
5. Auth is token-gated. The plugin writes `~/.runelite/.agent-token`; the CLI reads it automatically.

## Quick Start

```bash
# Check if the server is reachable and the player is logged in
./cupidbot-cli state

# See what's in the inventory
./cupidbot-cli inventory

# Find nearby NPCs
./cupidbot-cli npcs --name Guard --distance 15

# Attack the nearest guard
./cupidbot-cli npcs interact "Guard" "Attack"

# Eat a lobster
./cupidbot-cli inventory interact "Lobster" "Eat"

# Walk somewhere
./cupidbot-cli walk 3222 3218
```

## Configuration

The CLI reads three environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `CUPIDBOT_HOST` | `127.0.0.1` | Agent Server host |
| `CUPIDBOT_PORT` | `8081` | Agent Server port (must match plugin config) |
| `CUPIDBOT_TIMEOUT` | `30` | Request timeout in seconds |
| `CUPIDBOT_TOKEN` | unset | Override the `X-Agent-Token` value |
| `CUPIDBOT_TOKEN_FILE` | `~/.runelite/.agent-token` | Token file path |

Example with custom port:

```bash
CUPIDBOT_PORT=9090 ./cupidbot-cli state
```

## Command Reference

### Game State

```bash
# Full player state: health, position, animation, combat level
./cupidbot-cli state

# All skill levels, boosted levels, and XP
./cupidbot-cli skills

# Single skill
./cupidbot-cli skills --name Attack
```

### Widget Inspection

Widgets are the game's UI elements (interfaces, buttons, toggles, text). Use these commands to explore and interact with any game interface at runtime.

```bash
# List all currently visible widget interfaces
./cupidbot-cli widgets list
./cupidbot-cli widgets list --limit 50 --offset 10

# Search for widgets by keywords (results ranked by relevance)
./cupidbot-cli widgets search "notification,level-up,pop-up"
./cupidbot-cli widgets search "bank" --limit 5

# Describe the widget tree under a specific widget
./cupidbot-cli widgets describe 134 0
./cupidbot-cli widgets describe 134 0 --depth 3

# Click a widget by group/child ID
./cupidbot-cli widgets click 134 42

# Click a widget by text content
./cupidbot-cli widgets click --text "Toggle"
```

**Typical widget workflow:**

1. Search for the widget you need: `widgets search "notifications"`
2. Drill into the match: `widgets describe <groupId> <childId> --depth 3`
3. Find the interactable child and click it: `widgets click <groupId> <childId>`

### Inventory

```bash
# List all items with id, name, quantity, slot, actions
./cupidbot-cli inventory

# Use/eat/equip an item
./cupidbot-cli inventory interact "Lobster" "Eat"
./cupidbot-cli inventory interact "Bronze sword" "Wield"

# Drop a single item
./cupidbot-cli inventory drop "Logs"

# Drop all of an item
./cupidbot-cli inventory drop "Logs" --all
```

### NPCs

```bash
# Query nearby NPCs (all within 20 tiles by default)
./cupidbot-cli npcs

# Filter by name and distance
./cupidbot-cli npcs --name "Banker" --distance 10
./cupidbot-cli npcs --name "Guard" --distance 15 --limit 5

# Interact with the nearest matching NPC
./cupidbot-cli npcs interact "Banker" "Bank"
./cupidbot-cli npcs interact "Guard" "Attack"
```

### Objects

Game objects include trees, rocks, doors, bank booths, furnaces, and all other world objects.

```bash
# Query nearby objects
./cupidbot-cli objects
./cupidbot-cli objects --name "Oak tree" --distance 10

# Interact with the nearest matching object
./cupidbot-cli objects interact "Oak tree" "Chop down"
./cupidbot-cli objects interact "Bank booth" "Bank"
```

### Ground Items

```bash
# See what's on the ground nearby
./cupidbot-cli ground-items
./cupidbot-cli ground-items --name "Dragon bones" --distance 15

# Pick up the nearest matching item
./cupidbot-cli ground-items pickup "Dragon bones"
./cupidbot-cli ground-items pickup "Coins"
```

### Walking

```bash
# Walk to coordinates (x, y, optional plane)
./cupidbot-cli walk 3222 3218
./cupidbot-cli walk 3222 3218 1    # plane 1 (upstairs)
```

The walk command is **non-blocking by default** — it starts the walk and returns immediately. Add `--wait` to block until the player arrives (or the timeout elapses, default 30s). The response includes both the destination and the player's final position.

```bash
# Blocking walk — waits until arrival or 60s timeout
./cupidbot-cli walk 3222 3218 --wait --timeout 60
```

### Banking

```bash
# Check if the bank is open (includes item list when open)
./cupidbot-cli bank

# Open the nearest bank (waits up to 5 seconds)
./cupidbot-cli bank open

# Close the bank
./cupidbot-cli bank close

# Deposit all items in inventory
./cupidbot-cli bank deposit-all

# Deposit a specific item
./cupidbot-cli bank deposit "Logs"

# Withdraw items (name + quantity)
./cupidbot-cli bank withdraw "Pure essence" 28
./cupidbot-cli bank withdraw "Coins" 1000
```

### Dialogue

```bash
# Check current dialogue state (options, text, continue button)
./cupidbot-cli dialogue

# Click the continue button
./cupidbot-cli dialogue continue

# Select a dialogue option by text
./cupidbot-cli dialogue select "Buy sword"
./cupidbot-cli dialogue select "Yes"
```

### Login

```bash
# Check login status (game state, profile, world, error detection)
./cupidbot-cli login

# Trigger login using active profile
./cupidbot-cli login now

# Login to a specific world
./cupidbot-cli login now --world 360

# Login and wait until logged in (blocks until success or timeout)
./cupidbot-cli login wait --timeout 60
./cupidbot-cli login wait --timeout 60 --world 450
```

When on the login screen, the status response includes `loginIndex` and `loginError` for detecting issues like non-member accounts on members worlds (`loginIndex: 34`), banned accounts (`loginIndex: 14`), or invalid credentials (`loginIndex: 4`).

### Profiles

```bash
# List all profiles (shows name, member status, world selection, active indicator)
./cupidbot-cli profile

# Switch the active profile (used by the next login)
./cupidbot-cli profile switch "player@email.com"
```

After switching, `./cupidbot-cli login now` will use the newly selected profile. Profile switching is safe while logged in — the current session is unaffected; only the next login uses the new profile.

### Client-Thread Lookup (offline)

Look up whether a specific RuneLite API method needs to be called on the client thread. This subcommand is **fully offline** — it does not require the game client or Agent Server to be running. It reads `docs/client-thread-lookup.tsv`, which is regenerated by `./gradlew :client:runClientThreadScanner`.

```bash
# Exact match (default) — single method by name
./cupidbot-cli ct getItems

# Class.method shorthand auto-splits into class filter + method name
./cupidbot-cli ct Player.getName

# Substring matching — useful for exploring an API surface
./cupidbot-cli ct getItem --like

# Restrict matches to a specific class (substring)
./cupidbot-cli ct getName --class Player

# Filter by evidence kind (only show methods with the strongest signal)
./cupidbot-cli ct getName --evidence ASSERT

# Machine-readable output for tooling / scripts
./cupidbot-cli ct getDynamicChildren --json
```

Each match prints the fully-qualified method, the evidence types backing the verdict, and a confidence rating:

```
net.runelite.api.ItemContainer.getItems(): Item[]
  Evidence: ASSERT,LAMBDA,SUBSCRIBE
  Callers : 20
  Verdict : REQUIRED (asserted)
```

**Evidence kinds:**

| Tag | Meaning |
|---|---|
| `ASSERT` | A method in this repo wraps the call with `assert client.isClientThread()` (highest confidence — throws `AssertionError` off-thread under `-ea`) |
| `SUBSCRIBE` | A `@Subscribe`-annotated event handler calls this method; RuneLite's event bus dispatches handlers on the client thread |
| `LAMBDA` | A lambda body that was passed to `clientThread.invoke*()` calls this method, transitively |

**Verdicts:**

- **REQUIRED (asserted)** — has `ASSERT` evidence; calling off-thread will fail loudly under assertions
- **Strongly indicated (N signals)** — two or more independent evidence types
- **Likely (single signal)** — only one evidence type; treat as needing the client thread but with lower certainty

**Caveat:** absence from the lookup file does **not** prove a method is safe off-thread. Many RuneLite API methods are silently unsafe with no surrounding wrapper anywhere in this repo, so the scanner cannot detect them. When in doubt, marshal via `CupidBot.getClientThread().runOnClientThreadOptional(...)`.

A typical workflow: a contributor unsure whether their script utility needs to wrap a call in `clientThread.invoke()` runs `./cupidbot-cli ct <methodName>` and gets an answer in ~10 ms without booting the client. The full inferred-API table lives in `docs/client-thread-manifest.md` if you prefer browsing.

### Script Lifecycle

```bash
# List all cupidbot plugins with active/enabled status
./cupidbot-cli scripts

# Start a plugin by class name
./cupidbot-cli scripts start --class "net.runelite.client.plugins.cupidbot.example.ExamplePlugin"

# Start a plugin by display name
./cupidbot-cli scripts start --name "Example"

# Stop a plugin
./cupidbot-cli scripts stop --class "net.runelite.client.plugins.cupidbot.example.ExamplePlugin"

# Check plugin status (runtime, state)
./cupidbot-cli scripts status --class "net.runelite.client.plugins.cupidbot.example.ExamplePlugin"

# Submit test results
./cupidbot-cli scripts results post --class "com.hub.MyPlugin" --data '{"passed":true,"kills":10}'

# Retrieve test results
./cupidbot-cli scripts results --class "com.hub.MyPlugin"
```

These endpoints are designed for the CupidBot-Hub automated testing loop: the Hub spawns a client, logs in, starts a script, polls status, and collects results. See `docs/AGENT_SERVER.md` for the full HTTP API.

## Response Format

All responses are JSON. Successful queries return structured data:

```json
{
  "count": 3,
  "total": 3,
  "npcs": [
    {"id": 3010, "name": "Guard", "combatLevel": 21, "distance": 5, "position": {"x": 3215, "y": 3219, "plane": 0}}
  ]
}
```

Action responses indicate success/failure with a reason:

```json
{"success": true, "action": "Attack", "npc": {"name": "Guard", "id": 3010}}
```

```json
{"success": false, "reason": "NPC not found"}
```

Connection errors output:

```json
{"error": "Connection refused - is the Agent Server plugin enabled?"}
```

## Agent Workflow Examples

### Example: Mine copper ore and bank it

```bash
# 1. Check current state
./cupidbot-cli state

# 2. Walk to mining area
./cupidbot-cli walk 3285 3365

# 3. Find copper rocks
./cupidbot-cli objects --name "Copper rocks" --distance 10

# 4. Mine
./cupidbot-cli objects interact "Copper rocks" "Mine"

# 5. Check if inventory is full
./cupidbot-cli inventory

# 6. Walk to bank
./cupidbot-cli walk 3269 3167

# 7. Open bank and deposit
./cupidbot-cli bank open
./cupidbot-cli bank deposit-all

# 8. Close bank and repeat
./cupidbot-cli bank close
```

### Example: Navigate a settings interface

```bash
# 1. Search for the settings widget
./cupidbot-cli widgets search "settings"

# 2. Click to open settings
./cupidbot-cli widgets click --text "Settings"

# 3. Search for the specific toggle
./cupidbot-cli widgets search "notification,pop-up"

# 4. Describe the widget tree around the match
./cupidbot-cli widgets describe 134 42 --depth 2

# 5. Click the toggle
./cupidbot-cli widgets click 134 42
```

### Example: Talk to an NPC

```bash
# 1. Find the NPC
./cupidbot-cli npcs --name "Shop keeper"

# 2. Talk to them
./cupidbot-cli npcs interact "Shop keeper" "Talk-to"

# 3. Read the dialogue
./cupidbot-cli dialogue

# 4. Click through dialogue
./cupidbot-cli dialogue continue

# 5. Select an option
./cupidbot-cli dialogue select "What do you have for sale?"
```

## Extending the CLI

The CLI is a bash wrapper around `curl` calls to the Agent Server HTTP API. New commands map 1:1 to server endpoints. To add a new command:

1. Add a handler in `agentserver/handler/` (extend `AgentHandler`)
2. Register it in `AgentServerPlugin.startUp()`
3. Add a `case` block in `cupidbot-cli`

See `docs/AGENT_SERVER.md` for the full HTTP API reference, handler architecture, and server internals.
