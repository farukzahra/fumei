# Project integration checklist

## package.json scripts

```json
{
  "chrome:debug": "powershell -ExecutionPolicy Bypass -File scripts/chrome-debug-start.ps1",
  "instagram:post:chrome": "tsx scripts/instagram-post-cdp.ts"
}
```

## .gitignore

```
.chrome-cdp-profile/
.instagram-browser-profile/
```

## Minimal file layout

```text
scripts/
  chrome-debug-start.ps1
  instagram-post-cdp.ts
  lib/
    instagram-cdp.ts       # connectOverCDP, isChromeDebugPortOpen
    instagram-playwright.ts  # isLoggedIn, waitForManualLogin, ensureAccount
    instagram-publish.ts     # create flow + setInputFiles
```

## instagram-cdp.ts essentials

- `chromium.connectOverCDP('http://127.0.0.1:9222')`
- Reuse existing Instagram tab or `goto` instagram.com
- `browser.close()` disconnects only — does not quit user's Chrome

## instagram-publish.ts essentials

- `page.goto('https://www.instagram.com/create/select/')` fallback
- `fileInput.waitFor({ state: 'attached' })` — not `visible`
- Next loop until caption; Share / Compartilhar
- Verify `a[href*="/p/"]` on target profile

## Constants per project

- `INSTAGRAM_USERNAME` — expected active account
- Image path + caption from project marketing module (single source of truth)
- Optional: `../secrets/instagram/<app>-login.env` for human reference only

## Numinoso reference implementation

Repo: `sessao-gravador` (terapia / Numinoso)

- Copy: `social-copy.ts` + `npm run instagram:sync` for JSON sidecar
- Post image: `docs/instagram/post-lancamento.jpg` (convert from PNG)
