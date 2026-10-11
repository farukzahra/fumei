---
name: finish-with-dev-server
description: >-
  After implementation or UI fixes, ensure local dev servers are running on
  documented ports, resolve port conflicts, validate health or title, and report
  test URLs. Use when finishing a coding task on a runnable app.
disable-model-invocation: false
---

# Finish with dev server

When a task changed runnable code (UI, API, config), before closing:

## 1. Find documented ports

Read **project docs first** — prefer ports already written somewhere:

- `AGENTS.md`, `README.md`, `docs/stack.md`
- `.env.example`, `vite.config.*`, `package.json` scripts
- Fallback order in project rules (e.g. backend 3000 → 3001)

Do not invent a port if the repo documents one.

## 2. Check if servers are up

Inspect terminals and listen on the documented port(s). Start missing processes **without asking permission** using the project's documented commands.

## 3. Port already in use

When the preferred port is taken:

1. Identify the owning process (PID, command line, working directory).
2. **Same project** (cwd under this repo, or known dev script for this app) → stop that process, restart on the **documented** port.
3. **Another project or unknown** → **do not kill**; use the **next fallback port** documented in this repo (e.g. 3000 → 3001 → 3002). Update nothing in the other project.
4. If no fallback is documented, pick the next free port, note it in the reply, and align env vars if the project requires it (`VITE_API_URL`, `CORS_ORIGIN`, etc.).

## 4. Validate

Use the project's identity check: `GET /health`, `/api/health`, page `<title>`, or equivalent from `AGENTS.md`.

## 5. Report

Give the user **effective URLs** (scheme + host + port + path). Never end a coding task without test URLs when a local app exists.

## Skip

Libraries, templates with no dev server, or tasks that only changed docs/skills with no runnable app.
