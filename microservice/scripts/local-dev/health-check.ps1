$services = @(
    @{ Name = "gateway"; Port = 8080 },
    @{ Name = "auth-center"; Port = 8081 },
    @{ Name = "bms-service"; Port = 8082 },
    @{ Name = "data-source"; Port = 8083 },
    @{ Name = "data-metadata"; Port = 8084 }
)

$results = @()
$failed = $false

foreach ($svc in $services) {
    $name = $svc.Name
    $port = [int]$svc.Port
    $url = "http://localhost:$port/actuator/health"
    $status = "DOWN"
    $message = ""

    try {
        $resp = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 3
        if ($resp -and $resp.status) {
            $status = $resp.status
        } else {
            $status = "UP(HTTP200)"
        }
    } catch {
        $statusCode = $null
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        if ($statusCode -ne $null) {
            if ($statusCode -eq 401 -or $statusCode -eq 403) {
                $status = "UP(AUTH)"
                $message = "health endpoint requires auth"
            } else {
                $status = "UP(HTTP$statusCode)"
                $message = "service is reachable but health endpoint did not return UP"
            }
        } else {
            $status = "DOWN"
            $message = $_.Exception.Message
        }
    }

    if ($status -like "DOWN*") {
        $failed = $true
    }

    $results += [pscustomobject]@{
        Service = $name
        Port = $port
        Status = $status
        Url = $url
        Message = $message
    }
}

$results | Format-Table -AutoSize

if ($failed) {
    exit 1
}

exit 0
