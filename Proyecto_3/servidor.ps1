param([int]$Puerto = 8080)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not (Test-Path -LiteralPath 'bin/ServidorAsteroides.class')) { throw 'Ejecuta primero .\compilar.ps1' }
    & java --add-modules jdk.httpserver -cp bin ServidorAsteroides $Puerto
    if ($LASTEXITCODE -ne 0) { throw 'El servidor termino con error.' }
} finally { Pop-Location }
