#!/usr/bin/env bash
set -euo pipefail

# One-click bootstrap for non-Docker Linux hosts with systemd.
# Deployment model: artifacts are prebuilt and uploaded manually.

log() {
  echo "[INFO] $*"
}

warn() {
  echo "[WARN] $*" >&2
}

fail() {
  echo "[ERROR] $*" >&2
  exit 1
}

require_root() {
  if [[ "${EUID}" -ne 0 ]]; then
    fail "Please run as root: sudo bash bootstrap_systemd.sh"
  fi
}

check_systemd() {
  if ! command -v systemctl >/dev/null 2>&1; then
    fail "systemctl not found. This script is for systemd hosts."
  fi
  if ! systemctl list-unit-files >/dev/null 2>&1; then
    fail "systemd does not seem active. Use Docker doc instead (DEPLOYMENT.md)."
  fi
}

install_packages() {
  if command -v dnf >/dev/null 2>&1; then
    log "Installing dependencies via dnf..."
    dnf install -y rsync tar gzip nginx mysql-server java-17-openjdk-headless curl
  elif command -v apt-get >/dev/null 2>&1; then
    log "Installing dependencies via apt..."
    apt-get update
    apt-get install -y rsync tar gzip nginx mysql-server openjdk-17-jre-headless curl
  else
    fail "Unsupported package manager. Install nginx/mysql/java17/rsync manually."
  fi
}

mysql_root_cmd() {
  if mysql -uroot -e "SELECT 1;" >/dev/null 2>&1; then
    echo "mysql -uroot"
    return
  fi
  if [[ -n "${MYSQL_ROOT_PASSWORD}" ]] && mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "SELECT 1;" >/dev/null 2>&1; then
    echo "mysql -uroot -p${MYSQL_ROOT_PASSWORD}"
    return
  fi
  fail "Cannot login MySQL as root. Set MYSQL_ROOT_PASSWORD or initialize root account first."
}

sql_escape() {
  printf "%s" "$1" | sed "s/'/''/g"
}

configure_mysql() {
  log "Ensuring MySQL is started..."
  systemctl enable --now mysqld

  local root_cmd
  root_cmd="$(mysql_root_cmd)"
  local db_pass_esc
  db_pass_esc="$(sql_escape "${DB_PASSWORD}")"

  log "Initializing database and user..."
  eval "${root_cmd}" <<SQL
CREATE DATABASE IF NOT EXISTS ${DB_NAME}
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'127.0.0.1' IDENTIFIED BY '${db_pass_esc}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USERNAME}'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
}

prepare_dirs() {
  log "Preparing directories..."
  mkdir -p "${UPLOAD_DIR}" "${BACKEND_DIR}" "${FRONTEND_DIR}" "${APP_ETC_DIR}"
}

ensure_user() {
  if ! id "${APP_USER}" >/dev/null 2>&1; then
    log "Creating service user: ${APP_USER}"
    useradd --system --home "${APP_ROOT}" --shell /sbin/nologin "${APP_USER}"
  fi
}

publish_artifacts() {
  local jar_src="${UPLOAD_DIR}/${JAR_FILENAME}"
  local fe_tar="${UPLOAD_DIR}/${FRONTEND_TAR_FILENAME}"
  local fe_tmp="${UPLOAD_DIR}/frontend-dist"

  [[ -f "${jar_src}" ]] || fail "Missing backend artifact: ${jar_src}"
  [[ -f "${fe_tar}" ]] || fail "Missing frontend artifact: ${fe_tar}"

  log "Publishing backend artifact..."
  cp "${jar_src}" "${BACKEND_DIR}/data-governance.jar"
  chown -R "${APP_USER}:${APP_USER}" "${BACKEND_DIR}"

  log "Publishing frontend artifact..."
  rm -rf "${fe_tmp}"
  mkdir -p "${fe_tmp}"
  tar -xzf "${fe_tar}" -C "${fe_tmp}"
  rsync -av --delete "${fe_tmp}/" "${FRONTEND_DIR}/" >/dev/null
}

