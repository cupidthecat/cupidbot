# Installation

CupidBot is installed from local builds in this workspace. The launcher does not download client jars, and CupidBot does not download hub plugin jars at runtime. Locally installed hub plugins may still use networking for their own features.

## Requirements

- Java 17 for the CupidBot client
- Java 11 for `cupidbot-plugins`
- Node.js/npm for `cupidbot-launcher`
- A working Jagex account token flow for launcher-based accounts

## Build The Client

```bash
cd /home/frank/micro-client-custom/cupidbot
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :client:assemble
```

Install the jar for the launcher:

```bash
scripts/install-cupidbot-launcher-jar.sh
```

Use the script instead of copying into `~/.cupidbot` manually. It publishes the
jar atomically so a running client can keep its old jar open safely until
restart. Replacing the launcher jar with raw `cp` while CupidBot is running can
cause delayed `NoClassDefFoundError` crashes when Java loads another class from
the overwritten file.

## Build And Install Local Hub Plugins

```bash
cd /home/frank/micro-client-custom/cupidbot-plugins
JAVA_HOME=/usr/lib/jvm/java-11-openjdk ./gradlew clean build generatePluginsJson copyPluginDocs \
  -PcupidbotClientPath=/home/frank/micro-client-custom/cupidbot/runelite-client/build/libs/cupidbot-2.6.11.jar
scripts/install-cupidbot-local-plugins.sh
```

This installs:

- `~/.runelite/cupidbot-plugins/plugins.json`
- `~/.runelite/cupidbot-plugins/<InternalName>.jar`
- copied plugin docs under `~/.runelite/cupidbot-plugins/plugins/` when docs are present

## Run With The Launcher

```bash
cd /home/frank/micro-client-custom/cupidbot-launcher
npm install
npm run dev
```

The launcher reads `~/.cupidbot/cupidbot-<version>.jar`, stores launcher state in `~/.cupidbot`, and uses only the Jagex OAuth/session endpoints for account launch.

## Run The Jar Directly

```bash
java -jar /home/frank/micro-client-custom/cupidbot/runelite-client/build/libs/cupidbot-2.6.11.jar
```

Direct jar launch uses the same local plugin directory at `~/.runelite/cupidbot-plugins`.

## Offline Tips

- Rebuild and rerun `scripts/install-cupidbot-local-plugins.sh` after changing hub plugin source.
- If a plugin shows a hash warning, reinstall the hub plugins so the jar and manifest match.
- If the launcher cannot find a client, rerun `scripts/install-cupidbot-launcher-jar.sh`.
- If Java is not found, install JDK 17 locally and make `java` available on `PATH`.
