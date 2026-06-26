# OSRS Update Playbook

Use this when an OSRS update requires refreshing CupidBot, the local plugin
repository, or the launcher-installed client jar.

## Scope

CupidBot has three separate update surfaces:

- `cupidbot`: the RuneLite fork, CupidBot runtime, and shaded client jar.
- `cupidbot-plugins`: the local CupidBot Hub plugin source and local
  `plugins.json` generator.
- `cupidbot-launcher`: the Electron launcher that starts local jars from
  `~/.cupidbot`.

Most OSRS updates should start in `cupidbot`. The launcher usually does not need
code changes because it does not download client jars; it only launches local
files named `cupidbot-<version>.jar`.

## Update Order

1. Pull or merge the latest RuneLite changes into `cupidbot`.
2. Resolve client, API, injection, mixin, and CupidBot runtime conflicts.
3. Build and test the CupidBot client.
4. Install the new client jar into `~/.cupidbot`.
5. Rebuild and install local CupidBot Hub plugins against that new jar.
6. Package the launcher only when launcher code or packaging metadata changed.

Keep RuneLite's official Plugin Hub online. Keep CupidBot Hub plugin discovery
and jar loading local/offline through `~/.runelite/cupidbot-plugins`.

## Client Update

Start clean and work on an update branch:

```bash
cd /home/frank/micro-client-custom/cupidbot
git status --short
git remote get-url runelite >/dev/null 2>&1 || \
  git remote add runelite https://github.com/runelite/runelite.git
git fetch runelite
git switch -c update/runelite-YYYY-MM-DD
git merge runelite/master
```

Resolve conflicts in the smallest sensible slices. Pay closest attention to:

- `gradle.properties`
- `runelite-api`
- `runelite-client`
- injected client, mixin, and deobfuscation changes
- `net.runelite.client.plugins.cupidbot`
- local plugin loader code
- tests and generated baselines

After conflicts are resolved, update `cupidbot.version` in `gradle.properties`
when publishing a new local client jar. Keep RuneLite version properties aligned
with the merged RuneLite code.

Build and run the repo-specific gates:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :client:runUnitTests :client:assemble
```

Install the built jar for the launcher:

```bash
scripts/install-cupidbot-launcher-jar.sh
```

Always use this helper for launcher jar installs. It writes a temporary file in
`~/.cupidbot` and publishes it with an atomic rename. Do not use raw `cp` over
`~/.cupidbot/cupidbot-<version>.jar` while a client is running; that can rewrite
the same inode a Java classloader is still reading and trigger late
`NoClassDefFoundError` crashes. Restart any running CupidBot client after the
install to load the new jar.

Verify the launcher install points at the jar that was just built:

```bash
version="$(awk -F= '$1 == "cupidbot.version" { print $2; exit }' gradle.properties)"
cmp -s "runelite-client/build/libs/cupidbot-${version}.jar" "$HOME/.cupidbot/cupidbot-${version}.jar"
```

`cmp` exits `0` when the launcher jar matches the build output.

## Local Plugin Update

Rebuild local plugins after the new client jar exists:

```bash
cd /home/frank/micro-client-custom/cupidbot
version="$(awk -F= '$1 == "cupidbot.version" { print $2; exit }' gradle.properties)"
client_jar="/home/frank/micro-client-custom/cupidbot/runelite-client/build/libs/cupidbot-${version}.jar"

cd /home/frank/micro-client-custom/cupidbot-plugins
JAVA_HOME=/usr/lib/jvm/java-11-openjdk ./gradlew clean build generatePluginsJson copyPluginDocs \
  -PcupidbotClientPath="$client_jar"
scripts/install-cupidbot-local-plugins.sh
```

The install helper writes:

- `~/.runelite/cupidbot-plugins/plugins.json`
- `~/.runelite/cupidbot-plugins/<InternalName>.jar`
- optional plugin docs under `~/.runelite/cupidbot-plugins/plugins/`

If a plugin fails because the upstream RuneLite API changed, fix the shared
compatibility surface first when possible. If a plugin is not ready yet, keep it
visible as disabled or unavailable with a reason instead of blocking the whole
local repository.

## Launcher Update

Do not change the launcher just because OSRS updated. The normal launcher update
is only a new local client jar in `~/.cupidbot`.

Run launcher checks only when launcher code, local jar selection, auth flow, or
packaging changed:

```bash
cd /home/frank/micro-client-custom/cupidbot-launcher
npm install
npm test
npx electron-builder --linux AppImage snap --publish never
```

Use `--publish never` for local packaging. The plain `npm run release` target may
try to publish snap artifacts and require Snap Store tooling.

## Runtime Smoke Checks

After the builds pass:

1. Start the launcher from `cupidbot-launcher` with `npm run dev`.
2. Confirm the launcher sees the new CupidBot client version.
3. Launch the client.
4. Confirm normal RuneLite/Jagex startup reaches the login screen.
5. Confirm the local plugin list loads from `~/.runelite/cupidbot-plugins`.
6. If testing a plugin fix, capture relevant lines from `/tmp/cupidbot.log`.

## Closeout Checklist

Before calling an OSRS update done:

- `git status --short` is understood in each touched repo.
- `:client:runUnitTests` passed in `cupidbot`.
- `:client:assemble` produced `runelite-client/build/libs/cupidbot-<version>.jar`.
- `scripts/install-cupidbot-launcher-jar.sh` installed the same jar into
  `~/.cupidbot`.
- `cupidbot-plugins` rebuilt and installed local plugin jars against the new
  client jar.
- Launcher tests passed if launcher code changed.
- The update branch is committed after the final green gate.
