# Deploy privacy policy to VPS for Play Console URL.
# Target: https://www.faruk.dev.br/fumei/privacy/
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$source = Join-Path $repoRoot "docs\play-store\privacy-policy.html"
$sshKey = Join-Path (Split-Path -Parent $repoRoot) "secrets\vps\ssh\github-actions-vps-shared"
$hostAddr = "root@66.23.231.218"
$remoteDir = "/opt/fumei-site/privacy"
$remoteFile = "$remoteDir/index.html"

if (-not (Test-Path $source)) {
    throw "Privacy policy not found: $source"
}
if (-not (Test-Path $sshKey)) {
    throw "SSH key not found: $sshKey"
}

Write-Host "Uploading privacy policy..."
ssh -i $sshKey -o StrictHostKeyChecking=accept-new $hostAddr "mkdir -p $remoteDir"
scp -i $sshKey -o StrictHostKeyChecking=accept-new $source "${hostAddr}:${remoteFile}"

Write-Host "Patching Caddy for /fumei/* static files..."
$patchScript = @'
from pathlib import Path

path = Path("/etc/caddy/Caddyfile")
content = path.read_text(encoding="utf-8")
block = """faruk.dev.br, www.faruk.dev.br {
\tencode gzip zstd
\treverse_proxy 127.0.0.1:3000
}"""
replacement = """faruk.dev.br, www.faruk.dev.br {
\thandle /fumei/* {
\t\turi strip_prefix /fumei
\t\troot * /opt/fumei-site
\t\tfile_server
\t}
\thandle {
\t\tencode gzip zstd
\t\treverse_proxy 127.0.0.1:3000
\t}
}"""
if "handle /fumei/*" not in content:
    if block not in content:
        raise SystemExit("Caddy block not found; update scripts/deploy-privacy.ps1")
    content = content.replace(block, replacement, 1)
    path.write_text(content, encoding="utf-8")
    print("caddy-updated")
else:
    print("caddy-already-ok")
'@

$patchPath = Join-Path $env:TEMP "patch-caddy-fumei-remote.py"
Set-Content -Path $patchPath -Value $patchScript -Encoding UTF8
scp -i $sshKey -o StrictHostKeyChecking=accept-new $patchPath "${hostAddr}:/tmp/patch-caddy-fumei.py"
ssh -i $sshKey -o StrictHostKeyChecking=accept-new $hostAddr "python3 /tmp/patch-caddy-fumei.py && caddy validate --config /etc/caddy/Caddyfile && systemctl reload caddy"

Write-Host "Verifying https://www.faruk.dev.br/fumei/privacy/ ..."
$response = Invoke-WebRequest -Uri "https://www.faruk.dev.br/fumei/privacy/" -UseBasicParsing
if ($response.Content -notmatch "Quantos fumei" -or $response.Content -notmatch "Privacidade") {
    throw "Privacy page verification failed"
}
Write-Host "OK: privacy policy is live at https://www.faruk.dev.br/fumei/privacy/"
