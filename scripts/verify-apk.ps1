# Verifies APK version/package matches app/build.gradle.kts and optional copy integrity.
param(
    [Parameter(Mandatory = $true)]
    [string]$ApkPath,

    [string]$ExpectedApkPath = "",

    [switch]$RequireCopyMatch
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$gradlePath = Join-Path $repoRoot "app\build.gradle.kts"

if (-not (Test-Path $ApkPath)) {
    throw "APK not found: $ApkPath"
}

$gradle = Get-Content $gradlePath -Raw
if ($gradle -notmatch 'applicationId\s*=\s*"([^"]+)"') {
    throw "applicationId not found in app/build.gradle.kts"
}
$expectedAppId = $Matches[1]

if ($gradle -notmatch 'versionCode\s*=\s*(\d+)') {
    throw "versionCode not found in app/build.gradle.kts"
}
$expectedCode = [int]$Matches[1]

if ($gradle -notmatch 'versionName\s*=\s*"([^"]+)"') {
    throw "versionName not found in app/build.gradle.kts"
}
$expectedName = $Matches[1]

$androidHome = $env:ANDROID_HOME
if (-not $androidHome) {
    $androidHome = Join-Path $env:LOCALAPPDATA "Android\Sdk"
}
$buildTools = Get-ChildItem (Join-Path $androidHome "build-tools") -Directory |
    Sort-Object Name -Descending |
    Select-Object -First 1
if (-not $buildTools) {
    throw "Android build-tools not found under $androidHome"
}
$aapt = Join-Path $buildTools.FullName "aapt.exe"
if (-not (Test-Path $aapt)) {
    throw "aapt not found: $aapt"
}

$badging = & $aapt dump badging $ApkPath 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "aapt dump badging failed: $badging"
}

$packageLine = ($badging | Where-Object { $_ -like "package:*" } | Select-Object -First 1)
if (-not $packageLine) {
    throw "Could not read package line from APK badging"
}

$actualAppId = $null
$actualCode = $null
$actualName = $null
if ($packageLine -match "name='([^']+)'") { $actualAppId = $Matches[1] }
if ($packageLine -match "versionCode='([^']+)'") { $actualCode = [int]$Matches[1] }
if ($packageLine -match "versionName='([^']+)'") { $actualName = $Matches[1] }

$errors = @()
if ($actualAppId -ne $expectedAppId) {
    $errors += "applicationId: expected=$expectedAppId actual=$actualAppId"
}
if ($actualCode -ne $expectedCode) {
    $errors += "versionCode: expected=$expectedCode actual=$actualCode"
}
if ($actualName -ne $expectedName) {
    $errors += "versionName: expected=$expectedName actual=$actualName"
}
if ($errors.Count -gt 0) {
    throw ("APK verification failed:`n - " + ($errors -join "`n - "))
}

$apkHash = (Get-FileHash -Algorithm SHA256 -Path $ApkPath).Hash
$apkItem = Get-Item $ApkPath

$result = [ordered]@{
    apkPath = $apkItem.FullName
    applicationId = $actualAppId
    versionCode = $actualCode
    versionName = $actualName
    sha256 = $apkHash
    sizeBytes = $apkItem.Length
    lastWriteTime = $apkItem.LastWriteTime.ToString("o")
}

if ($ExpectedApkPath) {
    if (-not (Test-Path $ExpectedApkPath)) {
        throw "Expected APK not found for comparison: $ExpectedApkPath"
    }
    $expectedHash = (Get-FileHash -Algorithm SHA256 -Path $ExpectedApkPath).Hash
    $result.expectedApkPath = (Resolve-Path $ExpectedApkPath).Path
    $result.expectedSha256 = $expectedHash
    if ($RequireCopyMatch -and ($apkHash -ne $expectedHash)) {
        throw "APK copy mismatch: SHA256 differs between source and destination"
    }
}

Write-Host "apk-verify OK: $actualName ($actualCode) $actualAppId"
Write-Host "  path: $($apkItem.FullName)"
Write-Host "  sha256: $apkHash"

return $result
