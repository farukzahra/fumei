---
name: dont-forget
description: >-
  Turn recurring obligations into executable automation (CI, hooks, scripts,
  codegen, lint) instead of prose reminders agents may skip. Use when the user
  asks to always, never forget, every time, on each deploy, before commit, or
  when a task was forgotten repeatedly and needs hard enforcement.
disable-model-invocation: false
---

# Don't forget — obligation as automation

Agents forget. Written rules and chat promises are **soft** — they compete with context, get skipped under pressure, and vanish in new sessions.

**Default:** when the user asks for something that must happen **repeatedly** or was **forgotten before**, encode it in **executable machinery** the next agent cannot bypass without breaking the build.

The agent picks the **highest tier that fits this repo** — CI step, hook, failing test, codegen diff, npm script chain, etc. The "software" is whatever executable gate matches the obligation and trigger; not a fixed recipe.

> Not "AI safety guardrails" — **enforcement**: repo machinery that fails when an obligation was skipped.

## When to use

- User says: *always*, *every time*, *never forget*, *on each deploy*, *before every commit*, *don't let this slip again*
- Same instruction was given in a prior session and not followed
- A counter, check, sync step, or enforcement must survive session handoff
- User prefers "put it in the build" over "remember to do X"

## When NOT to use

- One-off task in the current session only
- Judgment-heavy work (copy tone, architecture tradeoffs) with no objective pass/fail
- Automation cost exceeds value (5-minute yearly task)
- User explicitly wants a manual checklist only

## Enforcement ladder (prefer top)

| Tier | Mechanism | Survives new chat? | Example |
|------|-----------|-------------------|---------|
| 1 | **CI / deploy pipeline** | Yes | Bump deploy counter in GitHub Actions |
| 2 | **Git hooks** (pre-commit, pre-push) | Yes | Block commit if DER not updated |
| 3 | **Build / test failure** | Yes | `make der-check` in CI |
| 4 | **Lint / type rules** | Yes | ESLint rule, custom Ruff check |
| 5 | **Codegen / scaffolder** | Yes | `npm run generate:api` writes client from OpenAPI |
| 6 | **Scheduled job** | Yes | Cron syncs inventory nightly |
| 7 | **Cursor hook / project rule** | Partial | `.cursor/hooks.json` runs validator on save |
| 8 | **Makefile / npm script** | Partial | `npm run deploy` chains all required steps |
| 9 | **Prose rule** (AGENTS.md, SKILL.md) | Weak | Last resort or supplement to tiers 1–8 |

Pick the **highest tier that fits**. Add a prose rule only to **point at** the automation, not replace it.

## Workflow

1. **Name the obligation** — one sentence, observable outcome (*"deploy counter increments on every production deploy"*).
2. **Find the natural trigger** — deploy, commit, push, PR merge, file save, schedule, API call.
3. **Choose enforcement tier** from the ladder above.
3b. **Propose to user (before any file edits)** — Present in plain language:
   - The obligation (one sentence, observable)
   - The trigger (deploy, commit, PR, schedule, etc.)
   - Chosen mechanism and tier (e.g. "GitHub Actions step after deploy — tier 1")
   - Files to create or change
   - What fails or blocks if the obligation is skipped
   - Optional: one-line `AGENTS.md` pointer you will add after implementation

   **Wait for user approval or adjustment.** Do not implement until the user confirms.
4. **After user approval, implement the smallest hard gate** — script that exits non-zero, generated file diff, or pipeline step that fails.
5. **Wire the trigger** — hook, workflow, `package.json` script chain, Makefile `deploy` target.
6. **Verify** — run the trigger locally; confirm failure when obligation is skipped and success when met.
7. **Document briefly** — one line pointing at the automation:
   - **AGENTS.md** (preferred in Cursor/agent repos): e.g. `Deploy counter: auto-incremented in .github/workflows/deploy.yml`
   - **README.md** when operators without agents need it
   - Never a paragraph asking future agents to remember

## Decision checklist

Before finishing, confirm:

- [ ] Obligation is tied to a **concrete event** (not "sometimes")
- [ ] Skipping it causes **visible failure** (red CI, blocked commit, test fail) OR **automatic correction** (codegen)
- [ ] A new agent can discover it from **repo files**, not chat history
- [ ] User saw and approved the enforcement proposal before code changed
- [ ] User was not given "I'll remember" without code

## Patterns by domain

### Deploy / release

- GitHub Actions step after successful deploy
- Release script that bumps version + changelog + counter in one command
- Tag hook that refuses tag if checklist script fails

### Commits / PRs

- pre-commit: format, secrets scan, schema ↔ DER sync
- PR CI: required check that cannot be skipped
- Danger / custom bot comment is **weak** — prefer failing check

### Schema / docs drift

- Codegen from single source of truth (OpenAPI → client, SQL → types)
- `make check` compares generated output to committed files
- Fail CI if `database/schema.sql` changed but `database/der.md` did not

### Environment / ports / health

- `docker-compose` healthcheck + `depends_on: condition: service_healthy`
- Startup script that probes `/health` before reporting URL to user
- `.env.example` validated by script in CI

### Agent-specific enforcement

- Cursor **hook** on `afterFileEdit` running project validator
- `.cursor/rules` that say **run** `npm run verify-x`, not "remember to verify"
- Project skill with `disable-model-invocation: false` only when prose truly helps

### Recurring maintenance

- `cron` / GitHub scheduled workflow
- Dependabot + auto-merge with passing CI
- Stale-issue bot is notification-only — prefer workflow that **does** the sync

## Anti-patterns

| Bad | Better |
|-----|--------|
| "I'll increment the counter next deploy" | Step in `deploy.yml` increments `deploy-count.txt` |
| Long AGENTS.md section repeating the same reminder | `pre-commit` + one-line pointer |
| Optional npm script nobody runs | `deploy` script calls it; CI runs `deploy --dry-run` |
| Comment `// TODO: don't forget X` | Test or lint that fails until X exists |

## Example (deploy counter)

**User:** "Increment a counter every deploy — you forgot three times."

**Propose (before coding):**

> **Obligation:** `deploy-count.json` increments on every successful production deploy.
> **Trigger:** GitHub Actions deploy workflow, after health check passes.
> **Mechanism:** Tier 1 — workflow step runs `scripts/increment-deploy-count.sh` and commits or updates the counter file.
> **Files:** `.github/workflows/deploy.yml`, `scripts/increment-deploy-count.sh`, `deploy-count.json`
> **If skipped:** deploy job fails (or counter stays stale — prefer failing the step).
> **AGENTS.md line:** `Deploy count: see .github/workflows/deploy.yml`

Approve to implement?

**Do:**

1. Add `scripts/increment-deploy-count.sh` (or inline in workflow).
2. Call it in `.github/workflows/deploy.yml` after successful deploy.
3. Commit `deploy-count.json` (or expose via API).
4. AGENTS.md: `Deploy count: see .github/workflows/deploy.yml`

**Don't:** Add only a user rule saying "remember to increment deploy count."

## Report to user

After implementation (post-approval confirmation — not a substitute for the pre-implementation proposal), state:

1. **What** is enforced
2. **Where** (file + trigger)
3. **What fails** if skipped
4. That future sessions do **not** rely on memory

## Additional examples

See [reference/examples.md](reference/examples.md) for more scenarios and tier choices.
