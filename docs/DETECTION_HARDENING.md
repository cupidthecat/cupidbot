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
