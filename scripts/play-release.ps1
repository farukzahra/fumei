# Build release AAB and print Play Console fields (arquivo, nome da versão, notas).
# SEMPRE incrementa versionCode (+1) antes do build — nunca reutilizar código na Play.
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$studioJbr = "C:\Program Files\Android\Android Studio\jbr"
if (Test-Path $studioJbr) {
    $env:JAVA_HOME = $studioJbr
    $env:PATH = "$studioJbr\bin;$env:PATH"
}

Write-Host "=== AUTO BUMP versionCode (+1) ==="
& powershell -File (Join-Path $repoRoot "scripts\bump-version-code.ps1")

& powershell -File (Join-Path $repoRoot "scripts\release-check.ps1")

$notesPath = Join-Path $repoRoot "docs\play-store\release-notes-pt-BR.txt"
$historyPath = Join-Path $repoRoot "docs\release-history.json"
$gradlePath = Join-Path $repoRoot "app\build.gradle.kts"
$aabPath = Join-Path $repoRoot "app\build\outputs\bundle\release\app-release.aab"
$manifestPath = Join-Path $repoRoot "docs\play-store\last-release.json"

$history = Get-Content $historyPath -Raw | ConvertFrom-Json
$gradle = Get-Content $gradlePath -Raw

if ($gradle -notmatch 'versionCode\s*=\s*(\d+)') { throw "versionCode not found" }
$versionCode = [int]$Matches[1]
if ($gradle -notmatch 'versionName\s*=\s*"([^"]+)"') { throw "versionName not found" }
$versionName = $Matches[1]

if (-not (Test-Path $notesPath)) {
    $top = $history.entries[0]
    $fallback = if ($top.summary) { $top.summary } else { $top.title }
    Set-Content -Path $notesPath -Value $fallback -Encoding UTF8
}

$releaseNotes = [System.IO.File]::ReadAllText($notesPath).Trim()

Write-Host "Building release AAB (versionCode $versionCode)..."
& .\gradlew test bundleRelease
if (-not (Test-Path $aabPath)) {
    throw "AAB not found after build: $aabPath"
}

$aab = Get-Item $aabPath
$manifest = @{
    generatedAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    versionName = $versionName
    versionCode = $versionCode
    aabPath = $aab.FullName
    aabSizeBytes = $aab.Length
    releaseNotes = $releaseNotes
    playConsole = @{
        arquivo = $aab.FullName
        nomeDaVersao = $versionName
        notasDaVersao = $releaseNotes
    }
}

$json = $manifest | ConvertTo-Json -Depth 4
[System.IO.File]::WriteAllText($manifestPath, $json, [System.Text.UTF8Encoding]::new($false))

Write-Host ""
Write-Host "========== PLAY CONSOLE =========="
Write-Host "versionCode: $versionCode"
Write-Host "Arquivo: $($aab.FullName)"
Write-Host "Nome da versao: $versionName"
Write-Host "Notas da versao:"
Write-Host $releaseNotes
Write-Host "=================================="
Write-Host "Manifest: $manifestPath"
