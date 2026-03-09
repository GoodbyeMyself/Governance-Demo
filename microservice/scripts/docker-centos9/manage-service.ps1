[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('list', 'status', 'start', 'stop', 'restart')]
    [string]$Action,

    [string]$Service = 'all',

    [string]$ContainerName = 'governance-centos9'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Invoke-External {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $FilePath $($Arguments -join ' ')"
    }
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Missing command: docker'
}

$command = "/opt/governance-demo/run/service-manager.sh $Action $Service"
Invoke-External -FilePath 'docker' -Arguments @('exec', $ContainerName, 'bash', '-lc', $command)
