param(
    [switch]$Build,
    [switch]$StartNacos,
    [switch]$SkipInfraCheck,
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"

$rootDir = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$logDir = Join-Path $rootDir "logs"
$pidFile = Join-Path $logDir "local-dev-pids.json"
$composeFile = Join-Path $PSScriptRoot "docker-compose.nacos.yml"

$services = @(
    @{ Name = "bms-service"; Dir = "service/bms-service"; Port = 8082 },
    @{ Name = "data-source"; Dir = "service/data-source"; Port = 8083 },
    @{ Name = "data-metadata"; Dir = "service/data-metadata"; Port = 8084 },
    @{ Name = "iot-device"; Dir = "service/iot-device"; Port = 8085 },
    @{ Name = "iot-collection"; Dir = "service/iot-collection"; Port = 8086 },
    @{ Name = "auth-center"; Dir = "service/auth-center"; Port = 8081 },
    @{ Name = "gateway"; Dir = "gateway"; Port = 8080 }
)

function Test-PortListening {
    param([int]$Port)
    $conn = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
    return $null -ne $conn
}

function Test-TcpPort {
    param(
        [string]$HostName = "127.0.0.1",
        [int]$Port,
        [int]$TimeoutMs = 1200
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $connectTask = $client.ConnectAsync($HostName, $Port)
        if (-not $connectTask.Wait($TimeoutMs)) {
            return $false
        }
        return $client.Connected
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Wait-TcpPort {
    param(
        [string]$HostName = "127.0.0.1",
        [int]$Port,
        [int]$Timeout = 120
    )

    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpPort -HostName $HostName -Port $Port) {
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

function Ensure-NacosReady {
    param([switch]$ForceStart)

    $nacosHttpReady = Test-TcpPort -Port 8848
    $nacosGrpcReady = Test-TcpPort -Port 9848

    if ($ForceStart -or -not ($nacosHttpReady -and $nacosGrpcReady)) {
        $nacosContainerName = "governance-nacos"
        $nacosState = ""

        try {
            $nacosState = (& docker inspect $nacosContainerName --format "{{.State.Status}}" 2>$null).Trim()
        } catch {
            $nacosState = ""
        }

        if ($nacosState -in @("restarting", "exited", "dead")) {
            Write-Host "[precheck] Removing unhealthy Nacos container ($nacosState)..."
            & docker rm -f $nacosContainerName | Out-Null
        }

        Write-Host "[precheck] Starting Nacos by docker compose..."
        Push-Location $rootDir
        try {
            docker compose -f $composeFile up -d
        } finally {
            Pop-Location
        }
    }

    Write-Host "[precheck] Waiting Nacos HTTP port 8848..."
    if (-not (Wait-TcpPort -Port 8848 -Timeout 120)) {
        throw "[precheck] Nacos port 8848 is not ready. Please check docker and Nacos container logs."
    }

    Write-Host "[precheck] Waiting Nacos gRPC port 9848..."
    if (-not (Wait-TcpPort -Port 9848 -Timeout 120)) {
        throw "[precheck] Nacos port 9848 is not ready. Please check docker and Nacos container logs."
    }

    Write-Host "[precheck] Nacos is ready."
}

function Wait-Health {
    param(
        [string]$Url,
        [int]$Timeout
    )

    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        try {
            $resp = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 3
            if ($resp.status -eq "UP") {
                return $true
            }
        } catch {
            $statusCode = $null
            if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
                $statusCode = [int]$_.Exception.Response.StatusCode
            }
            if ($statusCode -ne $null) {
                return $true
            }
            Start-Sleep -Seconds 2
            continue
        }
        Start-Sleep -Seconds 1
    }
    return $false
}

function Ensure-SharedSupportInstalled {
    Write-Host "[precheck] Installing service-support to local Maven repository..."
    Push-Location $rootDir
    try {
        mvn -pl common/service-support -am -DskipTests install
    } finally {
        Pop-Location
    }
}

if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

if ($Build) {
    Write-Host "[start] Building all modules..."
    Push-Location $rootDir
    try {
        mvn -DskipTests clean package
    } finally {
        Pop-Location
    }
}

if (-not $SkipInfraCheck) {
    Ensure-NacosReady -ForceStart:$StartNacos

    $checkLocalMySql = $true
    if (-not [string]::IsNullOrWhiteSpace($env:DB_URL)) {
        if ($env:DB_URL -notmatch "localhost:3306|127\.0\.0\.1:3306") {
            $checkLocalMySql = $false
        }
    }

    if ($checkLocalMySql) {
        Write-Host "[precheck] Checking MySQL port 3306..."
        if (-not (Test-TcpPort -Port 3306 -TimeoutMs 1200)) {
            throw "[precheck] MySQL 3306 is unreachable. Please start MySQL or set DB_URL/DB_USERNAME/DB_PASSWORD to a reachable instance."
        }
    }
}

Ensure-SharedSupportInstalled

$records = @()

foreach ($svc in $services) {
    $name = $svc.Name
    $dir = $svc.Dir
    $port = [int]$svc.Port
    $svcDir = Join-Path $rootDir $dir
    $stdout = Join-Path $logDir "$name.out.log"
    $stderr = Join-Path $logDir "$name.err.log"
    $healthUrl = "http://localhost:$port/actuator/health"

    if (-not (Test-Path $svcDir)) {
        throw "Service directory not found: $svcDir"
    }

    if (Test-PortListening -Port $port) {
        Write-Host "[start] ${name}: port $port already in use, skip process start."
        $records += [pscustomobject]@{
            name = $name
            port = $port
            pid  = $null
            reused = $true
            healthUrl = $healthUrl
            outLog = $stdout
            errLog = $stderr
        }
        continue
    }

    Write-Host "[start] ${name}: starting..."
    $proc = Start-Process `
        -FilePath "mvn" `
        -ArgumentList "spring-boot:run" `
        -WorkingDirectory $svcDir `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -WindowStyle Hidden `
        -PassThru

    $records += [pscustomobject]@{
        name = $name
        port = $port
        pid  = $proc.Id
        reused = $false
        healthUrl = $healthUrl
        outLog = $stdout
        errLog = $stderr
    }
}

$records | ConvertTo-Json -Depth 4 | Set-Content -Path $pidFile -Encoding UTF8
Write-Host "[start] PID file written: $pidFile"

$anyFailed = $false
foreach ($item in $records) {
    Write-Host "[check] Waiting health: $($item.name) -> $($item.healthUrl)"
    $ok = Wait-Health -Url $item.healthUrl -Timeout $TimeoutSeconds
    if ($ok) {
        Write-Host "[ok] $($item.name) is UP"
    } else {
        $anyFailed = $true
        Write-Host "[fail] $($item.name) health check timeout. See logs:"
        Write-Host "       $($item.outLog)"
        Write-Host "       $($item.errLog)"
    }
}

if ($anyFailed) {
    exit 1
}

Write-Host "[done] All services are UP."
