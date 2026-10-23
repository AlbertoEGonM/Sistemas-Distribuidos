$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'compilar.ps1')
$javaInfo = & java -XshowSettings:properties -version 2>&1 | Out-String
$javaHomeMatch = [regex]::Match($javaInfo, 'java.home = (.+)')
$javaExe = Join-Path $javaHomeMatch.Groups[1].Value.Trim() 'bin/java.exe'
$classPath = Join-Path $PSScriptRoot 'classes'
$evidence = Join-Path $PSScriptRoot 'evidencias'
New-Item -ItemType Directory -Force $evidence | Out-Null
$owned = @()
foreach ($port in @(8081,8082,8080,8090)) {
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
        throw "El puerto $port esta ocupado. Cierra la instancia anterior antes de iniciar."
    }
}
foreach ($port in @(8081,8082)) {
    $p = Start-Process -FilePath $javaExe -ArgumentList @('-cp', ('"' + $classPath + '"'), 'ServidorHTTP', $port) -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput "$evidence/worker-$port.log" -RedirectStandardError "$evidence/worker-$port-error.log"
    $owned += @{id=$p.Id;role="worker-$port"}
}
$p = Start-Process -FilePath $javaExe -ArgumentList @('-cp', ('"' + $classPath + '"'), 'WebServer') -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput "$evidence/dispatcher.log" -RedirectStandardError "$evidence/dispatcher-error.log"
$owned += @{id=$p.Id;role='dispatcher'}
$owned | ConvertTo-Json | Set-Content "$evidence/procesos.json"
$nginxFolder = Join-Path $PSScriptRoot 'nginx/runtime/nginx-1.28.0'
$nginxExe = Join-Path $nginxFolder 'nginx.exe'
$config = Join-Path $PSScriptRoot 'nginx/nginx.conf'
if (!(Test-Path $nginxExe)) { throw 'Falta NGINX. Consulta README.md para instalarlo.' }
& $nginxExe -t -p ($nginxFolder.Replace('\','/') + '/') -c $config.Replace('\','/')
if ($LASTEXITCODE -ne 0) { throw 'Configuracion NGINX invalida' }
Start-Process -FilePath $nginxExe -ArgumentList @('-p', ('"' + $nginxFolder.Replace('\','/') + '/"'), '-c', ('"' + $config.Replace('\','/') + '"')) -WorkingDirectory $nginxFolder -WindowStyle Hidden
Write-Host 'Sistema iniciado. Abre http://localhost:8090 y pulsa Cargar ejemplo.'
Write-Host 'Para ver el dispatcher: Get-Content ./evidencias/dispatcher.log -Wait'
Write-Host 'Para detener: ./detener.ps1'
