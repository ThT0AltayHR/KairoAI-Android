#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
command -v kotlinc >/dev/null 2>&1 || { echo 'kotlinc bulunamadı; bu ek smoke test için Kotlin compiler gerekli.' >&2; exit 2; }
kotlinc \
  "$ROOT/app/src/main/java/com/altayhr/kairo/terminal/TerminalCommandPolicy.kt" \
  "$ROOT/scripts/TerminalPolicySmoke.kt" \
  -include-runtime -d "$TMP/terminal-policy-smoke.jar"
java -jar "$TMP/terminal-policy-smoke.jar"
