$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not (Test-Path -LiteralPath 'lib/lanterna-3.1.2.jar')) {
        throw 'Falta lib/lanterna-3.1.2.jar. Consulta README.md.'
    }
    New-Item -ItemType Directory -Force -Path bin | Out-Null
    $fuentes = Get-ChildItem -LiteralPath src -Filter '*.java' | ForEach-Object FullName
    & javac --release 11 --add-modules jdk.httpserver -encoding UTF-8 -cp 'lib/lanterna-3.1.2.jar' -d bin @fuentes
    if ($LASTEXITCODE -ne 0) { throw 'La compilacion fallo.' }
    Write-Host 'Compilacion correcta. Abre dos terminales y ejecuta servidor.ps1 y cliente.ps1.'
} finally { Pop-Location }
