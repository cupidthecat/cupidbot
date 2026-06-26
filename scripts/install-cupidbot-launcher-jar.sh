#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
version="$(awk -F= '$1 == "cupidbot.version" { print $2; exit }' "$repo_root/gradle.properties")"
jar_path="${1:-$repo_root/runelite-client/build/libs/cupidbot-${version}.jar}"
target_dir="${CUPIDBOT_LAUNCHER_DIR:-$HOME/.cupidbot}"

if [[ ! -f "$jar_path" ]]; then
  echo "Missing CupidBot jar: $jar_path" >&2
  echo "Build it first with: JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :client:assemble" >&2
  exit 1
fi

mkdir -p "$target_dir"
install -m 0644 "$jar_path" "$target_dir/cupidbot-${version}.jar"

echo "Installed $target_dir/cupidbot-${version}.jar"
