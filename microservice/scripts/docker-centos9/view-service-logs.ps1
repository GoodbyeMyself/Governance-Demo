[CmdletBinding()]
param(
    [string]$Service = 'all',
    [int]$Lines = 200,
    [switch]$Follow,
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

if ($Lines -le 0) {
    throw 'Lines must be greater than 0.'
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Missing command: docker'
}

$followFlag = if ($Follow) { 'true' } else { 'false' }
$command = "/opt/governance-demo/run/service-manager.sh logs $Service $Lines $followFlag"
$arguments = @('exec')
if ($Follow) {
    $arguments += '-it'
}
$arguments += @($ContainerName, 'bash', '-lc', $command)
Invoke-External -FilePath 'docker' -Arguments $arguments
