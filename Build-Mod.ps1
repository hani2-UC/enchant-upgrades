$ErrorActionPreference = 'Stop'
$localJdk = Join-Path $PSScriptRoot '..\.tools\jdk17\jdk-17.0.20.1+1'
if (Test-Path -LiteralPath (Join-Path $localJdk 'bin\javac.exe')) {
    $env:JAVA_HOME = (Resolve-Path -LiteralPath $localJdk).Path
    $env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '..\.tools\gradle-cache'
}
& (Join-Path $PSScriptRoot 'gradlew.bat') -p $PSScriptRoot build --console=plain
if ($LASTEXITCODE -ne 0) { throw 'MOD build failed. Check the output above.' }
$outputDirectory = Join-Path $PSScriptRoot 'dist'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'build\libs\enchant_upgrades-1.0.0.jar') -Destination $outputDirectory
Write-Output (Join-Path $outputDirectory 'enchant_upgrades-1.0.0.jar')
