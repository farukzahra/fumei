# Validates version alignment between docs/release-history.json, version-codes.json and app/build.gradle.kts
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$historyPath = Join-Path $root "docs\release-history.json"
$codesPath = Join-Path $root "docs\play-store\version-codes.json"
$gradlePath = Join-Path $root "app\build.gradle.kts"

$history = Get-Content $historyPath -Raw | ConvertFrom-Json
$codes = Get-Content $codesPath -Raw | ConvertFrom-Json
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

$currentEntry = $codes.releases | Where-Object { $_.playStore -eq "current" } | Select-Object -First 1
if (-not $currentEntry) {
    Write-Error "No playStore=current entry in docs/play-store/version-codes.json"
}
if ([int]$currentEntry.versionCode -ne $gradleCode) {
    Write-Error "versionCode mismatch: version-codes current=$($currentEntry.versionCode) gradle=$gradleCode"
}
if ($currentEntry.versionName -ne $gradleName) {
    Write-Error "versionName mismatch: version-codes current=$($currentEntry.versionName) gradle=$gradleName"
}
if ($gradleCode -le [int]$codes.playStoreMaxVersionCode) {
    Write-Error "versionCode $gradleCode must be > playStoreMaxVersionCode $($codes.playStoreMaxVersionCode). See docs/play-store/version-codes.md"
}

Write-Host "release-check OK: $gradleName ($gradleCode)"
