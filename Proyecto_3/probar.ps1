$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & (Join-Path $PSScriptRoot 'compilar.ps1')
    & javac --release 11 --add-modules jdk.httpserver -encoding UTF-8 -cp 'bin;lib/lanterna-3.1.2.jar' -d bin tests/PruebasProyecto3.java
    if ($LASTEXITCODE -ne 0) { throw 'No se compilaron las pruebas.' }
    & java --add-modules jdk.httpserver '-Djava.awt.headless=true' -cp 'bin;lib/lanterna-3.1.2.jar' PruebasProyecto3
    if ($LASTEXITCODE -ne 0) { throw 'Las pruebas fallaron.' }
} finally { Pop-Location }