write_backend_env() {
  log "Writing backend env file..."
  local jwt_secret="${AUTH_CENTER_JWT_SECRET}"
  if [[ -z "${jwt_secret}" ]]; then
    if command -v openssl >/dev/null 2>&1; then
      jwt_secret="$(openssl rand -base64 48)"
    else
      jwt_secret="replace-this-jwt-secret-$(date +%s)"
      warn "openssl not found; generated weak fallback JWT secret."
    fi
  fi

  cat >"${BACKEND_ENV_FILE}" <<EOF
DB_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}

AUTH_CENTER_JWT_SECRET=${jwt_secret}
AUTH_CENTER_JWT_EXPIRE_SECONDS=${AUTH_CENTER_JWT_EXPIRE_SECONDS}
AUTH_CENTER_ADMIN_USERNAME=${AUTH_CENTER_ADMIN_USERNAME}
AUTH_CENTER_ADMIN_PASSWORD=${AUTH_CENTER_ADMIN_PASSWORD}
AUTH_CENTER_ADMIN_NICKNAME=${AUTH_CENTER_ADMIN_NICKNAME}
APP_CORS_ALLOWED_ORIGIN_PATTERNS=${APP_CORS_ALLOWED_ORIGIN_PATTERNS}

JAVA_OPTS=${JAVA_OPTS}
EOF

  chmod 600 "${BACKEND_ENV_FILE}"
  chown root:root "${BACKEND_ENV_FILE}"
}

write_systemd_service() {
  log "Writing systemd service..."
  cat >"${SERVICE_FILE}" <<EOF
[Unit]
Description=Governance Demo Backend
After=network.target mysqld.service
Wants=mysqld.service

[Service]
Type=simple
User=${APP_USER}
Group=${APP_USER}
WorkingDirectory=${BACKEND_DIR}
EnvironmentFile=${BACKEND_ENV_FILE}
ExecStart=/bin/bash -lc 'exec /usr/bin/java \$JAVA_OPTS -jar ${BACKEND_DIR}/data-governance.jar'
SuccessExitStatus=143
Restart=always
RestartSec=5
LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
EOF

  systemctl daemon-reload
  systemctl enable --now governance-backend
}

write_nginx_conf() {
  log "Writing Nginx config..."
  cat >"${NGINX_CONF_FILE}" <<EOF
server {
    listen ${NGINX_PORT};
    server_name ${NGINX_SERVER_NAME};

    root ${FRONTEND_DIR};
    index index.html;

    client_max_body_size 20m;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:${APP_PORT};
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Request-Id \$request_id;
        proxy_read_timeout 60s;
    }

    location = /runtime-config.js {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    location /assets/ {
        add_header Cache-Control "public, max-age=31536000, immutable";
        try_files \$uri =404;
    }
}
EOF

  nginx -t
  systemctl enable --now nginx
  systemctl reload nginx
}

configure_selinux_if_needed() {
  if command -v getenforce >/dev/null 2>&1 && [[ "$(getenforce)" == "Enforcing" ]]; then
    warn "SELinux Enforcing detected. Applying common nginx allowances."
    if command -v setsebool >/dev/null 2>&1; then
      setsebool -P httpd_can_network_connect 1 || warn "setsebool failed; check SELinux policy manually."
    fi
    if command -v chcon >/dev/null 2>&1; then
      chcon -R -t httpd_sys_content_t "${FRONTEND_DIR}" || warn "chcon failed; restorecon may be required."
    fi
  fi
}

configure_firewalld_if_needed() {
  if systemctl is-active --quiet firewalld 2>/dev/null && command -v firewall-cmd >/dev/null 2>&1; then
    log "Opening firewalld port ${NGINX_PORT}/tcp..."
    firewall-cmd --permanent --add-port="${NGINX_PORT}/tcp" >/dev/null
    firewall-cmd --reload >/dev/null
  fi
}

