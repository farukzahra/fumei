# Build debug APK, verify version, copy to PC Downloads, push to phone Downloads when USB is available.
param(
    [string]$Destination = "$env:USERPROFILE\Downloads\fumei-debug.apk",
    [string]$PhoneFileName = "fumei-debug.apk"
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

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"

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

function Get-PhysicalPhoneSerial {
    if (-not (Test-Path $adb)) {
        return $null
    }

    $lines = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\tdevice$' }
    $physical = @()
    foreach ($line in $lines) {
        $serial = ($line -split '\t')[0].Trim()
        if ($serial -and $serial -notmatch '^emulator-') {
            $physical += $serial
        }
    }

    if ($physical.Count -eq 1) {
        return $physical[0]
    }
    if ($physical.Count -gt 1) {
        Write-Warning "Mais de um celular USB detectado: $($physical -join ', '). Use ANDROID_SERIAL ou desconecte extras."
    }
    return $null
}

function Send-ApkViaMtp {
    param(
        [string]$ApkPath,
        [string]$RemoteFileName,
        [string]$DeviceNamePattern = "S24|Galaxy|Samsung|faruk"
    )

    $localSize = (Get-Item $ApkPath).Length
    $shell = New-Object -ComObject Shell.Application
    $pc = $shell.NameSpace(0x11)
    $phone = $pc.Items() | Where-Object { $_.Name -match $DeviceNamePattern } | Select-Object -First 1
    if (-not $phone) {
        return $null
    }

    $storage = $phone.GetFolder().Items() | Where-Object {
        $_.Name -match 'Armazenamento interno|Internal storage'
    } | Select-Object -First 1
    if (-not $storage) {
        throw "Armazenamento interno nao encontrado em $($phone.Name)"
    }

    $download = $storage.GetFolder().Items() | Where-Object { $_.Name -eq 'Download' } | Select-Object -First 1
    if (-not $download) {
        throw "Pasta Download nao encontrada no celular $($phone.Name)"
    }

    $destFolder = $download.GetFolder()
    $destFolder.CopyHere($ApkPath, 0x14)

    for ($i = 1; $i -le 60; $i++) {
        Start-Sleep -Seconds 2
        $found = $destFolder.Items() | Where-Object { $_.Name -eq $RemoteFileName } | Select-Object -First 1
        if ($found) {
            $remoteSize = 0
            if ($found.Size -match '^\d+$') {
                $remoteSize = [long]$found.Size
            }
            if ($remoteSize -eq 0 -or $remoteSize -eq $localSize) {
                return @{
                    Serial = "mtp:$($phone.Name)"
                    Path = "Download/$RemoteFileName"
                    Size = if ($remoteSize -gt 0) { $remoteSize } else { $localSize }
                }
            }
        }
    }

    throw "Timeout ao copiar via MTP para $($phone.Name)"
}

function Send-ApkToPhoneDownloads {
    param(
        [string]$ApkPath,
        [string]$Serial,
        [string]$RemoteFileName
    )

    $phonePaths = @(
        "/sdcard/Download/$RemoteFileName",
        "/storage/emulated/0/Download/$RemoteFileName"
    )

    $localSize = (Get-Item $ApkPath).Length
    foreach ($phonePath in $phonePaths) {
        $remoteDir = Split-Path -Parent $phonePath
        & $adb -s $Serial shell "mkdir -p `"$remoteDir`"" | Out-Null
        & $adb -s $Serial push $ApkPath $phonePath
        if ($LASTEXITCODE -ne 0) {
            continue
        }

        $remoteSizeRaw = (& $adb -s $Serial shell "stat -c %s `"$phonePath`" 2>/dev/null || wc -c < `"$phonePath`"").Trim()
        $remoteSize = [long]($remoteSizeRaw -replace '\D', '')
        if ($remoteSize -eq $localSize) {
            & $adb -s $Serial shell "am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file://$phonePath" | Out-Null
            return @{
                Serial = $Serial
                Path = $phonePath
                Size = $remoteSize
            }
        }
    }

    throw "Falha ao enviar APK para Downloads do celular ($Serial)."
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

$phoneSerial = $env:ANDROID_SERIAL
if (-not $phoneSerial) {
    $phoneSerial = Get-PhysicalPhoneSerial
}

$phonePush = $null
if ($phoneSerial) {
    Write-Host "Enviando APK para Downloads do celular via adb ($phoneSerial)..."
    try {
        $phonePush = Send-ApkToPhoneDownloads -ApkPath $Destination -Serial $phoneSerial -RemoteFileName $PhoneFileName
        Write-Host "phone-push OK: $($phonePush.Path) ($($phonePush.Size) bytes)"
    }
    catch {
        Write-Warning $_.Exception.Message
    }
}

if (-not $phonePush) {
    Write-Host "Tentando envio via MTP (Explorer)..."
    try {
        $phonePush = Send-ApkViaMtp -ApkPath $Destination -RemoteFileName $PhoneFileName
        if ($phonePush) {
            Write-Host "mtp-copy OK: $($phonePush.Path) ($($phonePush.Size) bytes)"
        }
    }
    catch {
        Write-Warning $_.Exception.Message
    }
}

if (-not $phonePush) {
    Write-Warning "Celular nao recebeu o arquivo. Conecte USB (MTP ou Depuracao USB) e rode de novo."
}

$metaPath = "$Destination.meta.json"
@{
    generatedAt = (Get-Date).ToString("o")
    applicationId = "fumei.faruk.dev.br"
    versionCode = $versionCode
    versionName = $versionName
    sha256 = $apkHash
    sourceApk = (Resolve-Path $sourceApk).Path
    destinationApk = (Resolve-Path $Destination).Path
    phoneSerial = if ($phonePush) { $phonePush.Serial } else { $null }
    phoneDownloadPath = if ($phonePush) { $phonePush.Path } else { $null }
} | ConvertTo-Json -Depth 3 | Set-Content -Path $metaPath -Encoding UTF8

$item = Get-Item $Destination
Write-Host ""
Write-Host "APK pronto:"
Write-Host "  PC: $($item.FullName)"
Write-Host "  versao: $versionName (build $versionCode)"
Write-Host "  $([math]::Round($item.Length / 1MB, 1)) MB"
Write-Host "  meta: $metaPath"
if ($phonePush) {
    Write-Host "  celular: $($phonePush.Path)"
    Write-Host ""
    Write-Host "No celular: Arquivos > Download > $PhoneFileName. Instale e confira Mais > Sobre > Versao $versionName."
    Write-Host "Se Atualizar falhar, desinstale o Fumei e instale de novo (APK debug nao substitui build da Play Store)."
}
else {
    Write-Host ""
    Write-Host "Celular nao recebeu o arquivo. Conecte USB (modo arquivos/MTP) e rode o script novamente."
}
