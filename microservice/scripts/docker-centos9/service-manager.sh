#!/usr/bin/env bash
# 单容器微服务管理脚本。
# 支持对单个服务或全部服务执行 start / stop / restart / status / logs / list。
set -euo pipefail

BASE_DIR=/opt/governance-demo
LOG_DIR="${BASE_DIR}/logs"
RUN_DIR="${BASE_DIR}/run"
JAR_DIR="${BASE_DIR}/jars"

mkdir -p "${LOG_DIR}" "${RUN_DIR}"

if [ -f "${RUN_DIR}/runtime.env" ]; then
  # shellcheck disable=SC1090
  source "${RUN_DIR}/runtime.env"
fi

DEFAULT_JAVA_OPTS="${SERVICE_JAVA_OPTS:--Xms256m -Xmx512m}"

usage() {
  cat <<'EOF'
Usage:
  service-manager.sh list
  service-manager.sh status [service|all]
  service-manager.sh start [service|all]
  service-manager.sh stop [service|all]
  service-manager.sh restart [service|all]
  service-manager.sh logs [service|all] [lines] [follow]

Services:
  gateway
  auth-center
  bms-service
  data-source
  data-metadata
  all
EOF
}

list_services() {
  cat <<'EOF'
bms-service
data-source
data-metadata
auth-center
gateway
EOF
}

service_exists() {
  local name="$1"
  case "$name" in
    bms-service|data-source|data-metadata|auth-center|gateway)
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

jar_path() {
  local name="$1"
  case "$name" in
    bms-service) echo "${JAR_DIR}/bms-service.jar" ;;
    data-source) echo "${JAR_DIR}/data-source.jar" ;;
    data-metadata) echo "${JAR_DIR}/data-metadata.jar" ;;
    auth-center) echo "${JAR_DIR}/auth-center.jar" ;;
    gateway) echo "${JAR_DIR}/gateway.jar" ;;
    *)
      echo "Unsupported service: ${name}" >&2
      exit 1
      ;;
  esac
}

pid_file() {
  local name="$1"
  echo "${RUN_DIR}/${name}.pid"
}

log_file() {
  local name="$1"
  echo "${LOG_DIR}/${name}.log"
}

read_pid() {
  local name="$1"
  local file
  file="$(pid_file "$name")"
  if [ -f "$file" ]; then
    tr -d '[:space:]' <"$file"
  fi
}

pid_is_alive() {
  local pid="${1:-}"
  if [ -z "$pid" ]; then
    return 1
  fi

  if ! kill -0 "$pid" >/dev/null 2>&1; then
    return 1
  fi

  local stat
  stat="$(ps -o stat= -p "$pid" 2>/dev/null | tr -d '[:space:]' || true)"
  if [ -z "$stat" ] || [[ "$stat" == Z* ]]; then
    return 1
  fi

  return 0
}

is_running() {
  local name="$1"
  local pid
  pid="$(read_pid "$name" || true)"
  if pid_is_alive "${pid:-}"; then
    return 0
  fi

  local jar
  jar="$(jar_path "$name")"

  while IFS= read -r candidate; do
    if pid_is_alive "$candidate"; then
      return 0
    fi
  done < <(pgrep -f "$jar" || true)

  return 1
}

normalize_services() {
  local service="${1:-all}"
  if [ "$service" = "all" ]; then
    list_services
    return 0
  fi

  if ! service_exists "$service"; then
    echo "Unsupported service: ${service}" >&2
    exit 1
  fi

  echo "$service"
}

wait_until_stopped() {
  local name="$1"
  for _ in $(seq 1 30); do
    if ! is_running "$name"; then
      return 0
    fi
    sleep 1
  done
  echo "Service stop timeout: ${name}" >&2
  return 1
}

start_service() {
  local name="$1"
  local jar
  jar="$(jar_path "$name")"
  local log
  log="$(log_file "$name")"

  if [ ! -f "$jar" ]; then
    echo "Jar not found for ${name}: ${jar}" >&2
    exit 1
  fi

  if is_running "$name"; then
    echo "[SKIP] ${name} is already running"
    return 0
  fi

  nohup java ${DEFAULT_JAVA_OPTS} -jar "$jar" >"$log" 2>&1 &
  local pid=$!
  echo "$pid" >"$(pid_file "$name")"
  echo "[OK] started ${name} (pid=${pid})"
}

stop_service() {
  local name="$1"
  local pid
  pid="$(read_pid "$name" || true)"

  if pid_is_alive "${pid:-}"; then
    kill "$pid" >/dev/null 2>&1 || true
  else
    local jar
    jar="$(jar_path "$name")"
    while IFS= read -r candidate; do
      kill "$candidate" >/dev/null 2>&1 || true
    done < <(pgrep -f "$jar" || true)
  fi

  wait_until_stopped "$name"
  rm -f "$(pid_file "$name")"
  echo "[OK] stopped ${name}"
}

status_service() {
  local name="$1"
  local log
  log="$(log_file "$name")"
  if is_running "$name"; then
    local pid
    pid="$(read_pid "$name" || true)"
    if [ -z "${pid:-}" ]; then
      pid="$(pgrep -f "$(jar_path "$name")" | head -n 1 || true)"
    fi
    echo "[RUNNING] ${name} pid=${pid:-unknown} log=${log}"
  else
    echo "[STOPPED] ${name} log=${log}"
  fi
}

logs_service() {
  local service="${1:-all}"
  local lines="${2:-200}"
  local follow="${3:-false}"

  if ! [[ "$lines" =~ ^[0-9]+$ ]]; then
    echo "lines must be an integer" >&2
    exit 1
  fi

  if [ "$service" = "all" ]; then
    local files=()
    while IFS= read -r item; do
      files+=("$(log_file "$item")")
    done < <(list_services)

    if [ "$follow" = "true" ]; then
      tail -n "$lines" -F "${files[@]}"
    else
      for file in "${files[@]}"; do
        echo "===== ${file} ====="
        if [ -f "$file" ]; then
          tail -n "$lines" "$file"
        else
          echo "(empty)"
        fi
        echo
      done
    fi
    return 0
  fi

  if ! service_exists "$service"; then
    echo "Unsupported service: ${service}" >&2
    exit 1
  fi

  local file
  file="$(log_file "$service")"
  if [ ! -f "$file" ]; then
    echo "Log file not found: ${file}" >&2
    exit 1
  fi

  if [ "$follow" = "true" ]; then
    tail -n "$lines" -F "$file"
  else
    tail -n "$lines" "$file"
  fi
}

ACTION="${1:-}"
SERVICE="${2:-all}"

case "${ACTION}" in
  list)
    list_services
    ;;
  start)
    while IFS= read -r service; do
      start_service "$service"
    done < <(normalize_services "$SERVICE")
    ;;
  stop)
    while IFS= read -r service; do
      stop_service "$service"
    done < <(normalize_services "$SERVICE")
    ;;
  restart)
    while IFS= read -r service; do
      stop_service "$service"
      start_service "$service"
    done < <(normalize_services "$SERVICE")
    ;;
  status)
    while IFS= read -r service; do
      status_service "$service"
    done < <(normalize_services "$SERVICE")
    ;;
  logs)
    logs_service "${SERVICE}" "${3:-200}" "${4:-false}"
    ;;
  *)
    usage
    exit 1
    ;;
esac
