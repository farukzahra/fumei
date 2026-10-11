---
name: automate-before-manual
description: >-
  Try PAT, SSH, APIs, Azure CLI, and documented scripts before asking the user
  for manual browser or terminal steps. Use when about to request login, token,
  deploy, DNS, or "please run this yourself".
disable-model-invocation: false
---

# Automate before manual

Before asking the user to click, install, create a token, configure DNS, or run something in their terminal:

## 1. Try automation first

- GitHub: `gh` CLI or PAT from the secrets vault (see below)
- Deploy: scripts in `docs/deploy-vps.md`, `scripts/deploy-*`, GitHub Actions API
- SSH: deploy keys documented in the project or secrets vault
- Azure: `az login` or service principal path documented in the repo

## 2. Secrets vault (relative paths only)

**Never hardcode absolute paths** (`C:\...`, `/home/...`) in skills, commits, or chat copy.

From the **project repo root**, the shared vault is:

```
../secrets/
```

Common files (read only if they exist):

| Need | Relative path |
|------|----------------|
| GitHub PAT | `../secrets/github/pat.txt` (line starting with `ghp_`) |
| VPS SSH | `../secrets/vps/ssh/` |
| Per-app env | `../secrets/projects/<app>.env` |
| VPS inventory | `../secrets/vps/inventory.md` |

If the repo layout differs, check `AGENTS.md` or `secrets.local.md` in the project for the vault location — still prefer **relative** paths in docs.

## 3. Before asking for a credential

State which **role/scope** unlocks full automation (e.g. GitHub repo scope, Azure Contributor). Only escalate when impossible: OAuth consent, 2FA, billing, domain registrar.

## 4. Do not

- Send a manual checklist without trying API/SSH/PAT/Azure first
- Ask permission for steps you can already run with existing keys
- Commit or paste secrets into the repo or skills.sh

## 5. User pushes back ("lazy", "you do it")

Read and follow **`dont-be-lazy`**: search MCP/API, store creds in `../secrets/`, then drive the Cursor browser — do not be lazier than the user.
