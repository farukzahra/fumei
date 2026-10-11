# Instagram Chrome post — troubleshooting

## Port 9222 not listening

- Chrome 136+ **ignores** `--remote-debugging-port` on the default `%LOCALAPPDATA%\Google\Chrome\User Data`.
- Fix: dedicated `--user-data-dir` (e.g. `.chrome-cdp-profile` in project root).
- Confirm with `curl http://127.0.0.1:9222/json/version`.

## `chrome:debug` says N processes still running

- User must close all Chrome windows, or agent kills with user approval: `Stop-Process -Name chrome -Force`.

## Script waits forever on login

- Do not navigate away from login and poll only on `/accounts/login/`.
- Poll by reloading `https://www.instagram.com/` and checking `sessionid` / `ds_user_id` cookie + home nav.

## Post “succeeds” but profile still empty

- **Wrong account** — verify Perfil link href matches target `@username`.
- **PNG rejected** — use JPEG.
- **Share clicked too early** — wait for caption field; loop Next until visible.
- **Wrong profile URL in verify step** — check logged-in user’s profile, not only public URL.

## Captcha on Playwright-launched Chrome

- Switch to **CDP + real chrome.exe** with `.chrome-cdp-profile`, manual login once.
- Do not retry automated password fills.

## Cannot attach to user’s everyday Chrome without restart

- Default profile + CDP blocked on Chrome 136+.
- Options: (a) `.chrome-cdp-profile` + one manual login, (b) Instagram Graph API, (c) `chrome://inspect/#remote-debugging` (newer Chrome; tooling support varies).
