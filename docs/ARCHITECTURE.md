# Architecture

## Components

- **RuneLite base client**: Preserves the normal RuneLite startup, Jagex/OSRS config loading, world list access, and official RuneLite Plugin Hub support.
- **CupidBot runtime**: The renamed local runtime under `net.runelite.client.plugins.cupidbot`. It exposes script lifecycle helpers, queryable caches, and utility facades.
- **Local CupidBot Hub loader**: Reads `~/.runelite/cupidbot-plugins/plugins.json` and loads matching local jars from the same directory. It does not download plugin jars.
- **CupidBot Hub source repo**: The sibling `../cupidbot-plugins` checkout builds all local plugin jars and produces the local manifest.
- **CupidBot Launcher**: The sibling Electron launcher reads local jars from `~/.cupidbot` and uses Jagex OAuth/session endpoints for account launch.

## Network Policy

CupidBot removes legacy project and CupidBot service networking from the core client and loader. Core runtime networking is limited to:

- Official RuneLite endpoints used by the base client and official Plugin Hub
- Jagex/OSRS login, account, config, world, and game endpoints
- Localhost control APIs such as the Agent Server
- Local CupidBot Hub plugins, which may use networking for their own plugin features

The local hub loader skips release lookup, jar download, install telemetry, and update telemetry. Plugin jars are trusted only when their local SHA-256 hash matches the local manifest.

## Data Flow

1. The launcher selects a local `~/.cupidbot/cupidbot-<version>.jar`.
2. CupidBot starts and loads normal RuneLite/Jagex runtime data.
3. The local hub loader reads `~/.runelite/cupidbot-plugins/plugins.json`.
4. Enabled local plugins are loaded from `~/.runelite/cupidbot-plugins/<InternalName>.jar`.
5. Optional Agent Server traffic stays on localhost unless explicitly configured otherwise.

## Build Outputs

- Client jar: `runelite-client/build/libs/cupidbot-<version>.jar`
- Hub manifest: `../cupidbot-plugins/public/docs/plugins.json`
- Hub plugin jars: `../cupidbot-plugins/build/libs/<InternalName>-<version>.jar`
- Launcher jar directory: `~/.cupidbot`
- Local plugin directory: `~/.runelite/cupidbot-plugins`
