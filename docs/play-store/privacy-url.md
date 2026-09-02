# Política de Privacidade — URL pública

**URL para o Play Console:**

https://www.faruk.dev.br/fumei/privacy/

Arquivo fonte: `docs/play-store/privacy-policy.html`  
Deploy na VPS: `/opt/fumei-site/privacy/index.html`

## Deploy

```powershell
cd C:\repo\fumei
.\scripts\deploy-privacy.ps1
```

O script envia o HTML, garante a rota `/fumei/*` no Caddy e valida a URL pública.
