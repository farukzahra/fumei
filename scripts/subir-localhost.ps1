#Requires -Version 5.1
$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
$emu = Join-Path $env:ANDROID_HOME "emulator\emulator.exe"
$repoRoot = Split-Path -Parent $PSScriptRoot

Set-Location $repoRoot

$devices = & $adb devices | Select-String "`tdevice$"
if (-not $devices) {
    Start-Process $emu -ArgumentList "-avd", "Pixel6"
    & $adb wait-for-device
    do { Start-Sleep 3 } until ((& $adb shell getprop sys.boot_completed 2>$null) -match "1")
}

$serials = @(& $adb devices | Select-String "`tdevice$" | ForEach-Object { ($_ -split "`t")[0] })
$emulator = $serials | Where-Object { $_ -match "^emulator-" } | Select-Object -First 1
if ($emulator) {
    & $adb -s $emulator shell pm clear fumei.faruk.dev.br | Out-Null
    Write-Host "cleared app data on $emulator (debug sample will load on start)"
}

& "$repoRoot\gradlew.bat" installDebug --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$target = if ($emulator) { $emulator } else { $serials[0] }
& $adb -s $target shell am start -n fumei.faruk.dev.br/.MainActivity
$resumed = & $adb -s $target shell dumpsys activity activities | Select-String "ResumedActivity.*MainActivity"
Write-Host $resumed
Write-Host "localhost OK: Pixel6 debug with 30-day sample ending today (emulator only)"
