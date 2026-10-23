param([string]$Servidor = 'http://localhost:8080', [switch]$TerminalTexto)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not (Test-Path -LiteralPath 'bin/ClienteAsteroides.class')) { throw 'Ejecuta primero .\compilar.ps1' }
    $opciones = @()
    if ($TerminalTexto) { $opciones += '--terminal' }
    & java -cp 'bin;lib/lanterna-3.1.2.jar' ClienteAsteroides $Servidor @opciones
    if ($LASTEXITCODE -ne 0) { throw 'El cliente termino con error.' }
} finally { Pop-Location }