verify_deployment() {
  log "Verifying services..."
  systemctl --no-pager --full status mysqld | sed -n '1,6p'
  systemctl --no-pager --full status governance-backend | sed -n '1,8p'
  systemctl --no-pager --full status nginx | sed -n '1,6p'

  local http_code_root
  local http_code_api
  http_code_root="$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${NGINX_PORT}/")"
  http_code_api="$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${NGINX_PORT}/api/workbench/overview")"

  log "HTTP / status: ${http_code_root} (expected 200)"
  log "HTTP /api/workbench/overview status: ${http_code_api} (expected 401 before login)"
}

print_summary() {
  cat <<EOF

[DONE] Deployment finished.
  Frontend URL:  http://<server-ip>:${NGINX_PORT}/
  Backend proxy: http://<server-ip>:${NGINX_PORT}/api

Useful commands:
  systemctl status governance-backend --no-pager
  journalctl -u governance-backend -f
  systemctl reload nginx

EOF
}

main() {
  require_root
  check_systemd

  install_packages
  configure_mysql
  prepare_dirs
  ensure_user
  publish_artifacts
  write_backend_env
  write_systemd_service
  write_nginx_conf
  configure_selinux_if_needed
  configure_firewalld_if_needed
  verify_deployment
  print_summary
}

# -----------------------------
# Configurable variables (env)
# -----------------------------
APP_ROOT="${APP_ROOT:-/opt/governance-demo}"
UPLOAD_DIR="${UPLOAD_DIR:-${APP_ROOT}/upload}"
BACKEND_DIR="${BACKEND_DIR:-${APP_ROOT}/backend}"
FRONTEND_DIR="${FRONTEND_DIR:-/var/www/governance-demo}"
APP_ETC_DIR="${APP_ETC_DIR:-/etc/governance-demo}"
BACKEND_ENV_FILE="${BACKEND_ENV_FILE:-${APP_ETC_DIR}/backend.env}"
SERVICE_FILE="${SERVICE_FILE:-/etc/systemd/system/governance-backend.service}"
NGINX_CONF_FILE="${NGINX_CONF_FILE:-/etc/nginx/conf.d/governance-demo.conf}"

APP_USER="${APP_USER:-governance}"
JAR_FILENAME="${JAR_FILENAME:-data-governance-0.0.1-SNAPSHOT.jar}"
FRONTEND_TAR_FILENAME="${FRONTEND_TAR_FILENAME:-frontend-dist.tar.gz}"

APP_PORT="${APP_PORT:-8080}"
NGINX_PORT="${NGINX_PORT:-9001}"
NGINX_SERVER_NAME="${NGINX_SERVER_NAME:-_}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-governance_demo}"
DB_USERNAME="${DB_USERNAME:-governance}"
DB_PASSWORD="${DB_PASSWORD:-ChangeThis_DB_Password!}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"

AUTH_CENTER_JWT_SECRET="${AUTH_CENTER_JWT_SECRET:-}"
AUTH_CENTER_JWT_EXPIRE_SECONDS="${AUTH_CENTER_JWT_EXPIRE_SECONDS:-86400}"
AUTH_CENTER_ADMIN_USERNAME="${AUTH_CENTER_ADMIN_USERNAME:-admin}"
AUTH_CENTER_ADMIN_PASSWORD="${AUTH_CENTER_ADMIN_PASSWORD:-ChangeThis_Admin_Password!}"
AUTH_CENTER_ADMIN_NICKNAME="${AUTH_CENTER_ADMIN_NICKNAME:-System Admin}"
APP_CORS_ALLOWED_ORIGIN_PATTERNS="${APP_CORS_ALLOWED_ORIGIN_PATTERNS:-http://localhost:${NGINX_PORT},http://127.0.0.1:${NGINX_PORT}}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx1024m}"

main "$@"
