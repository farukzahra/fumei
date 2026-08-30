# Build debug APK, verify version, copy to Downloads, verify copy matches source.
param(
    [string]$Destination = "$env:USERPROFILE\Downloads\fumei-debug.apk"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if (-not $env:JAVA_HOME) {
    $studioJbr = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path $studioJbr) {
        $env:JAVA_HOME = $studioJbr
    }
}

function Invoke-ApkVerify {
    param(
        [string]$ApkPath,
        [string]$ExpectedApkPath = "",
        [switch]$RequireCopyMatch
    )
    $args = @("-ApkPath", $ApkPath)
    if ($ExpectedApkPath) { $args += @("-ExpectedApkPath", $ExpectedApkPath) }
    if ($RequireCopyMatch) { $args += "-RequireCopyMatch" }
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot "scripts\verify-apk.ps1") @args
    if ($LASTEXITCODE -ne 0) {
        throw "verify-apk failed for $ApkPath"
    }
}

Write-Host "Running release-check..."
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot "scripts\release-check.ps1")
if ($LASTEXITCODE -ne 0) { throw "release-check failed" }

Write-Host "Building debug APK..."
& .\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) {
    throw "assembleDebug failed with exit code $LASTEXITCODE"
}

$sourceApk = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $sourceApk)) {
    throw "APK not found: $sourceApk"
}

Write-Host "Verifying built APK..."
Invoke-ApkVerify -ApkPath $sourceApk

$destDir = Split-Path -Parent $Destination
if ($destDir -and -not (Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
}

Copy-Item $sourceApk $Destination -Force

Write-Host "Verifying copied APK..."
Invoke-ApkVerify -ApkPath $Destination -ExpectedApkPath $sourceApk -RequireCopyMatch

$gradle = Get-Content (Join-Path $repoRoot "app\build.gradle.kts") -Raw
$versionName = if ($gradle -match 'versionName\s*=\s*"([^"]+)"') { $Matches[1] } else { "?" }
$versionCode = if ($gradle -match 'versionCode\s*=\s*(\d+)') { [int]$Matches[1] } else { 0 }
$apkHash = (Get-FileHash -Algorithm SHA256 -Path $Destination).Hash

$metaPath = "$Destination.meta.json"
@{
    generatedAt = (Get-Date).ToString("o")
    applicationId = "fumei.faruk.dev.br"
    versionCode = $versionCode
    versionName = $versionName
    sha256 = $apkHash
    sourceApk = (Resolve-Path $sourceApk).Path
    destinationApk = (Resolve-Path $Destination).Path
} | ConvertTo-Json -Depth 3 | Set-Content -Path $metaPath -Encoding UTF8

$item = Get-Item $Destination
Write-Host ""
Write-Host "APK pronto para o celular:"
Write-Host "  $($item.FullName)"
Write-Host "  versao: $versionName (build $versionCode)"
Write-Host "  $([math]::Round($item.Length / 1MB, 1)) MB"
Write-Host "  meta: $metaPath"
Write-Host ""
Write-Host "No celular: abra o arquivo e instale. Em Mais > Sobre, confira Versao $versionName."
