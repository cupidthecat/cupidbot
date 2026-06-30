# Runtime Boundary Checklist

CupidBot local mode removes project-owned cloud calls and remote jar downloads from the core client and plugin loader. Local plugins may still use networking for their own features.

## Allowed Runtime Networking

- Official RuneLite services used by the base client and official Plugin Hub
- Jagex/OSRS login, account, config, world, and game endpoints
- Localhost Agent Server or debug tooling when explicitly enabled
- Local CupidBot Hub plugins, including plugin-owned APIs or webhooks

## Removed Runtime Paths

- Project cloud telemetry and version checks
- Remote random facts, external IP checks, and install counters
- GitHub release lookup or jar downloads for local CupidBot Hub plugins
Plugin-owned networking is allowed when it is part of a locally installed plugin.

## Verification

Use focused scans after edits:

```bash
rg -n "https?://|openConnection|new URL|HttpClient|OkHttpClient|Socket\\b|WebSocket|telemetry|analytics" \
  cupidbot cupidbot-plugins cupidbot-launcher
```

Review matches manually. `localhost`, official RuneLite, Jagex, OSRS, and local plugin-owned endpoints are expected; core cloud, release download, and telemetry endpoints are not.

## Input and Timing Checklist

Shared automation input should avoid fastest-possible defaults and rectangular timing patterns.

- Keep new-profile mouse speed at a middle preset unless a profile explicitly overrides it.
- Keep new-profile mouse smoothness at the balanced preset unless a profile explicitly overrides it.
- Clamp probability settings to `0.0..1.0` when loading profiles and before runtime use.
- Treat action cooldown chances `>= 1.0` as always-on and `<= 0.0` as disabled.
- Keep natural mouse movement fatigue tied to `SessionFatigue` and cap it at the selected mouse preset maximum.
- Keep public mouse movement on the natural-motion path, but dispatch individual generated motion steps through the raw `moveInstant` path to avoid recursive smoothing.
- Preserve small bounded delays between synthetic mouse press/release/click events.

Focused verification:

```bash
./gradlew :client:runUnitTests \
  --tests "*MouseSpeedTest" \
  --tests "*MouseSmoothnessTest" \
  --tests "*Rs2AntibanSettingsProfilePersistenceTest" \
  --tests "*Rs2AntibanActivityPlayStyleTest" \
  --tests "*MousePanelTest" \
  --tests "*FactoryTemplatesMouseSpeedTest" \
  --tests "*VirtualMouseUngatedMotionTest"
```
