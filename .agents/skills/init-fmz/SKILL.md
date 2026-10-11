---
name: init-fmz
description: >-
  Use /init-fmz to bootstrap a new project from a prompt: ask for the project name,
  create C:\faruk\<project>, write PLAN.md, install Superpowers skills, and run
  the Superpowers planning pipeline on the plan.
disable-model-invocation: false
---

# Init FMZ

Use this skill only when the user explicitly invokes `/init-fmz`.

Bootstrap a new project under `C:\faruk\<project>` from the user's prompt, then hand off to the Superpowers workflow.

Works in **Cursor** and in **VS Code Copilot** — no IDE-specific MCP tools required. Use PowerShell + `git` for every step, always with absolute paths, because the shell working directory is **not** preserved between commands.

> **Machine layout:** this skill targets one specific setup — projects in `C:\faruk\<slug>`, conventions repo at `C:\repo\agent-skills`, secrets vault in `C:\faruk\secrets` / `C:\repo\secrets`. Adjust those roots if you clone it elsewhere.

## Parse Input

1. Treat all user text after `/init-fmz` as the **project prompt** (requirements, idea, or spec seed).
2. If the prompt is empty, ask once: "Qual é o prompt ou a ideia do projeto?" and wait.
3. Do not start scaffolding until you have a non-empty prompt.

## Ask Project Name

Ask once in plain chat:

> Qual o nome do projeto?

Normalize the answer into a folder-safe slug:

- lowercase
- letters, digits, and hyphens only
- spaces/underscores → hyphens
- collapse repeated hyphens

If the slug is empty or invalid, ask again with one short example (`meu-app`).

Confirm the final path: `C:\faruk\<slug>`.

## Create Project

Run these steps in order:

1. Ensure `C:\faruk` exists (`New-Item -ItemType Directory -Force` on Windows).
2. Set `projectRoot = C:\faruk\<slug>`.
3. If `projectRoot` already exists and is non-empty, ask whether to use it anyway or pick another name. Do not overwrite without explicit confirmation.
4. Create the directory and init git natively:

```powershell
New-Item -ItemType Directory -Force $projectRoot | Out-Null
git -C $projectRoot init -b main    # git >= 2.28; else: git init, then git branch -M main
git config --global user.name; git config --global user.email   # must be non-empty, else commits fail
```

5. **Move the agent to the project root before any other project work.** Pick the first option available:
   - The agent exposes a workspace/root tool (e.g. VS Code `set_workspace`) → ask the user once to confirm, then use it.
   - Otherwise → keep working with absolute paths: `git -C $projectRoot`, `npm --prefix $projectRoot`, or `Push-Location $projectRoot` inside a single command.

Do **not** depend on `mcp__cursor-app-control__create_project` / `move_agent_to_root` — those exist only in Cursor. Never assume the shell starts in `projectRoot`: each command starts in the session directory.

## Write PLAN.md

Create `PLAN.md` at the project root with this structure:

```markdown
# Project Plan

> Created by `/init-fmz` on <YYYY-MM-DD>.

## Prompt

<verbatim user prompt — do not paraphrase>

## Goals

- [ ] Define success criteria during brainstorming
- [ ] Produce an implementation plan
- [ ] Implement and verify

## Notes

_Add constraints, stack preferences, or open questions here during Superpowers brainstorming._
```

## Scaffold Files

Before the first commit, also create:

**`.gitignore`** (minimum):

```gitignore
.env
.env.*
!.env.example
node_modules/
dist/
build/
.venv/
__pycache__/
*.pyc
.DS_Store
Thumbs.db
.cursor/
.agents/
.claude/
```

**`README.md`** (one-liner from the prompt title — can expand later).

**Why the agent dirs are ignored:** `npx skills add` creates junctions on Windows (e.g. `.claude/skills/<name>` → `.agents/skills/<name>`). Git follows junctions and stages every target file as a duplicate (74 phantom files in a real run). Always run `git status --short` before the first commit and confirm no `.agents/`, `.cursor/` or `.claude/` paths are staged.

## GitHub First Commit

After `PLAN.md`, `.gitignore`, and `README.md` exist, create the repo on GitHub and push the first commit automatically. Do **not** ask the user for a PAT before checking local secret files.

### 1. Resolve PAT (never print the token)

Read in order; use the first file that yields a line matching `^(ghp_|github_pat_|gho_)` (classic and fine-grained PATs):

1. `C:\faruk\secrets\github\pat.txt`
2. `C:\faruk\secrets\github-pat.txt`
3. `C:\faruk\secrets\pat.txt`
4. `C:\repo\secrets\github\pat.txt`
5. `C:\repo\faruk\.env` → `GITHUB_TOKEN`

If none found, stop and tell the user to place a PAT in `C:\faruk\secrets\github\pat.txt`.

### 2. Detect GitHub owner

