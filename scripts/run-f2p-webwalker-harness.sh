#!/usr/bin/env bash
set -euo pipefail

CASE_ID="${1:-all}"
TIMEOUT_MS="${CUPIDBOT_WEBWALKER_TIMEOUT_MS:-1800000}"
LEG_TIMEOUT_MS="${CUPIDBOT_WEBWALKER_LEG_TIMEOUT_MS:-240000}"
OUTPUT_DIR="${CUPIDBOT_WEBWALKER_OUTPUT_DIR:-$HOME/.runelite/test-results/f2p-webwalker}"
USE_TELEPORTATION_SPELLS="${CUPIDBOT_WEBWALKER_USE_TELEPORTATION_SPELLS:-}"

cd "$(dirname "$0")/.."

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

./gradlew :client:compileJava

CMD=(
  ./gradlew :client:runTest
  -Dcupidbot.test.mode=true
  "-Dcupidbot.test.script=F2P Web Walker Harness"
  "-Dcupidbot.test.timeout=$TIMEOUT_MS"
  "-Dcupidbot.test.output=$OUTPUT_DIR"
  -Dcupidbot.test.webwalker.stopOnFailure=true
  "-Dcupidbot.test.webwalker.walkTimeoutMs=$LEG_TIMEOUT_MS"
)

if [[ "$CASE_ID" != "all" ]]; then
  CMD+=("-Dcupidbot.test.webwalker.case=$CASE_ID")
fi

if [[ -n "$USE_TELEPORTATION_SPELLS" ]]; then
  CMD+=("-Dcupidbot.test.webwalker.useTeleportationSpells=$USE_TELEPORTATION_SPELLS")
fi

set +e
"${CMD[@]}"
STATUS=$?
set -e

RESULT_FILE="$OUTPUT_DIR/result.json"
if [[ -f "$RESULT_FILE" ]]; then
  echo "Result written to $RESULT_FILE"
else
  echo "No result file was written at $RESULT_FILE" >&2
fi

exit "$STATUS"
