#!/usr/bin/env bash
# 微服务启动脚本。
# 统一按固定顺序拉起后端服务，并把 PID 写入运行目录，便于 stop 脚本回收。
set -euo pipefail

BASE_DIR=/opt/governance-demo
RUN_DIR="${BASE_DIR}/run"
"${RUN_DIR}/service-manager.sh" start all