```powershell
$pat = <from step 1>
$headers = @{ Authorization = "Bearer $pat"; Accept = "application/vnd.github+json"; "X-GitHub-Api-Version" = "2022-11-28" }
$user = (Invoke-RestMethod -Uri "https://api.github.com/user" -Headers $headers).login
```

Default repo owner is `$user` (typically `farukzahra`). Only ask the user if they want a different owner/org.

### 3. Create remote repository

**Always private** — do not ask; never create a public repo from `/init-fmz`.

```powershell
$body = @{ name = "<slug>"; private = $true; auto_init = $false } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "https://api.github.com/user/repos" -Headers $headers -Body $body -ContentType "application/json"
```

If HTTP 422 (repo already exists), reuse it — do not fail.

For an **org** repo: `POST https://api.github.com/orgs/<org>/repos` instead.

### 4. Commit and push

```powershell
git add -A                      # .gitignore keeps .agents/.cursor/.claude and secrets out
git status --short              # eyeball it: no secrets, no agent-dir duplicates
git commit -m "chore: initial project scaffold"
git branch -M main
git remote remove origin 2>&1 | Out-Null
git remote add origin "https://github.com/$user/<slug>.git"

# Authenticate without ever writing the token to disk:
$b64 = [Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("x-access-token:$pat"))
git -c "http.https://github.com/.extraheader=Authorization: Basic $b64" push -u origin main
```

Notes:

- Keep the remote URL **clean**. Never `git remote add origin` with credentials embedded — that writes the PAT in plain text into `.git/config`.
- Do **not** use `git push "<url-with-token>" -u origin main`: with a URL as the first positional argument, `origin` is parsed as a refspec and the push fails with `src refspec origin does not match any`. The `-c http.<url>.extraheader` form above keeps `-u origin main` working and sets the upstream correctly.
- Set `$env:GIT_TERMINAL_PROMPT = '0'` so a bad token fails fast instead of hanging on a credential prompt.

Use Conventional Commits English message. Never stage secrets (`.env`, `pat.txt`, keys).
### 5. Confirm

Report the repo URL `https://github.com/$user/<slug>` (no token in output). If push fails, read the API error and fix (auth, branch, or existing remote).

Prefer `Invoke-RestMethod` + `git` on Windows. Install `gh` only if already on PATH; do not block `/init-fmz` on `gh` missing.

## Production secrets — GitHub Actions (mandatory for VPS deploy)

If the project will deploy to a VPS (Docker, `deploy-vps.sh`, etc.), **during or right after init** scaffold:

1. **`docs/github-secrets.md`** — table of every GitHub Actions secret (app + infra).
2. **`.cursor/rules/production-secrets-github.mdc`** (Cursor) and **`.github/instructions/production-secrets-github.instructions.md`** (VS Code) — GitHub Secrets = source of truth; never commit `.env`; VPS `.env` generated by CI only.
3. **`scripts/render-production-env.sh`** — writes `.env` from env vars in the deploy workflow.
4. **Deploy workflow** — step *Render production .env from GitHub Secrets* → SCP to VPS before `deploy-vps.sh`.

Local cofre `C:\repo\secrets\projects\<slug>.env` is for the agent to **copy values into GitHub Secrets** — not for manual-only VPS `.env` edits.

Reference implementation: `farukzahra/terapia` (`docs/deploy-vps.md`, `.github/workflows/deploy.yml`).

## Install Superpowers

Once the agent is at the project root, install Superpowers skills **in the project**. Always pass the agent targets explicitly — without `-a`, the CLI installs into every agent it detects on the machine and sprays junctions everywhere:

```bash
npx skills add obra/superpowers --skill "*" -a cursor github-copilot -y
```

Agent dirs used by the `skills` CLI: **project** is `.agents/skills/` for both Cursor and GitHub Copilot; **global** is `~/.cursor/skills` (Cursor) and `~/.copilot/skills` (Copilot). On this machine Copilot also resolves `~/.agents/skills`.

If the command fails:

1. Retry once.
2. If it still fails, check whether the Superpowers skills are already available globally — `~/.agents/skills/` with `brainstorming`, `writing-plans`, `systematic-debugging`, `verification-before-completion`. In VS Code this is normal: the skill list is loaded at session start, so a mid-session project install does not appear until the window is reloaded.
3. Tell the user which path was used (project CLI install vs already-available global skills).

Do not block the pipeline on install failure if the skills are already available in the session.

## Install Faruk agent skills (mandatory)

After Superpowers, install **all** published skills from `farukzahra/agent-skills` (includes `automate-before-manual`, `dont-be-lazy`, `dont-forget`, `caveman-commit`, etc.):

```bash
npx skills add farukzahra/agent-skills -a cursor github-copilot -y
npx skills add mattpocock/skills@handoff -a cursor github-copilot -y
```

Copy the convention files for **both** agents (paths under `C:\repo\agent-skills`):

