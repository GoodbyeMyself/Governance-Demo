# Governance-Demo 部署文档（Systemd 版，产物上传部署）

> 适用场景：非 Docker 的常规 Linux 服务器（具备 `systemd`），前后端产物已构建完成并手动上传。  
> 目标：`Nginx + Spring Boot + MySQL`，支持开机自启与标准运维流程。

---

## 0. 一键脚本（推荐）

仓库已提供：`bootstrap_systemd.sh`  
该脚本会自动完成：

- 安装依赖（Java17 / MySQL / Nginx）
- 初始化 MySQL 库与账号
- 发布上传产物（JAR + 前端 dist 包）
- 生成后端 env、systemd service、Nginx 配置
- 启动并验证服务

执行前请先把产物上传到：

- `/opt/governance-demo/upload/data-governance-0.0.1-SNAPSHOT.jar`
- `/opt/governance-demo/upload/frontend-dist.tar.gz`

执行方式：

```bash
sudo bash bootstrap_systemd.sh
```

常用可选变量示例：

```bash
sudo NGINX_PORT=9001 \
  DB_PASSWORD='ChangeThis_DB_Password!' \
  AUTH_CENTER_ADMIN_PASSWORD='ChangeThis_Admin_Password!' \
  AUTH_CENTER_JWT_SECRET='Replace_With_Your_Long_Random_Secret' \
  APP_CORS_ALLOWED_ORIGIN_PATTERNS='http://localhost:9001,http://127.0.0.1:9001' \
  bash bootstrap_systemd.sh
```

---

## 1. 产物准备

部署前准备两个产物：

1. 后端 JAR  
`data-governance-0.0.1-SNAPSHOT.jar`

2. 前端静态包  
`frontend-dist.tar.gz`（由 `frontend/dist` 打包）

本地打包示例：

```bash
cp backend/target/data-governance-0.0.1-SNAPSHOT.jar .
tar -zcf frontend-dist.tar.gz -C frontend/dist .
```

---

## 2. 服务器目录规划

```text
/opt/governance-demo/upload            # 上传区
/opt/governance-demo/backend           # 后端运行目录
/var/www/governance-demo               # 前端发布目录
/etc/governance-demo/backend.env       # 后端环境变量
/etc/systemd/system/governance-backend.service
```

创建目录：

```bash
sudo mkdir -p /opt/governance-demo/upload
sudo mkdir -p /opt/governance-demo/backend
sudo mkdir -p /var/www/governance-demo
sudo mkdir -p /etc/governance-demo
```

---

## 3. 安装依赖（CentOS Stream 9）

```bash
sudo dnf install -y rsync tar gzip nginx mysql-server java-17-openjdk-headless
```

版本检查：

```bash
java -version
nginx -v
mysql --version
```

---

## 4. MySQL 初始化与开机自启

启动：

```bash
sudo systemctl enable --now mysqld
sudo systemctl status mysqld --no-pager
```

首次安装获取临时 root 密码：

```bash
sudo grep 'temporary password' /var/log/mysqld.log
```

执行安全初始化：

```bash
sudo mysql_secure_installation
```

创建业务库和账号：

```bash
mysql -uroot -p <<'SQL'
CREATE DATABASE IF NOT EXISTS governance_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'governance'@'127.0.0.1' IDENTIFIED BY 'ChangeThis_DB_Password!';
GRANT ALL PRIVILEGES ON governance_demo.* TO 'governance'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
```

验证：

```bash
mysql -h127.0.0.1 -ugovernance -pChangeThis_DB_Password! -e "SHOW DATABASES LIKE 'governance_demo';"
```

---

## 5. 上传并发布产物

假设你已将产物上传到：

- `/opt/governance-demo/upload/data-governance-0.0.1-SNAPSHOT.jar`
- `/opt/governance-demo/upload/frontend-dist.tar.gz`

发布后端：

```bash
sudo cp /opt/governance-demo/upload/data-governance-0.0.1-SNAPSHOT.jar /opt/governance-demo/backend/data-governance.jar
```

发布前端：

```bash
sudo rm -rf /opt/governance-demo/upload/frontend-dist
sudo mkdir -p /opt/governance-demo/upload/frontend-dist
sudo tar -xzf /opt/governance-demo/upload/frontend-dist.tar.gz -C /opt/governance-demo/upload/frontend-dist
sudo rsync -av --delete /opt/governance-demo/upload/frontend-dist/ /var/www/governance-demo/
```

---

## 6. 配置后端 systemd 服务

创建运行用户：

```bash
sudo useradd --system --home /opt/governance-demo --shell /sbin/nologin governance || true
sudo chown -R governance:governance /opt/governance-demo/backend
```

创建环境变量文件：

