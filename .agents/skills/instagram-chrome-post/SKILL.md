---
name: instagram-chrome-post
description: >-
  Publish Instagram posts via real Chrome + Playwright CDP (not Cursor browser
  MCP). Use when automating Instagram create-post, launch posts, or social
  publishing on Windows; handles Chrome 136+ debug profile, manual login,
  account verification, and JPEG upload.
disable-model-invocation: false
---

# Instagram — publish via Chrome + Playwright

Automate **single-image Instagram posts** for a **Business/Creator** account using **real Chrome** and **Playwright `connectOverCDP`**.

## Do not use

| Approach | Why |
|----------|-----|
| **Cursor IDE browser MCP** | `DOM.setFileInputFiles` blocked; paste/upload unreliable |
| **Playwright isolated profile without CDP** | Triggers captcha / “suspicious login” more often |
| **`--remote-debugging-port` on default Chrome `User Data`** | **Ignored on Chrome 136+** — port never opens |
| **Automated password login** | Captcha / 2FA — user logs in manually once |
| **PNG in create-post file input** | Input often `accept="image/jpeg"` — convert to `.jpg` |

## Preferred long-term path

**Instagram Graph API** (container + publish) when recurring automation is needed — no browser, no captcha. Browser flow below is for **one-off / until API is wired**.

## Architecture

```text
chrome.exe  --remote-debugging-port=9222  --user-data-dir=<project>/.chrome-cdp-profile
     ↑
Playwright chromium.connectOverCDP('http://127.0.0.1:9222')
     ↑
setInputFiles(image.jpg) → Next → caption → Share
```

Session persists in `.chrome-cdp-profile` (gitignore it).

## Workflow (agent)

### 1. Prerequisites in consumer project

- `@playwright/test` installed (or `playwright` with chromium)
- Scripts under `scripts/` — see [reference/project-integration.md](reference/project-integration.md)
- `package.json` scripts: `chrome:debug`, `instagram:post:chrome`
- Marketing copy / image paths owned by the project (e.g. `docs/instagram/`, `social-copy.ts`)

### 2. Start Chrome with CDP

1. **Kill stray Chrome** if `chrome:debug` reports processes still running (`Stop-Process -Name chrome -Force` when user approves).
2. From project root:

   ```bash
   npm run chrome:debug
   ```

3. Verify: `curl http://127.0.0.1:9222/json/version` → HTTP 200 JSON.

Uses **non-default** `--user-data-dir` (required since Chrome 136). Template: [reference/chrome-debug-start.ps1](reference/chrome-debug-start.ps1).

### 3. Login (manual, once per profile)

- User logs in on the opened Chrome window (captcha / 2FA OK).
- **Verify active account** before posting — `getByRole('link', { name: /^Perfil$|^Profile$/i })` → href must match target `@username`.
- If wrong account: user switches via Instagram **Switch accounts**; never post to the wrong profile.

### 4. Publish

```bash
npm run instagram:post:chrome
```

Script must:

1. `connectOverCDP` → find or open `instagram.com` tab
2. `ensureInstagramAccount(expectedUsername)` — fail fast if mismatch
3. Go to `/create/select/` or click `a[href*="/create/"]`
4. `input[type="file"]` — `waitFor({ state: 'attached' })` then `setInputFiles` (hidden input is OK)
5. Click **Next / Avançar** until caption field visible (loop, max ~5)
6. Fill caption (PT or EN selectors: `legenda`, `caption`, `role=textbox`)
7. **Share / Compartilhar**
8. Verify: profile `a[href*="/p/"]` count > 0

### 5. Image format

Convert PNG → JPEG before post if needed:

```bash
python -c "from PIL import Image; Image.open('post.png').convert('RGB').save('post.jpg', quality=95)"
```

Prefer `.jpg` in the publish script when both exist.

## Secrets

- Optional login hints in `../secrets/instagram/<project>-login.env` — **never** auto-submit password in browser automation.
- Never commit secrets or `.chrome-cdp-profile/`.

## Troubleshooting

See [reference/troubleshooting.md](reference/troubleshooting.md).

## Consumer project reference

First implementation: **Numinoso** (`sessao-gravador`) — `scripts/instagram-post-cdp.ts`, `scripts/lib/instagram-*.ts`, `npm run instagram:post:chrome`.

When adding to a new repo, copy patterns from [reference/project-integration.md](reference/project-integration.md); keep captions/images in project-specific modules.

## Related skills

- `automate-before-manual` — try scripts/PAT before asking user
- `dont-be-lazy` — user said “you do it”; still use **Chrome CDP**, not Cursor MCP upload hacks
- `validate-before-share` — confirm post on profile URL before reporting success
