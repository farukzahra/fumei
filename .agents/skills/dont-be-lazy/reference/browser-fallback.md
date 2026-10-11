# Browser fallback — Cursor IDE browser

Use when API/MCP/CLI cannot complete the task. **You** operate the browser; the user only handles unblockable gates.

## Tooling

Namespace: **`cursor-ide-browser`**

Discover with `GetDynamicTools` before first use in a session.

## Standard flow

1. `browser_tabs` — list open tabs; reuse when sensible.
2. `browser_navigate` — open target URL (background tab OK for automation).
3. `browser_lock` — `{ action: "lock" }` before a sequence of interactions.
4. `browser_snapshot` — accessibility tree; use `ref` handles for clicks/fills.
5. Interact: `browser_click`, `browser_fill`, `browser_type`, `browser_select_option`, `browser_press_key`, `browser_scroll`.
6. `browser_take_screenshot` — when visual state is unclear.
7. `browser_lock` — `{ action: "unlock" }` when finished.

**Order:** navigate → lock → work → unlock. If a tab already exists, lock first.

## Human-only gates (pause and ask once)

- OAuth / "Sign in with Google" consent screens you cannot complete
- 2FA / SMS / authenticator codes
- CAPTCHA / bot checks
- Payment or legal acceptance in the user's name

Message template:

> I opened [URL] and reached [step]. I need you to [single action]. Reply when done and I continue.

Then snapshot again and **continue the workflow yourself**.

## Anti-patterns

- "Please open LinkedIn and search for…" — **you** navigate and search
- "Copy this curl into your terminal" — **you** run curl unless it requires their local-only cert
- Repeat the same failing click without a new snapshot or hypothesis

## Stop conditions

After ~4 failed attempts on the same action with fresh evidence, report:

- Current URL and visible blocker
- What automation paths were tried (API, MCP, browser)
- The **one** minimum user action left, if any
