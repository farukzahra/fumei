# Template: Chrome with CDP 9222 (Chrome 136+ requires non-default user-data-dir).
# Copy to consumer project scripts/chrome-debug-start.ps1

$chrome = "${env:ProgramFiles}\Google\Chrome\Application\chrome.exe"
$profileDir = Join-Path (Get-Location) ".chrome-cdp-profile"

if (-not (Test-Path $chrome)) {
  Write-Error "Chrome not found at $chrome"
  exit 1
}

$chromeProcs = Get-Process chrome -ErrorAction SilentlyContinue
if ($chromeProcs) {
  Write-Host "Close ALL Chrome windows and run again."
  Write-Host "Chrome processes: $($chromeProcs.Count)"
  exit 1
}

New-Item -ItemType Directory -Force -Path $profileDir | Out-Null
Write-Host "Starting Chrome (.chrome-cdp-profile) + debug :9222..."
Start-Process -FilePath $chrome -ArgumentList @(
  "--remote-debugging-port=9222",
  "--remote-allow-origins=*",
  "--user-data-dir=`"$profileDir`""
)
Start-Sleep -Seconds 4
Write-Host "Done. Log in to Instagram if needed, then: npm run instagram:post:chrome"
