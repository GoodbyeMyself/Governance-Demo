#!/usr/bin/env bash
set -euo pipefail

BASE_DIR=/opt/governance-demo
LOG_DIR="${BASE_DIR}/logs"
RUN_DIR="${BASE_DIR}/run"

source "${RUN_DIR}/runtime.env"

mkdir -p "${LOG_DIR}"
pkill -f '/opt/governance-demo/jars/.*\.jar' >/dev/null 2>&1 || true

start_service() {
  local name="$1"
  local jar="$2"

  nohup java -Xms256m -Xmx512m -jar "${jar}" >"${LOG_DIR}/${name}.log" 2>&1 &
  echo $! >"${RUN_DIR}/${name}.pid"
}

start_service bms-service "${BASE_DIR}/jars/bms-service.jar"
start_service data-source "${BASE_DIR}/jars/data-source.jar"
start_service data-metadata "${BASE_DIR}/jars/data-metadata.jar"
start_service auth-center "${BASE_DIR}/jars/auth-center.jar"
start_service gateway "${BASE_DIR}/jars/gateway.jar"
