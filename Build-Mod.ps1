param(
    [ValidateSet('All','Forge','Fabric','NeoForge')][string]$Loader = 'All',
    [ValidateSet('All','1.20.1','1.21.1','1.21.4')][string]$Minecraft = 'All',
    [switch]$Test
)
$ErrorActionPreference = 'Stop'
$version = (Get-Content (Join-Path $PSScriptRoot 'gradle.properties') | Where-Object { $_ -match '^mod_version=' }) -replace '^mod_version=', ''
$variants = @(
    @{ Loader='Forge'; Minecraft='1.20.1'; Project='.'; Java=17 },
    @{ Loader='Fabric'; Minecraft='1.20.1'; Project='fabric'; Java=17 },
    @{ Loader='Fabric'; Minecraft='1.21.1'; Project='fabric-modern'; Java=21 },
    @{ Loader='Fabric'; Minecraft='1.21.4'; Project='fabric-modern'; Java=21 },
    @{ Loader='NeoForge'; Minecraft='1.21.1'; Project='neoforge'; Java=21 },
    @{ Loader='NeoForge'; Minecraft='1.21.4'; Project='neoforge'; Java=21 }
) | Where-Object { ($Loader -eq 'All' -or $_.Loader -eq $Loader) -and ($Minecraft -eq 'All' -or $_.Minecraft -eq $Minecraft) }
if (!$variants) { throw 'This loader/Minecraft combination is not supported.' }
$previousJava = $env:JAVA_HOME
$previousGradle = $env:GRADLE_USER_HOME
$outputDirectory = Join-Path $PSScriptRoot 'dist'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
try {
    foreach ($variant in $variants) {
        $jdkRoot = Join-Path $PSScriptRoot "..\.tools\jdk$($variant.Java)"
        $jdk = Get-ChildItem -LiteralPath $jdkRoot -Directory -ErrorAction SilentlyContinue | Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } | Select-Object -First 1
        if ($jdk) { $env:JAVA_HOME = $jdk.FullName }
        else {
            $candidate = [Environment]::GetEnvironmentVariable("JAVA$($variant.Java)_HOME")
            if ($candidate) { $env:JAVA_HOME = $candidate }
            else { $env:JAVA_HOME = $previousJava }
        }
        if (!$previousGradle -and (Test-Path (Join-Path $PSScriptRoot '..\.tools\gradle-cache'))) {
            $env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '..\.tools\gradle-cache'
        }
        $project = Join-Path $PSScriptRoot $variant.Project
        $wrapper = Join-Path $PSScriptRoot 'gradlew.bat'
        if ($variant.Project -eq 'fabric-modern') { $wrapper = Join-Path $project 'gradlew.bat' }
        $arguments = @('-p', $project, 'build', '--console=plain')
        if ($variant.Java -eq 21) { $arguments += "-PmcVersion=$($variant.Minecraft)" }
        if ($Test) { $arguments += $(if ($variant.Loader -eq 'Fabric') { 'runGametest' } else { 'runGameTestServer' }) }
        & $wrapper @arguments
        if ($LASTEXITCODE -ne 0) { throw "Build or tests failed: $($variant.Loader) $($variant.Minecraft)" }
        $loaderName = $variant.Loader.ToLowerInvariant()
        $name = "enchant_upgrades-$loaderName-$($variant.Minecraft)-$version.jar"
        $source = if ($variant.Loader -eq 'Forge') { Join-Path $project "build\libs\enchant_upgrades-$version.jar" }
                  elseif ($variant.Java -eq 17) { Join-Path $project "build\libs\$name" }
                  else { Join-Path $project "build\$($variant.Minecraft)\libs\$name" }
        Copy-Item -LiteralPath $source -Destination (Join-Path $outputDirectory $name)
        Write-Output (Join-Path $outputDirectory $name)
    }
    Get-ChildItem -LiteralPath $outputDirectory -Filter "*-$version.jar" | Sort-Object Name | ForEach-Object {
        '{0}  {1}' -f (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant(), $_.Name
    } | Set-Content -LiteralPath (Join-Path $outputDirectory "SHA256SUMS-$version.txt") -Encoding ASCII
} finally {
    $env:JAVA_HOME = $previousJava
    $env:GRADLE_USER_HOME = $previousGradle
}
