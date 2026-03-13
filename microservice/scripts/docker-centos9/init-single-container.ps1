[CmdletBinding()]
# 单容器初始化入口脚本。
# 负责构建产物、准备运行目录、创建并初始化 CentOS 9 容器，
# 再在容器内拉起 MySQL、Nacos、nginx、sshd 以及整套治理平台服务。
param(
    [string]$ContainerName = 'governance-centos9',
    [string]$CentosImage = 'centos9:latest',
    [string]$NacosSeedImage = 'nacos/nacos-server:v2.3.2',
    [switch]$SkipBuild,
    [switch]$SkipValidation,
    [switch]$KeepTemp,
    [string]$SshRootPassword = 'Governance@2026SSH',
    [string]$MysqlUsername = 'governance',
    [string]$MysqlPassword = 'Governance@2026',
    [string]$MysqlRootPassword = 'Root@2026',
    [string]$AdminUsername = 'admin',
    [string]$AdminPassword = 'Admin@123456',
    [string]$AdminNickname = 'System Admin',
    [string]$GatewayToken = 'governance-demo-gateway-token-2026',
    [string]$JwtSecret = 'governance-demo-auth-center-jwt-secret-2026'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Write-Step {
    param([string]$Message)
    Write-Host "[STEP] $Message" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Assert-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Missing command: $Name"
    }
}

function Invoke-External {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [string]$WorkingDirectory = $PWD.Path
    )

    Push-Location $WorkingDirectory
    try {
        & $FilePath @Arguments
        if ($LASTEXITCODE -ne 0) {
            throw "Command failed: $FilePath $($Arguments -join ' ')"
        }
    }
    finally {
        Pop-Location
    }
}

function Invoke-Docker {
    param([string[]]$Arguments)
    Invoke-External -FilePath 'docker' -Arguments $Arguments
}

function Invoke-DockerExec {
    param([string]$Command)
    # Avoid CRLF leaking into bash commands when running from Windows PowerShell.
    $normalizedCommand = $Command -replace "`r", ''
    Invoke-Docker @('exec', $ContainerName, 'bash', '-lc', $normalizedCommand)
}

function Test-DockerImage {
    param([string]$Image)
    try {
        & docker image inspect $Image *> $null
        return $LASTEXITCODE -eq 0
    }
    catch {
        return $false
    }
}

function Ensure-DockerImage {
    param([string]$Image)
    if (-not (Test-DockerImage $Image)) {
        Write-Step "Pull image $Image"
        Invoke-Docker @('pull', $Image)
    }
}

function Remove-ContainerIfExists {
    param([string]$Name)

    $containerNames = (& docker ps -a --format '{{.Names}}')
    if ($containerNames -contains $Name) {
        Write-Step "Remove existing container $Name"
        Invoke-Docker @('rm', '-f', $Name)
    }
}

function Get-ListeningPortInfo {
    param([int]$Port)

    try {
        return Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction Stop |
            Select-Object -First 1
    }
    catch {
        return $null
    }
}

function Wait-HostPortsAvailable {
    param(
        [int[]]$Ports,
        [int]$TimeoutSeconds = 60
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $busyPorts = @()
        foreach ($port in $Ports) {
            if (Get-ListeningPortInfo -Port $port) {
                $busyPorts += $port
            }
        }

        if ($busyPorts.Count -eq 0) {
            Write-Success "Host ports are available: $($Ports -join ', ')"
            return
        }

        Start-Sleep -Seconds 2
    }

    $busyDetails = foreach ($port in $Ports) {
        $connection = Get-ListeningPortInfo -Port $port
        if ($null -ne $connection) {
            $process = Get-Process -Id $connection.OwningProcess -ErrorAction SilentlyContinue
            if ($process) {
                "${port}($($process.ProcessName):$($connection.OwningProcess))"
            }
            else {
                "${port}(pid:$($connection.OwningProcess))"
            }
        }
    }

    throw "Host ports are still busy after ${TimeoutSeconds}s: $($busyDetails -join ', '). If the process is Docker Desktop, restart Docker Desktop and retry."
}