```bash
sudo tee /etc/governance-demo/backend.env >/dev/null <<'EOF'
DB_URL=jdbc:mysql://127.0.0.1:3306/governance_demo?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=governance
DB_PASSWORD=ChangeThis_DB_Password!

AUTH_CENTER_JWT_SECRET=Replace_With_Your_Long_Random_Secret
AUTH_CENTER_JWT_EXPIRE_SECONDS=86400
AUTH_CENTER_ADMIN_USERNAME=admin
AUTH_CENTER_ADMIN_PASSWORD=ChangeThis_Admin_Password!
AUTH_CENTER_ADMIN_NICKNAME=System Admin
APP_CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:9001,http://127.0.0.1:9001

JAVA_OPTS=-Xms256m -Xmx1024m
EOF
sudo chmod 600 /etc/governance-demo/backend.env
sudo chown root:root /etc/governance-demo/backend.env
```

创建 service 文件：

```bash
sudo tee /etc/systemd/system/governance-backend.service >/dev/null <<'EOF'
[Unit]
Description=Governance Demo Backend
After=network.target mysqld.service
Wants=mysqld.service

[Service]
Type=simple
User=governance
Group=governance
WorkingDirectory=/opt/governance-demo/backend
EnvironmentFile=/etc/governance-demo/backend.env
ExecStart=/bin/bash -lc 'exec /usr/bin/java $JAVA_OPTS -jar /opt/governance-demo/backend/data-governance.jar'
SuccessExitStatus=143
Restart=always
RestartSec=5
LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
EOF
```

启用并启动：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now governance-backend
sudo systemctl status governance-backend --no-pager
```

查看日志：

```bash
sudo journalctl -u governance-backend -f
```

---

## 7. 配置 Nginx（建议 9001，可改 80/443）

创建站点配置：

```bash
sudo tee /etc/nginx/conf.d/governance-demo.conf >/dev/null <<'EOF'
server {
    listen 9001;
    server_name _;

    root /var/www/governance-demo;
    index index.html;

    client_max_body_size 20m;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Request-Id $request_id;
        proxy_read_timeout 60s;
    }

    location = /runtime-config.js {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    location /assets/ {
        add_header Cache-Control "public, max-age=31536000, immutable";
        try_files $uri =404;
    }
}
EOF
```

启用 Nginx：

```bash
sudo nginx -t
sudo systemctl enable --now nginx
sudo systemctl reload nginx
```

---

## 8. 验证步骤

页面验证：

```bash
curl -i http://localhost:9001/
```

受保护接口验证（预期 401）：

```bash
curl -i http://localhost:9001/api/workbench/overview
```

登录验证（预期 success=true）：

```bash
curl -s -X POST http://localhost:9001/api/auth-center/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"ChangeThis_Admin_Password!"}'
```

数据库表验证：

```bash
mysql -h127.0.0.1 -ugovernance -pChangeThis_DB_Password! -D governance_demo -e "SHOW TABLES;"
```

---

## 9. 日常运维

状态检查：

```bash
sudo systemctl status mysqld --no-pager
sudo systemctl status governance-backend --no-pager
sudo systemctl status nginx --no-pager
```

重启：

```bash
sudo systemctl restart governance-backend
sudo systemctl reload nginx
```

日志：

```bash
sudo journalctl -u governance-backend -n 200 --no-pager
sudo tail -n 200 /var/log/nginx/error.log
```

---

## 10. 发布新版本（继续上传产物）

后端发布：

```bash
sudo cp /opt/governance-demo/upload/data-governance-0.0.1-SNAPSHOT.jar /opt/governance-demo/backend/data-governance.jar
sudo chown governance:governance /opt/governance-demo/backend/data-governance.jar
sudo systemctl restart governance-backend
```

前端发布：

```bash
sudo rm -rf /opt/governance-demo/upload/frontend-dist
sudo mkdir -p /opt/governance-demo/upload/frontend-dist
sudo tar -xzf /opt/governance-demo/upload/frontend-dist.tar.gz -C /opt/governance-demo/upload/frontend-dist
sudo rsync -av --delete /opt/governance-demo/upload/frontend-dist/ /var/www/governance-demo/
sudo systemctl reload nginx
```

---

## 11. 常见问题：登录接口 403

现象：

- 登录请求返回 `403`
- 响应体包含 `Invalid CORS request`

原因：

- 后端 CORS 白名单未包含当前前端访问来源（origin）。

排查：

```bash
curl -i -X OPTIONS "http://localhost:9001/api/auth-center/login" \
  -H "Origin: http://localhost:9001" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type"
```

修复：

1. 设置允许来源（示例）：

```bash
sudo sed -i '/^APP_CORS_ALLOWED_ORIGIN_PATTERNS=/d' /etc/governance-demo/backend.env
echo 'APP_CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:9001,http://127.0.0.1:9001' | sudo tee -a /etc/governance-demo/backend.env
```

2. 重启后端：

```bash
sudo systemctl restart governance-backend
```

3. 再次验证登录接口：

```bash
curl -s -X POST http://localhost:9001/api/auth-center/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"ChangeThis_Admin_Password!"}'
```
