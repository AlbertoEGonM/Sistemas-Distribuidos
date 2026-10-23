$ErrorActionPreference = 'Stop'
$nginxFolder = Join-Path $PSScriptRoot 'nginx/runtime/nginx-1.28.0'
$config = Join-Path $PSScriptRoot 'nginx/nginx.conf'
if (Test-Path "$nginxFolder/logs/nginx.pid") {
    & "$nginxFolder/nginx.exe" -p ($nginxFolder.Replace('\','/') + '/') -c $config.Replace('\','/') -s quit
}
$state = Join-Path $PSScriptRoot 'evidencias/procesos.json'
if (Test-Path $state) {
    foreach ($entry in @(Get-Content $state -Raw | ConvertFrom-Json)) {
        $p = Get-CimInstance Win32_Process -Filter "ProcessId = $($entry.id)" -ErrorAction SilentlyContinue
        if ($p -and $p.Name -eq 'java.exe' -and $p.CommandLine.Contains((Join-Path $PSScriptRoot 'classes'))) {
            Stop-Process -Id $entry.id
        }
    }
    Remove-Item -LiteralPath $state
}
Write-Host 'Procesos del proyecto detenidos.'
