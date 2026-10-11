# Default resume paths (Faruk)

Used when `/find-job` runs without an explicit CV path and the user says **default** or does not specify after being asked.

Repo: `../faruk` relative to most workspaces under `C:\repo\`.

## Primary

| File | Purpose |
| --- | --- |
| `../faruk/frontend/public/assets/Faruk Zahra - CV - Resume.pdf` | PDF export sent to recruiters |
| `../faruk/frontend/src/views/ResumeView.vue` | Canonical structured content (skills, experience, summary) |

## Regenerate PDF (optional)

From `../faruk`:

```bash
npm run pdf
```

Writes/updates `frontend/public/assets/Faruk Zahra - CV - Resume.pdf`.

## Parsing strategy

1. Try PDF via `Read` tool.
2. If text is incomplete, read `ResumeView.vue` — extract:
   - `skillGroups`
   - `previousRoles`
   - summary paragraphs in template
   - title line: "Senior Fullstack Engineer"
3. Do not commit resume content into agent-skills repo.

## Live preview (optional validation)

If `../faruk` dev server is running, resume page is typically at `/resume` on the site documented in that project's `AGENTS.md` / README.
