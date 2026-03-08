#!/usr/bin/env bash
# 单容器运行时引导脚本。
# 负责初始化 MySQL、启动 Nacos、加载配置、拉起 nginx/sshd，并最终启动各微服务。
set -euo pipefail

BASE_DIR=/opt/governance-demo
RUN_DIR="${BASE_DIR}/run"
NACOS_DIR=/opt/nacos

if [ -f "${RUN_DIR}/runtime.env" ]; then
  # 加载初始化脚本生成的运行时环境变量。
  source "${RUN_DIR}/runtime.env"
fi

mkdir -p /var/run/mysqld
mkdir -p "${BASE_DIR}/logs"
chown -R mysql:mysql /var/run/mysqld /var/lib/mysql
setcap -r /usr/libexec/mysqld >/dev/null 2>&1 || true

if [ ! -d /var/lib/mysql/mysql ]; then
  # 首次启动时初始化 MySQL 数据目录。
  mysqld --initialize-insecure --user=mysql --datadir=/var/lib/mysql
fi

if ! pgrep -x mysqld >/dev/null 2>&1; then
  mysqld --user=mysql \
    --daemonize \
    --datadir=/var/lib/mysql \
    --bind-address=0.0.0.0 \
    --socket=/var/run/mysqld/mysqld.sock
fi

for _ in $(seq 1 30); do
  if mysqladmin --socket=/var/run/mysqld/mysqld.sock ping >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! pgrep -f 'nacos-server.jar' >/dev/null 2>&1; then
  # 启动单机模式 Nacos，并复用容器内预置插件目录。
  mkdir -p "${NACOS_DIR}/plugins" "${NACOS_DIR}/plugins/health" "${NACOS_DIR}/plugins/cmdb" "${NACOS_DIR}/plugins/selector"
  nohup java -Xms256m -Xmx256m -Xmn128m \
    -Dnacos.standalone=true \
    -Dloader.path="${NACOS_DIR}/plugins,${NACOS_DIR}/plugins/health,${NACOS_DIR}/plugins/cmdb,${NACOS_DIR}/plugins/selector" \
    -Dnacos.home="${NACOS_DIR}" \
    -jar "${NACOS_DIR}/target/nacos-server.jar" \
    --spring.config.additional-location=file:${NACOS_DIR}/conf/ \
    --spring.config.name=application \
    --logging.config="${NACOS_DIR}/conf/nacos-logback.xml" \
    --server.max-http-header-size=524288 \
    >"${NACOS_DIR}/logs/startup-console.log" 2>&1 &
fi

for _ in $(seq 1 60); do
  if curl -fsS http://127.0.0.1:8848/nacos >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

for file in "${BASE_DIR}"/nacos-config/*.yaml; do
  # 将本地生成的服务配置发布到 Nacos。
  data_id=$(basename "${file}")
  curl -fsS -X POST 'http://127.0.0.1:8848/nacos/v1/cs/configs' \
    -d "dataId=${data_id}" \
    -d 'group=DEFAULT_GROUP' \
    -d 'type=yaml' \
    --data-urlencode "content@${file}" >/dev/null
done

if ! pgrep -x nginx >/dev/null 2>&1; then
  nginx
else
  nginx -s reload
fi

if ! pgrep -f 'mail-catcher.py' >/dev/null 2>&1; then
  mkdir -p "${BASE_DIR}/mailbox"
  nohup python3 "${RUN_DIR}/mail-catcher.py" >"${BASE_DIR}/logs/mail-catcher.log" 2>&1 &
fi

if command -v sshd >/dev/null 2>&1; then
  # 可选启动 SSH，方便宿主机直接登录单容器环境。
  mkdir -p /var/run/sshd
  ssh-keygen -A >/dev/null 2>&1 || true
  if [ -n "${SSH_ROOT_PASSWORD:-}" ]; then
    echo "root:${SSH_ROOT_PASSWORD}" | chpasswd
  fi
  if ! pgrep -x sshd >/dev/null 2>&1; then
    /usr/sbin/sshd
  fi
fi

"${RUN_DIR}/start-services.sh"
