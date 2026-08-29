# Validates version alignment between docs/release-history.json and app/build.gradle.kts
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$historyPath = Join-Path $root "docs\release-history.json"
$gradlePath = Join-Path $root "app\build.gradle.kts"

$history = Get-Content $historyPath -Raw | ConvertFrom-Json
$gradle = Get-Content $gradlePath -Raw

if ($gradle -notmatch 'versionCode\s*=\s*(\d+)') {
    Write-Error "versionCode not found in app/build.gradle.kts"
}
$gradleCode = [int]$Matches[1]

if ($gradle -notmatch 'versionName\s*=\s*"([^"]+)"') {
    Write-Error "versionName not found in app/build.gradle.kts"
}
$gradleName = $Matches[1]

if ($history.versionCode -ne $gradleCode) {
    Write-Error "versionCode mismatch: release-history=$($history.versionCode) gradle=$gradleCode"
}
if ($history.currentVersion -ne $gradleName) {
    Write-Error "versionName mismatch: release-history=$($history.currentVersion) gradle=$gradleName"
}

Write-Host "release-check OK: $gradleName ($gradleCode)"
