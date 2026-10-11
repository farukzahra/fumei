---
name: dont-be-lazy
description: >-
  When the user is lazy or refuses manual steps, the agent must not be lazier —
  discover API/MCP first, store credentials in ../secrets/ (never commit), then
  drive the Cursor browser to finish the task. Use when the user says lazy, "you
  do it", or the agent was about to delegate a step the user could avoid.
disable-model-invocation: false
---

# Don't be lazy — outwork the user

**Golden rule:** If the user is lazy, **you must not be lazier.** Do not offload work you can still do with API, MCP, secrets, or the Cursor browser.

Pair with **`automate-before-manual`** (try automation first). This skill applies when the user pushes back on manual steps or you were about to send a checklist they will ignore.

## When to use

- User says **lazy**, **"you do it"**, **"don't ask me"**, **"just handle it"**
- You were about to ask the user to click, log in, copy-paste, or run something in **their** terminal/browser
- A task looks "manual" but might have an API, MCP server, CLI, or browser path you have not tried yet

## Escalation ladder (always in order)

### 1. Discover official automation

Before any manual ask, spend real effort finding a programmatic path:

| Source | Action |
|--------|--------|
| **MCP** | `GetDynamicTools` — search namespaces/tools for the product |
| **Project** | `AGENTS.md`, `docs/`, `scripts/`, `Makefile`, existing PAT/SSH usage |
| **CLI** | `gh`, `az`, cloud/vendor CLIs documented in the repo |
| **HTTP API** | Official REST/GraphQL docs; prefer documented endpoints over scraping |
| **Web** | Search for official API/MCP/CLI — not forum hacks |

If you find a working path, **use it** without asking the user to repeat steps you can run.

### 2. Credentials → vault, then retry

When automation needs a token, password, or session:

1. **Ask once** for exactly what is missing (scope/role included — see `automate-before-manual`).
2. **Store in the secrets vault** (relative path from project root):

   ```
   ../secrets/<service>/<purpose>.txt
   ```

   Examples: `../secrets/linkedin/farukz.txt`, `../secrets/stripe/api-key.txt`.

3. **Never commit** secrets — confirm `.gitignore` covers `../secrets/` (vault lives outside repos).
4. **Retry** the API/CLI/MCP path with the stored credential before browser fallback.

See [reference/credential-vault.md](reference/credential-vault.md).

### 3. Browser fallback — you drive, user only unblocks

If **no official API/MCP/CLI** exists, or auth requires a human-only gate once:

1. Open the **Cursor browser** (`cursor-ide-browser` MCP): `browser_navigate` → `browser_lock` → `browser_snapshot`.
2. **Do the workflow yourself** — navigate, fill forms, submit, download, configure.
3. **Pause only for hard blockers:** OAuth consent, 2FA, captcha, payment, legal checkbox the user must own.
4. After the user unblocks (e.g. completes 2FA), **resume automation** — do not hand back a multi-step checklist.
5. Validate outcome (HTTP 200, expected page title, file saved) before reporting done.

See [reference/browser-fallback.md](reference/browser-fallback.md).

### 4. Escalate to the user (last resort)

Only when **all** of the above failed or a step is legally/financially bound to the human:

- State what you tried (API, MCP, vault, browser).
- Ask for the **minimum** single action (one login, one OTP, one approval).
- Immediately continue yourself after they respond.

## Do not

- Send a long manual checklist when MCP/API/browser could do 80% of it
- Ask the user to run commands you can run in the agent shell
- Ask the user to open a URL you can open in the Cursor browser
- Store or paste secrets into the repo, skills, commits, or chat logs
- Claim "manual only" without searching for API/MCP and attempting browser automation
- Stop after one failed click — diagnose with snapshot/screenshot, adjust, retry (max ~4 attempts per action; then report blocker)

## Relationship to other skills

| Skill | Role |
|-------|------|
| `automate-before-manual` | Default: try PAT/SSH/API before any manual ask |
| **`dont-be-lazy`** | Stronger: user won't do manual work — you finish via vault + browser |
| `validate-before-share` | After browser/API work, verify URLs and outcomes before telling the user |
| `find-job` | Example: LinkedIn login manual once; agent drives search and Easy Apply |

## Quick checklist (agent self-audit)

Before asking the user to do something:

- [ ] Searched MCP tools for this product?
- [ ] Checked `../secrets/` for an existing credential?
- [ ] Tried official API/CLI?
- [ ] If still blocked: opened Cursor browser and attempted the flow?
- [ ] Escalation is a **single** unblock, not a tutorial?