function Wait-TcpPort {
    param(
        [string]$Name,
        [int]$Port,
        [int]$TimeoutSeconds = 300
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $result = Test-NetConnection -ComputerName 127.0.0.1 -Port $Port -WarningAction SilentlyContinue
        if ($result.TcpTestSucceeded) {
            Write-Success "$Name port $Port is reachable"
            return
        }
        Start-Sleep -Seconds 2
    }

    throw "$Name port $Port did not become reachable within $TimeoutSeconds seconds"
}

function Wait-HttpOk {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TimeoutSeconds = 300
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Success "$Name responded with HTTP 200"
                return
            }
        }
        catch {
        }
        Start-Sleep -Seconds 2
    }

    throw "$Name did not return HTTP 200 within $TimeoutSeconds seconds: $Url"
}

function ConvertTo-BashSingleQuoted {
    param([string]$Value)
    $escapedSingleQuote = [string]::Concat("'", '"', "'", '"', "'")
    return "'" + $Value.Replace("'", $escapedSingleQuote) + "'"
}

function ConvertTo-SqlSingleQuoted {
    param([string]$Value)
    return "'" + $Value.Replace("'", "''") + "'"
}

function Write-Utf8NoBom {
    param(
        [string]$Path,
        [string]$Content
    )

    $encoding = New-Object System.Text.UTF8Encoding($false)
    $normalizedContent = $Content -replace "`r`n", "`n"
    [System.IO.File]::WriteAllText($Path, $normalizedContent, $encoding)
}

$scriptDir = Split-Path -Parent $PSCommandPath
$microserviceDir = (Resolve-Path (Join-Path $scriptDir '..\..')).Path
$repoRoot = (Resolve-Path (Join-Path $scriptDir '..\..\..')).Path
$webDir = (Join-Path $repoRoot 'web')
$tmpRoot = Join-Path $repoRoot '.tmp\docker-centos9'
$nacosTempDir = Join-Path $tmpRoot 'nacos'
$runtimeEnvPath = Join-Path $tmpRoot 'runtime.env'
$seedContainerName = "$ContainerName-nacos-seed"
$hostPorts = @(12222, 23306, 19001, 19002, 18080, 18081, 18082, 18083, 18084, 18085, 18086, 18848, 19848, 19849)

$gatewayJar = Join-Path $microserviceDir 'gateway\target\gateway-0.0.1-SNAPSHOT.jar'
$authJar = Join-Path $microserviceDir 'service\auth-center\target\auth-center-0.0.1-SNAPSHOT.jar'
$bmsJar = Join-Path $microserviceDir 'service\bms-service\target\bms-service-0.0.1-SNAPSHOT.jar'
$dataSourceJar = Join-Path $microserviceDir 'service\data-source\target\data-source-0.0.1-SNAPSHOT.jar'
$dataMetadataJar = Join-Path $microserviceDir 'service\data-metadata\target\data-metadata-0.0.1-SNAPSHOT.jar'
$iotDeviceJar = Join-Path $microserviceDir 'service\iot-device\target\iot-device-0.0.1-SNAPSHOT.jar'
$iotCollectionJar = Join-Path $microserviceDir 'service\iot-collection\target\iot-collection-0.0.1-SNAPSHOT.jar'
$governDistDir = Join-Path $webDir 'apps\govern\dist'
$portalDistDir = Join-Path $webDir 'apps\portal\dist'

Assert-Command 'docker'
if (-not $SkipBuild) {
    Assert-Command 'mvn'
    Assert-Command 'pnpm'
}

