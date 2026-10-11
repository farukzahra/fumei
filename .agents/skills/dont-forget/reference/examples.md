# Don't forget — examples

Before implementing any pattern below, **propose** the obligation, trigger, tier, files, and failure mode to the user and wait for approval.

## Increment counter on every deploy

| Tier | Implementation |
|------|----------------|
| 1 | `deploy.yml` job step: `node scripts/bump-deploy-count.js` after health check passes |
| 8 | `npm run deploy` runs bump script; document that manual deploys must use the script |

## Update DER when schema changes

| Tier | Implementation |
|------|----------------|
| 2 | pre-commit: if `schema.sql` staged, require `der.md` + `der.meta.json` staged |
| 3 | `make der-check` in CI; Makefile target regenerates or diffs |

## Run E2E before claiming UI done

| Tier | Implementation |
|------|----------------|
| 3 | CI job `test:e2e` required on PR |
| 8 | `package.json` `"prepush": "npm run test:e2e"` via husky |

## Sync OpenAPI client after backend route change

| Tier | Implementation |
|------|----------------|
| 4 | CI: `npm run generate:api && git diff --exit-code` |
| 5 | Codegen from `openapi.json`; dev runs `npm run generate:api` |

## Restart app after config change (local dev)

| Tier | Implementation |
|------|----------------|
| 7 | Cursor hook or project rule wired to **script** `scripts/restart-if-needed.sh` |
| 9 | Rule alone: weak — agent may still skip |

## Nightly backup / sync

| Tier | Implementation |
|------|----------------|
| 6 | `.github/workflows/nightly-sync.yml` on `schedule:` |
| 1 | VPS cron + monitoring alert if job misses |

## Never commit secrets

| Tier | Implementation |
|------|----------------|
| 2 | pre-commit: gitleaks or `detect-secrets` |
| 1 | CI secret scan on every push |

## Bump version on release

| Tier | Implementation |
|------|----------------|
| 5 | `npm version` / `changeset publish` single command |
| 1 | Release workflow tags only if `CHANGELOG.md` entry exists |

## Choosing tier when unsure

Ask:

1. **Who must comply?** Only humans → hook/script. CI bots too → tier 1–3.
2. **Can we detect violation objectively?** Yes → automate. No → prose + checklist.
3. **What's the blast radius if forgotten?** High → tier 1–2. Low → tier 7–8 may suffice.
