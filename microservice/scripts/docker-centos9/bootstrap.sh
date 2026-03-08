#!/usr/bin/env bash
set -euo pipefail

BASE_DIR=/opt/governance-demo
RUN_DIR="${BASE_DIR}/run"
NACOS_DIR=/opt/nacos

if [ -f "${RUN_DIR}/runtime.env" ]; then
  source "${RUN_DIR}/runtime.env"
fi

mkdir -p /var/run/mysqld
mkdir -p "${BASE_DIR}/logs"
chown -R mysql:mysql /var/run/mysqld /var/lib/mysql
setcap -r /usr/libexec/mysqld >/dev/null 2>&1 || true

if [ ! -d /var/lib/mysql/mysql ]; then
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

if command -v sshd >/dev/null 2>&1; then
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