| From | To | Applies to |
|------|----|------------|
| `reference\commands\{init,commit-push,recap,handoff}.md` | `.cursor\commands\` | Cursor |
| `reference\rules\automate-before-manual.mdc` | `.cursor\rules\` | Cursor |
| `.github\prompts\*.prompt.md` | `.github\prompts\` | VS Code Copilot |
| `.github\instructions\*.instructions.md` | `.github\instructions\` | VS Code Copilot |

```powershell
$p = $projectRoot; $repo = "C:\repo\agent-skills"
New-Item -ItemType Directory -Force "$p\.cursor\rules","$p\.cursor\commands","$p\.github\instructions","$p\.github\prompts" | Out-Null
Copy-Item "$repo\reference\rules\automate-before-manual.mdc" "$p\.cursor\rules\" -Force
foreach ($c in "init","commit-push","recap","handoff") { Copy-Item "$repo\reference\commands\$c.md" "$p\.cursor\commands\" -Force }
Copy-Item "$repo\.github\instructions\*.instructions.md" "$p\.github\instructions\" -Force
Copy-Item "$repo\.github\prompts\*.prompt.md" "$p\.github\prompts\" -Force
```

`scripts\sync-agent-conventions.ps1 -SkipSkills:$false` walks only the **parent of the agent-skills repo** (`C:\repo\*`), so it does **not** reach a fresh `C:\faruk\<slug>` project. Use it to refresh existing `C:\repo` repos; for a new project run the copies above.

### Rules + AGENTS.md

`AGENTS.md` is read by both Cursor and VS Code Copilot, so it holds the workflow contract for either IDE.

1. Cursor rule: copy `C:\repo\agent-skills\reference\rules\automate-before-manual.mdc` → `.cursor/rules/`.
2. VS Code rule: copy `C:\repo\agent-skills\.github\instructions\*.instructions.md` → `.github/instructions/`. Copilot only reads `.instructions.md` (with `applyTo`) there — `.mdc` is ignored.
3. Append to `AGENTS.md` the **Agent workflow (mandatory skills)** table from `C:\repo\agent-skills\skills\project-init\reference\agents-snippet.md` plus the **Agent automation (mandatory)** block from `C:\repo\agent-skills\reference\agents-automation-snippet.md`. Never replace a rich existing `AGENTS.md` — append or merge only.

Before any manual ask, agents must read **`automate-before-manual`**; if the user says lazy / "you do it", also **`dont-be-lazy`**.

## Run Superpowers on the Plan

Read `PLAN.md` and run the Superpowers pipeline **in this session**, without asking the user to re-send the prompt.

Skills are referenced below by plain name (`brainstorming`, `writing-plans`, …). Cursor's plugin exposes the same skills namespaced as `superpowers:<name>` — if only that form resolves in the current IDE, use it.

### Phase 1 — Brainstorming

**REQUIRED:** Read and follow `brainstorming`.

- Use `PLAN.md` → **Prompt** as the starting idea.
- Explore context, ask clarifying questions one at a time, propose approaches, and get design approval.
- Save the approved design to `docs/superpowers/specs/YYYY-MM-DD-<topic>-design.md`.
- Update `PLAN.md` **Notes** with any resolved constraints.

### Phase 2 — Writing Plans

**REQUIRED:** Read and follow `writing-plans`.

- Use the approved design spec (not the raw prompt alone) as input.
- Save the implementation plan to `docs/superpowers/plans/YYYY-MM-DD-<feature-name>.md`.
- Offer the execution choice from that skill (subagent-driven vs inline executing-plans).

### Phase 3 — Continue

When the user picks an execution mode:

- **Subagent-driven** → `subagent-driven-development`
- **Inline** → `executing-plans`

Continue until the plan is done or the user stops. Use `verification-before-completion` before claiming the work is finished.

## Hard Rules

- Only run when `/init-fmz` is explicitly invoked.
- Never depend on IDE-specific MCP tools (`cursor-app-control`). Every step must work with `git` + PowerShell alone, in Cursor and VS Code.
- Always ask for the project name; never invent it from the prompt.
- Always use `C:\faruk\<slug>` as the project root (not `~/Projects` or the current workspace).
- Always move the agent to `projectRoot` — or work with absolute paths — before writing files or installing skills.
- Never skip Superpowers brainstorming for "simple" projects.
- Always create the GitHub repo as **private** and push the first commit during `/init-fmz` (after scaffold files).
- Never expose or log PAT values; read only from `C:\faruk\secrets` or documented fallbacks.
- For VPS-deployed apps: production app secrets live in **GitHub Actions Secrets**, not in Git or manual VPS `.env` alone.
- Keep the user informed with short status updates between phases.
- Respond in Portuguese unless the user writes in another language.

## Usage

```
/init-fmz Quero um app de controle financeiro pessoal com Vue e FastAPI
```

```
/init-fmz
```

(empty prompt → agent asks for the idea, then continues)