try {
    if (-not $SkipBuild) {
        Write-Step 'Build backend artifacts'
        Invoke-External -FilePath 'mvn' -Arguments @('-DskipTests', 'package') -WorkingDirectory $microserviceDir

        Write-Step 'Build frontend artifacts'
        Invoke-External -FilePath 'pnpm' -Arguments @('build') -WorkingDirectory $webDir
    }

    foreach ($artifact in @($gatewayJar, $authJar, $bmsJar, $dataSourceJar, $dataMetadataJar, $iotDeviceJar, $iotCollectionJar)) {
        if (-not (Test-Path $artifact)) {
            throw "Missing artifact: $artifact"
        }
    }

    if (-not (Test-Path (Join-Path $governDistDir 'index.html'))) {
        throw "Missing govern dist output: $governDistDir"
    }

    if (-not (Test-Path (Join-Path $portalDistDir 'index.html'))) {
        throw "Missing portal dist output: $portalDistDir"
    }

    Write-Step 'Ensure required Docker images'
    Ensure-DockerImage $CentosImage
    Ensure-DockerImage $NacosSeedImage

    Remove-ContainerIfExists -Name $ContainerName
    Remove-ContainerIfExists -Name $seedContainerName

    if (Test-Path $tmpRoot) {
        Remove-Item -Recurse -Force $tmpRoot
    }
    New-Item -ItemType Directory -Path $nacosTempDir -Force | Out-Null

    Write-Step 'Extract Nacos runtime from seed image'
    Invoke-Docker @('create', '--name', $seedContainerName, $NacosSeedImage)
    Invoke-Docker @('cp', "${seedContainerName}:/home/nacos/.", $nacosTempDir)
    Invoke-Docker @('rm', '-f', $seedContainerName)

    Write-Step 'Create single CentOS9 container'
    Wait-HostPortsAvailable -Ports $hostPorts -TimeoutSeconds 90
    Invoke-Docker @(
        'run', '-d',
        '--name', $ContainerName,
        '--restart', 'unless-stopped',
        '-p', '12222:22',
        '-p', '23306:3306',
        '-p', '19001:9001',
        '-p', '19002:9002',
        '-p', '18080:8080',
        '-p', '18081:8081',
        '-p', '18082:8082',
        '-p', '18083:8083',
        '-p', '18084:8084',
        '-p', '18085:8085',
        '-p', '18086:8086',
        '-p', '18848:8848',
        '-p', '19848:9848',
        '-p', '19849:9849',
        $CentosImage,
        'tail', '-f', '/dev/null'
    )

    Write-Step 'Install system dependencies inside container'
    $rootCredential = ConvertTo-BashSingleQuoted "root:$SshRootPassword"
    $installCommand = @"
set -euo pipefail
dnf install -y openssh-server nginx java-17-openjdk java-17-openjdk-devel wget unzip procps-ng which git >/dev/null
dnf install -y mysql-server >/dev/null
setcap -r /usr/libexec/mysqld >/dev/null 2>&1 || true
mkdir -p /var/run/sshd
ssh-keygen -A >/dev/null 2>&1 || true
sed -i 's/^#\?PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config
sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config
sed -i 's/^#\?PubkeyAuthentication.*/PubkeyAuthentication yes/' /etc/ssh/sshd_config
sed -i 's/^#\?UsePAM.*/UsePAM no/' /etc/ssh/sshd_config
printf '%s\n' $rootCredential | chpasswd
pkill -x sshd >/dev/null 2>&1 || true
/usr/sbin/sshd
"@
    Invoke-DockerExec $installCommand

    Write-Step 'Prepare container directories'
    Invoke-DockerExec 'mkdir -p /opt/nacos /opt/governance-demo/jars /opt/governance-demo/govern /opt/governance-demo/portal /opt/governance-demo/nacos-config /opt/governance-demo/logs /opt/governance-demo/run'

    Write-Step 'Copy Nacos runtime and project artifacts into container'
    Invoke-Docker @('cp', (Join-Path $nacosTempDir '.'), "${ContainerName}:/opt/nacos")
    Invoke-Docker @('cp', $gatewayJar, "${ContainerName}:/opt/governance-demo/jars/gateway.jar")
    Invoke-Docker @('cp', $authJar, "${ContainerName}:/opt/governance-demo/jars/auth-center.jar")
    Invoke-Docker @('cp', $bmsJar, "${ContainerName}:/opt/governance-demo/jars/bms-service.jar")
    Invoke-Docker @('cp', $dataSourceJar, "${ContainerName}:/opt/governance-demo/jars/data-source.jar")
    Invoke-Docker @('cp', $dataMetadataJar, "${ContainerName}:/opt/governance-demo/jars/data-metadata.jar")
    Invoke-Docker @('cp', $iotDeviceJar, "${ContainerName}:/opt/governance-demo/jars/iot-device.jar")
    Invoke-Docker @('cp', $iotCollectionJar, "${ContainerName}:/opt/governance-demo/jars/iot-collection.jar")
    Invoke-Docker @('cp', (Join-Path $governDistDir '.'), "${ContainerName}:/opt/governance-demo/govern")
    Invoke-Docker @('cp', (Join-Path $portalDistDir '.'), "${ContainerName}:/opt/governance-demo/portal")
    Invoke-Docker @('cp', (Join-Path $microserviceDir 'nacos-config\.'), "${ContainerName}:/opt/governance-demo/nacos-config")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'bootstrap.sh'), "${ContainerName}:/opt/governance-demo/run/bootstrap.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'start-services.sh'), "${ContainerName}:/opt/governance-demo/run/start-services.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'stop-services.sh'), "${ContainerName}:/opt/governance-demo/run/stop-services.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'service-manager.sh'), "${ContainerName}:/opt/governance-demo/run/service-manager.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'mail-catcher.py'), "${ContainerName}:/opt/governance-demo/run/mail-catcher.py")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'nginx.governance-demo.conf'), "${ContainerName}:/etc/nginx/conf.d/governance-demo.conf")

    Write-Step 'Generate runtime environment file'
    $runtimeEnv = @"
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3306
export MYSQL_USERNAME=$MysqlUsername
export MYSQL_PASSWORD=$(ConvertTo-BashSingleQuoted $MysqlPassword)
export NACOS_SERVER_ADDR=127.0.0.1:8848
export GATEWAY_TRUSTED_TOKEN=$(ConvertTo-BashSingleQuoted $GatewayToken)
export AUTH_CENTER_JWT_SECRET=$(ConvertTo-BashSingleQuoted $JwtSecret)
export AUTH_CENTER_JWT_EXPIRE_SECONDS=86400
export AUTH_CENTER_ADMIN_USERNAME=$AdminUsername
export AUTH_CENTER_ADMIN_PASSWORD=$(ConvertTo-BashSingleQuoted $AdminPassword)
export AUTH_CENTER_ADMIN_NICKNAME=$(ConvertTo-BashSingleQuoted $AdminNickname)
export APP_CORS_ALLOWED_ORIGIN_PATTERNS='http://localhost:*,http://127.0.0.1:*'
export SSH_ROOT_PASSWORD=$(ConvertTo-BashSingleQuoted $SshRootPassword)
export AUTH_CENTER_MAIL_MOCK_ENABLED='false'
export AUTH_CENTER_MAIL_DEBUG_RETURN_CODE='false'
export AUTH_CENTER_MAIL_FROM='no-reply@governance.local'
export MAIL_HOST='127.0.0.1'
export MAIL_PORT='1025'
export MAIL_SMTP_AUTH='false'
export MAIL_SMTP_STARTTLS_ENABLE='false'
"@
    Write-Utf8NoBom -Path $runtimeEnvPath -Content $runtimeEnv
    Invoke-Docker @('cp', $runtimeEnvPath, "${ContainerName}:/opt/governance-demo/run/runtime.env")

    Write-Step 'Normalize line endings for shell/runtime/config files'
    Invoke-DockerExec "sed -i 's/\r$//' /opt/governance-demo/run/*.sh /opt/governance-demo/run/*.py /opt/governance-demo/run/runtime.env /opt/governance-demo/nacos-config/*.yaml /etc/nginx/conf.d/governance-demo.conf"

    Write-Step 'Initialize MySQL schemas and users'
    $initSqlPath = Join-Path $tmpRoot 'init-mysql.sql'
    $initSql = @"
