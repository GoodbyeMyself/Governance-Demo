#!/usr/bin/env bash
set -euo pipefail

pkill -f '/opt/governance-demo/jars/.*\.jar' >/dev/null 2>&1 || true
rm -f /opt/governance-demo/run/*.pid
