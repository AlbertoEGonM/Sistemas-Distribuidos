$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force classes | Out-Null
    $sources = Get-ChildItem -LiteralPath source -Filter '*.java' | ForEach-Object FullName
    & javac -encoding UTF-8 -d classes $sources
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion' }
    & jar cfe classes/Application.jar Application -C classes Application.class -C classes Aggregator.class -C classes 'Aggregator$State.class' -C classes 'Aggregator$Result.class' -C classes FuerzaBrutaHash.class -C classes 'FuerzaBrutaHash$Resultado.class' -C classes networking
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la creacion del JAR' }
    Write-Host 'Compilacion terminada. JAR: classes/Application.jar'
} finally { Pop-Location }
