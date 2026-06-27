# CupidBot

CupidBot is a local-first RuneLite client fork with the CupidBot runtime, local legacy-derived plugin support, the official RuneLite Plugin Hub, and Jagex/OSRS login/game connectivity.

## Runtime Boundary

CupidBot core does not call legacy project services, CupidBot cloud services, or GitHub release downloads at runtime. Allowed core networking is limited to:

- Official RuneLite services and Plugin Hub endpoints used by the base client
- Jagex and OSRS login/game/config/world endpoints
- Localhost control surfaces such as the optional Agent Server
- Local CupidBot Hub plugins, which may use networking for their own features

CupidBot Hub plugins are loaded from `~/.runelite/cupidbot-plugins`.

## Build

Use Java 17 for the client:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :client:assemble
```

The launcher jar is written to:

```text
runelite-client/build/libs/cupidbot-2.6.11.jar
```

## Install For CupidBot Launcher

```bash
scripts/install-cupidbot-launcher-jar.sh
```

That copies the built jar to `~/.cupidbot/cupidbot-<version>.jar`, which is the local jar directory used by `../cupidbot-launcher`.

Use the install script instead of copying the jar by hand. It publishes the new
jar atomically so any already-running client keeps reading its old jar until it
is restarted. A raw `cp` over `~/.cupidbot/cupidbot-<version>.jar` can rewrite
the same file inode while Java is still loading classes from it, causing late
`NoClassDefFoundError` crashes.

## Install Local Hub Plugins

Build and install the sibling hub:

```bash
cd ../cupidbot-plugins
JAVA_HOME=/usr/lib/jvm/java-11-openjdk ./gradlew clean build generatePluginsJson copyPluginDocs \
  -PcupidbotClientPath=/home/frank/micro-client-custom/cupidbot/runelite-client/build/libs/cupidbot-2.6.11.jar
scripts/install-cupidbot-local-plugins.sh
```

This installs `plugins.json` and local plugin jars into `~/.runelite/cupidbot-plugins`.

## Local Plugin Status

CupidBot's local plugin support is a work in progress. Many plugins are imported and build locally, but some may still fail to start, stop early because their setup is incomplete, or need more runtime fixes before they are reliable.

If a plugin is broken, open an issue and include the plugin name, what you were trying to do, the steps to reproduce it, and the relevant log lines from `/tmp/cupidbot.log` or the launcher log.

## Common Commands

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :client:compileJava
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :client:assemble
```

## Docs

- Installation: [docs/installation.md](docs/installation.md)
- Architecture: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- Development: [docs/development.md](docs/development.md)
- OSRS update playbook: [docs/osrs-update-playbook.md](docs/osrs-update-playbook.md)
- Runtime CLI: [docs/CUPIDBOT_CLI.md](docs/CUPIDBOT_CLI.md)
- Queryable API: [runelite-client/src/main/java/net/runelite/client/plugins/cupidbot/api/QUERYABLE_API.md](runelite-client/src/main/java/net/runelite/client/plugins/cupidbot/api/QUERYABLE_API.md)