CREATE DATABASE IF NOT EXISTS bms_service_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS data_source_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS data_metadata_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS $(ConvertTo-SqlSingleQuoted $MysqlUsername)@'%' IDENTIFIED BY $(ConvertTo-SqlSingleQuoted $MysqlPassword);
ALTER USER $(ConvertTo-SqlSingleQuoted $MysqlUsername)@'%' IDENTIFIED BY $(ConvertTo-SqlSingleQuoted $MysqlPassword);
GRANT ALL PRIVILEGES ON bms_service_db.* TO $(ConvertTo-SqlSingleQuoted $MysqlUsername)@'%';
GRANT ALL PRIVILEGES ON data_source_db.* TO $(ConvertTo-SqlSingleQuoted $MysqlUsername)@'%';
GRANT ALL PRIVILEGES ON data_metadata_db.* TO $(ConvertTo-SqlSingleQuoted $MysqlUsername)@'%';
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY $(ConvertTo-SqlSingleQuoted $MysqlRootPassword);
ALTER USER 'root'@'%' IDENTIFIED BY $(ConvertTo-SqlSingleQuoted $MysqlRootPassword);
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
"@
    Write-Utf8NoBom -Path $initSqlPath -Content $initSql
    Invoke-Docker @('cp', $initSqlPath, "${ContainerName}:/tmp/init-mysql.sql")
    $mysqlInitCommand = @'
