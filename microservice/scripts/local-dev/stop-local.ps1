param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$rootDir = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$pidFile = Join-Path (Join-Path $rootDir "logs") "local-dev-pids.json"

if (-not (Test-Path $pidFile)) {
    Write-Host "[stop] PID file not found: $pidFile"
    exit 0
}

$records = Get-Content -Raw $pidFile | ConvertFrom-Json
if ($records -isnot [System.Array]) {
    $records = @($records)
}

foreach ($item in $records) {
    if ($null -eq $item.pid) {
        Write-Host "[stop] $($item.name): no pid recorded (reused process), skip."
        continue
    }

    $procId = [int]$item.pid
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    if ($null -eq $proc) {
        Write-Host "[stop] $($item.name): pid $procId not running."
        continue
    }

    Write-Host "[stop] $($item.name): stopping pid $procId ..."
    if ($Force) {
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    } else {
        Stop-Process -Id $procId -ErrorAction SilentlyContinue
    }
}

Remove-Item -Force $pidFile
Write-Host "[done] Stop finished."
