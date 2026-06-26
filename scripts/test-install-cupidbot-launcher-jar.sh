#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
version="$(awk -F= '$1 == "cupidbot.version" { print $2; exit }' "$repo_root/gradle.properties")"
tmpdir="$(mktemp -d)"
cleanup() {
  exec 3<&- || true
  rm -rf "$tmpdir"
}
trap cleanup EXIT

source_jar="$tmpdir/source.jar"
target_dir="$tmpdir/launcher"
target_jar="$target_dir/cupidbot-${version}.jar"

mkdir -p "$target_dir"
printf 'old runtime jar\n' > "$target_jar"
printf 'new runtime jar\n' > "$source_jar"

before_inode="$(stat -c '%i' "$target_jar")"
exec 3< "$target_jar"

install_output="$(CUPIDBOT_LAUNCHER_DIR="$target_dir" "$repo_root/scripts/install-cupidbot-launcher-jar.sh" "$source_jar")"

after_inode="$(stat -c '%i' "$target_jar")"
path_content="$(cat "$target_jar")"
fd_content="$(cat <&3)"

if [[ "$path_content" != "new runtime jar" ]]; then
  echo "target path did not receive the new jar content" >&2
  exit 1
fi

if [[ "$fd_content" != "old runtime jar" ]]; then
  echo "open runtime descriptor was modified; launcher jar install must be atomic" >&2
  exit 1
fi

if [[ "$before_inode" == "$after_inode" ]]; then
  echo "target inode was reused; launcher jar install must publish via rename" >&2
  exit 1
fi

if [[ "$install_output" != *"Installed atomically"* ]]; then
  echo "installer output should identify the launcher jar publish as atomic" >&2
  exit 1
fi

if [[ "$install_output" != *"Restart running CupidBot clients"* ]]; then
  echo "installer output should remind operators that running clients keep the old jar" >&2
  exit 1
fi
