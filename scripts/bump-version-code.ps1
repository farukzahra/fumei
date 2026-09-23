# Sempre incrementa versionCode (+1) em Gradle, release-history e version-codes.json.
# Chamado automaticamente por play-release.ps1 antes do build.
param(
    [string]$VersionName,
    [int]$VersionCode
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$gradlePath = Join-Path $repoRoot "app\build.gradle.kts"
$historyPath = Join-Path $repoRoot "docs\release-history.json"
$codesPath = Join-Path $repoRoot "docs\play-store\version-codes.json"

$gradle = [System.IO.File]::ReadAllText($gradlePath)
if ($gradle -notmatch 'versionCode\s*=\s*(\d+)') { throw "versionCode not found in build.gradle.kts" }
$gradleCode = [int]$Matches[1]
if ($gradle -notmatch 'versionName\s*=\s*"([^"]+)"') { throw "versionName not found in build.gradle.kts" }
$gradleName = $Matches[1]

if ($VersionName) { $gradleName = $VersionName }
if ($VersionCode -gt 0) {
    $newCode = $VersionCode
} else {
    $codes = Get-Content $codesPath -Raw | ConvertFrom-Json
    $maxUsed = [int]$codes.playStoreMaxVersionCode
    $newCode = [Math]::Max($gradleCode, $maxUsed) + 1
}

if ($newCode -le $gradleCode -and $VersionCode -le 0) {
    throw "Refusing to bump: new versionCode $newCode must be > current $gradleCode"
}

Write-Host "Bumping versionCode: $gradleCode -> $newCode (versionName $gradleName)"

$gradle = $gradle -replace 'versionCode\s*=\s*\d+', "versionCode = $newCode"
$gradle = $gradle -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$gradleName`""
[System.IO.File]::WriteAllText($gradlePath, $gradle, [System.Text.UTF8Encoding]::new($false))

$history = Get-Content $historyPath -Raw | ConvertFrom-Json
$history.currentVersion = $gradleName
$history.versionCode = $newCode
$history.updatedAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")

$top = $history.entries[0]
if ($top.version -eq $gradleName) {
    $top.versionCode = $newCode
} else {
    $newEntry = [ordered]@{
        version = $gradleName
        versionCode = $newCode
        date = (Get-Date).ToString("yyyy-MM-dd")
        title = "Atualização $gradleName"
        summary = "Nova build para a Play Store (versionCode $newCode)."
        type = "chore"
        commit = $null
    }
    $history.entries = @($newEntry) + @($history.entries)
}

$historyJson = $history | ConvertTo-Json -Depth 6
[System.IO.File]::WriteAllText($historyPath, $historyJson, [System.Text.UTF8Encoding]::new($false))

$codesObj = Get-Content $codesPath -Raw | ConvertFrom-Json
foreach ($r in $codesObj.releases) {
    if ($r.playStore -eq "current") {
        $r.playStore = "consumed"
        if (-not $r.note) { $r.note = "Substituído por bump automático." }
    }
}
$newRelease = [pscustomobject]@{
    versionCode = $newCode
    versionName = $gradleName
    date = (Get-Date).ToString("yyyy-MM-dd")
    playStore = "current"
    note = "Gerado por bump-version-code.ps1 / play-release.ps1"
}
$codesObj.releases = @($codesObj.releases) + @($newRelease)
$codesObj.playStoreMaxVersionCode = $newCode - 1
$codesObj.nextVersionCode = $newCode + 1
$codesObj.publicVersionName = $gradleName
$codesObj.publicLaunchVersionCode = $newCode
$codesObj.updatedAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")

$codesJson = $codesObj | ConvertTo-Json -Depth 6
[System.IO.File]::WriteAllText($codesPath, $codesJson, [System.Text.UTF8Encoding]::new($false))

Write-Host "versionCode bumped to $newCode"
