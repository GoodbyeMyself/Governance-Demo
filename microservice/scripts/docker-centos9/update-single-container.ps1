# 单容器增量更新脚本。
# 适用于已经通过 init-single-container.ps1 初始化完成的环境；
# 当本地代码发生变更后，可使用本脚本重新构建产物并覆盖到现有容器中。
[CmdletBinding()]
param(
    [string]$ContainerName = 'governance-centos9',
    [switch]$SkipBuild,
    [switch]$BackendOnly,
    [switch]$FrontendOnly,
    [switch]$SkipValidation
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
    Invoke-Docker @('exec', $ContainerName, 'bash', '-lc', $Command)
}

function Test-ContainerRunning {
    param([string]$Name)
    $containerNames = (& docker ps --format '{{.Names}}')
    return $containerNames -contains $Name
}

function Wait-HttpOk {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TimeoutSeconds = 180
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
                Write-Success "$Name is ready: $Url"
                return
            }
        }
        catch {
        }
        Start-Sleep -Seconds 2
    }

    throw "$Name not ready after ${TimeoutSeconds}s: $Url"
}

function Update-DirectoryInContainer {
    param(
        [Parameter(Mandatory = $true)][string]$LocalPath,
        [Parameter(Mandatory = $true)][string]$ContainerPath
    )

    Invoke-DockerExec "mkdir -p '$ContainerPath' && rm -rf '$ContainerPath'/*"
    Invoke-Docker @('cp', (Join-Path $LocalPath '.'), "${ContainerName}:${ContainerPath}")
}

if ($BackendOnly -and $FrontendOnly) {
    throw 'BackendOnly and FrontendOnly cannot be used together.'
}

Assert-Command -Name 'docker'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = (Resolve-Path (Join-Path $scriptDir '..\..\..')).Path
$microserviceDir = Join-Path $repoRoot 'microservice'
$webDir = Join-Path $repoRoot 'web'

$updateBackend = -not $FrontendOnly
$updateFrontend = -not $BackendOnly

if (-not (Test-ContainerRunning -Name $ContainerName)) {
    throw "Container '$ContainerName' is not running. Please initialize the environment first."
}

if (-not $SkipBuild) {
    if ($updateBackend) {
        Assert-Command -Name 'mvn'
        Write-Step 'Build backend jars'
        Invoke-External -FilePath 'mvn' -Arguments @('-DskipTests', 'package') -WorkingDirectory $microserviceDir
    }

    if ($updateFrontend) {
        Assert-Command -Name 'pnpm'
        Write-Step 'Build frontend dist'
        Invoke-External -FilePath 'pnpm' -Arguments @('build') -WorkingDirectory $webDir
    }
}

if ($updateBackend) {
    Write-Step 'Update backend jars and runtime scripts'

    $jarMappings = @(
        @{ Local = 'gateway\target\gateway-0.0.1-SNAPSHOT.jar'; Remote = '/opt/governance-demo/jars/gateway.jar' },
        @{ Local = 'service\auth-center\target\auth-center-0.0.1-SNAPSHOT.jar'; Remote = '/opt/governance-demo/jars/auth-center.jar' },
        @{ Local = 'service\bms-service\target\bms-service-0.0.1-SNAPSHOT.jar'; Remote = '/opt/governance-demo/jars/bms-service.jar' },
        @{ Local = 'service\data-source\target\data-source-0.0.1-SNAPSHOT.jar'; Remote = '/opt/governance-demo/jars/data-source.jar' },
        @{ Local = 'service\data-metadata\target\data-metadata-0.0.1-SNAPSHOT.jar'; Remote = '/opt/governance-demo/jars/data-metadata.jar' }
    )

    foreach ($jar in $jarMappings) {
        $localJarPath = Join-Path $microserviceDir $jar.Local
        if (-not (Test-Path $localJarPath)) {
            throw "Jar not found: $localJarPath"
        }
        Invoke-Docker @('cp', $localJarPath, "${ContainerName}:$($jar.Remote)")
    }

    Invoke-DockerExec "mkdir -p /opt/governance-demo/run /opt/governance-demo/nacos-config"
    Invoke-Docker @('cp', (Join-Path $scriptDir 'bootstrap.sh'), "${ContainerName}:/opt/governance-demo/run/bootstrap.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'start-services.sh'), "${ContainerName}:/opt/governance-demo/run/start-services.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'stop-services.sh'), "${ContainerName}:/opt/governance-demo/run/stop-services.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'service-manager.sh'), "${ContainerName}:/opt/governance-demo/run/service-manager.sh")
    Invoke-Docker @('cp', (Join-Path $scriptDir 'mail-catcher.py'), "${ContainerName}:/opt/governance-demo/run/mail-catcher.py")

    Update-DirectoryInContainer `
        -LocalPath (Join-Path $microserviceDir 'nacos-config') `
        -ContainerPath '/opt/governance-demo/nacos-config'

    Write-Step 'Restart backend services and republish Nacos config'
    Invoke-DockerExec @'
chmod +x /opt/governance-demo/run/bootstrap.sh \
         /opt/governance-demo/run/start-services.sh \
         /opt/governance-demo/run/stop-services.sh \
         /opt/governance-demo/run/service-manager.sh \
         /opt/governance-demo/run/mail-catcher.py
/opt/governance-demo/run/bootstrap.sh >/opt/governance-demo/logs/bootstrap.log 2>&1
'@
}

if ($updateFrontend) {
    Write-Step 'Update frontend static assets'
    Update-DirectoryInContainer `
        -LocalPath (Join-Path $webDir 'apps\govern\dist') `
        -ContainerPath '/opt/governance-demo/govern'
    Update-DirectoryInContainer `
        -LocalPath (Join-Path $webDir 'apps\portal\dist') `
        -ContainerPath '/opt/governance-demo/portal'
    Invoke-DockerExec 'nginx -s reload'
}

if (-not $SkipValidation) {
    Write-Step 'Validate updated environment'
    Wait-HttpOk -Name 'Gateway health' -Url 'http://127.0.0.1:18080/actuator/health'
    Wait-HttpOk -Name 'Govern frontend' -Url 'http://127.0.0.1:19001'
    Wait-HttpOk -Name 'Portal frontend' -Url 'http://127.0.0.1:19002'
    Wait-HttpOk -Name 'Auth-center captcha' -Url 'http://127.0.0.1:18080/api/auth-center/captcha'
}

Write-Success 'Incremental update completed'
Write-Host ''
Write-Host 'Updated targets:' -ForegroundColor Yellow
if ($updateBackend) {
    Write-Host '  - Backend jars'
    Write-Host '  - Runtime scripts'
    Write-Host '  - Nacos config'
}
if ($updateFrontend) {
    Write-Host '  - Govern static files'
    Write-Host '  - Portal static files'
}
Write-Host ''
Write-Host 'Common commands:' -ForegroundColor Yellow
Write-Host "  Full update:    powershell -ExecutionPolicy Bypass -File `"$scriptDir\update-single-container.ps1`""
Write-Host "  Backend only:   powershell -ExecutionPolicy Bypass -File `"$scriptDir\update-single-container.ps1`" -BackendOnly"
Write-Host "  Frontend only:  powershell -ExecutionPolicy Bypass -File `"$scriptDir\update-single-container.ps1`" -FrontendOnly"
Write-Host "  Skip rebuild:   powershell -ExecutionPolicy Bypass -File `"$scriptDir\update-single-container.ps1`" -SkipBuild"