set -euo pipefail
setcap -r /usr/libexec/mysqld >/dev/null 2>&1 || true
mkdir -p /var/run/mysqld
chown -R mysql:mysql /var/run/mysqld /var/lib/mysql
if [ ! -d /var/lib/mysql/mysql ]; then
  mysqld --initialize-insecure --user=mysql --datadir=/var/lib/mysql
fi
if ! pgrep -x mysqld >/dev/null 2>&1; then
  mysqld --user=mysql --daemonize --datadir=/var/lib/mysql --bind-address=0.0.0.0 --socket=/var/run/mysqld/mysqld.sock
fi
for i in $(seq 1 60); do
  mysqladmin --socket=/var/run/mysqld/mysqld.sock ping >/dev/null 2>&1 && break
  sleep 1
done
mysql --socket=/var/run/mysqld/mysqld.sock -uroot < /tmp/init-mysql.sql
'@
    Invoke-DockerExec $mysqlInitCommand

    Write-Step 'Bootstrap Nacos, nginx, sshd and microservices'
    Invoke-DockerExec 'chmod +x /opt/governance-demo/run/bootstrap.sh /opt/governance-demo/run/start-services.sh /opt/governance-demo/run/stop-services.sh /opt/governance-demo/run/service-manager.sh /opt/governance-demo/run/mail-catcher.py && /opt/governance-demo/run/bootstrap.sh >/opt/governance-demo/logs/bootstrap.log 2>&1'

    if (-not $SkipValidation) {
        Write-Step 'Validate host-exposed ports and endpoints'
        Wait-TcpPort -Name 'SSH' -Port 12222
        Wait-TcpPort -Name 'MySQL' -Port 23306
        Wait-HttpOk -Name 'Nacos' -Url 'http://127.0.0.1:18848/nacos'
        Wait-HttpOk -Name 'Gateway health' -Url 'http://127.0.0.1:18080/actuator/health'
        Wait-HttpOk -Name 'Govern frontend' -Url 'http://127.0.0.1:19001'
        Wait-HttpOk -Name 'Portal frontend' -Url 'http://127.0.0.1:19002'
        Wait-HttpOk -Name 'Swagger' -Url 'http://127.0.0.1:18080/swagger-ui.html'

        Write-Step 'Validate auth-center captcha endpoint'
        $captchaResponse = Invoke-WebRequest -UseBasicParsing `
            -Method GET `
            -Uri 'http://127.0.0.1:18080/api/auth-center/captcha'
        $captchaPayload = $captchaResponse.Content | ConvertFrom-Json
        if (-not $captchaPayload.success) {
            throw 'Captcha validation failed'
        }

        Write-Step 'Validate SSH password login through host port'
        $sshCheckCommand = "dnf install -y sshpass >/dev/null && sshpass -p $(ConvertTo-BashSingleQuoted $SshRootPassword) ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p 12222 root@host.docker.internal 'echo SSH_OK'"
        Invoke-DockerExec $sshCheckCommand
    }

    Write-Success 'Single-container initialization completed'
    Write-Host ''
    Write-Host 'Access summary:' -ForegroundColor Yellow
    Write-Host '  SSH:      127.0.0.1:12222   root / Governance@2026SSH'
    Write-Host "  MySQL:    127.0.0.1:23306   $MysqlUsername / $MysqlPassword"
    Write-Host '  Govern:   http://127.0.0.1:19001'
    Write-Host '  Portal:   http://127.0.0.1:19002'
    Write-Host '  Gateway:  http://127.0.0.1:18080'
    Write-Host '  Nacos:    http://127.0.0.1:18848/nacos'
    Write-Host '  Swagger:  http://127.0.0.1:18080/swagger-ui.html'
}
finally {
    Remove-ContainerIfExists -Name $seedContainerName
    if ((-not $KeepTemp) -and (Test-Path $tmpRoot)) {
        Remove-Item -Recurse -Force $tmpRoot
    }
}
