---
name: ui-change-e2e
description: >-
  Require E2E tests when creating, changing, or fixing UI. Add a failing test
  for bugs, mock API writes, run project test:e2e before done. Use for web UI
  bug reports, new screens, or route changes.
disable-model-invocation: false
---

# UI change — E2E required

Applies when the project has a frontend E2E setup (`e2e/`, `playwright.config.*`, or path in `AGENTS.md`).

## New or changed screen

1. Find or create spec under the project's E2E folder.
2. Cover the new or changed behavior.
3. Prefer `data-testid` or stable roles over fragile CSS classes.
4. Run the project's E2E command before closing (`npm run test:e2e`, `pnpm test:e2e`, etc.).

## Bug fix

1. Check if a spec exists for that area.
2. Add a test case that **reproduces** the bug (or extend existing spec).
3. Order: **failing test → fix → green E2E**.
4. Do not mark the bug fixed without a regression test.

## API writes in E2E

- **Mock** POST/PATCH/DELETE via `page.route` (or project `fixtures/mock-api.ts`) unless the project's `AGENTS.md` documents a safe real-write exception.
- Real DB/integration tests belong in `backend/tests/` or documented E2E seed users — not production data.

## Before done

- Run full E2E or at least the affected spec (`playwright test path/to/spec.ts`).
- Fix failures — do not report complete with red E2E.

## Skip

No frontend, no Playwright/Cypress setup, or user explicitly scoped a docs-only change.

See project `AGENTS.md` for spec paths, auth helpers, and coverage tables.
