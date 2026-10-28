param([int]$Puerto = 8080)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
 New-Item -ItemType Directory -Force classes | Out-Null
 & javac -encoding UTF-8 -cp 'lib/*' -d classes source/Alumno.java source/WebServer.java
 if ($LASTEXITCODE -ne 0) { throw 'Error de compilacion' }
 & java -cp 'classes;lib/*' WebServer $Puerto
} finally { Pop-Location }
