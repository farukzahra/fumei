---
name: find-job
description: >-
  Search LinkedIn Jobs in the Cursor browser using the user's resume, verify
  true remote eligibility from Brazil/LATAM, apply worldwide remote filters,
  flag suspicious employers, and submit Easy Apply automatically. Use when the
  user invokes /find-job or asks to find and apply to remote jobs on LinkedIn.
disable-model-invocation: false
---

# /find-job — LinkedIn job search from resume

Find **remote, international** roles on **LinkedIn Jobs** (work from Brazil — **not** jobs that require living or working in the USA). Prefer **USD** when listed. The user logs in manually; the agent drives search, filtering, scoring, **Easy Apply submission**, and reporting.

**Every `/find-job` run must attempt applications** on eligible **Easy Apply** jobs (see §7), not search-only.

## When to use

- User invokes **`/find-job`**
- User asks to search LinkedIn for **remote international** jobs from Brazil/LATAM (not US-residency, not Dubai/UAE-only, etc.)
- User wants help avoiding suspicious or fake job postings

## Hard limits (tell the user upfront if relevant)

- **Login is manual** — LinkedIn blocks automated sign-in; pause until the user confirms they are logged in.
- **USD salary is not guaranteed** — LinkedIn often shows estimates; treat salary as a signal, not proof.
- **Fake-company detection is heuristic** — score risk; never claim 100% certainty.
- **Fragile automation** — LinkedIn UI changes and anti-bot measures can break steps; prefer small batches (first 1–2 result pages).
- **Personal use only** — do not scrape at high volume or store LinkedIn credentials.

## Workflow

### 1. Resolve resume input

If the user did **not** attach or name a resume path in the same message:

1. Ask: *Where is your resume? (paste text, file path, or say "default")*
2. If they say **default** or give no path after asking, load Faruk's default:

| Priority | Path (relative to workspace) | Notes |
| --- | --- | --- |
| 1 | `../faruk/frontend/public/assets/Faruk Zahra - CV - Resume.pdf` | Generated PDF |
| 2 | `../faruk/frontend/src/views/ResumeView.vue` | Structured source (skills, summary, titles) |
| 3 | `../faruk/frontend/dist/index.html` + `/resume` route | Only if built site is needed |

Read the file with `Read`. For PDF, extract text from readable content; if poor, fall back to `ResumeView.vue`.

Build a **resume profile** (keep in memory for this session):

- Target titles (e.g. Senior Fullstack Engineer, Java Architect)
- Top skills / keywords (10–20)
- Years of experience, seniority band
- Languages (English level matters for US roles)
- Locations to **avoid**: US-only remote, must live in US, US work authorization required (see [reference/candidate-geo-defaults.md](reference/candidate-geo-defaults.md))

### 2. Confirm search parameters

If not specified, **ask once** with defaults below. User may answer in Portuguese.

| Parameter | Default (Faruk-style) | Options |
| --- | --- | --- |
| Work mode | Remote | Remote, Hybrid (only if user asks) |
| Geography | **Worldwide** international remote (from Brazil) | Worldwide, LATAM, Global — **not** US-residency jobs |
| US work authorization | **No** (Faruk default) | User may override |
| Compensation | Prefer USD when listed | USD, EUR, any |
| Seniority | From resume | Entry, Mid, Senior, Staff |
| **Apply channel** | **Easy Apply only** | Easy Apply, any |
| Keywords | From resume titles + top skills | User override |
| Max jobs to analyze | 25 | 10–50 |
| **Max Easy Apply submits** | **20 per run** | 1–20 |

Record the final parameters before opening the browser.

### 3. Open LinkedIn Jobs (Cursor browser)

Use **`cursor-ide-browser`** MCP:

1. `browser_tabs` → list tabs
2. `browser_navigate` → `https://www.linkedin.com/jobs/`
3. `browser_snapshot` → check login state

**If not logged in:**

- Tell the user: *Please log in to LinkedIn in this tab, then reply "logged in".*
- Do **not** ask for their password.
- After confirmation, `browser_snapshot` again and verify feed/search UI (not login form).

### 4. Run search with filters

Build a search URL or use UI filters. Prefer URL for repeatability:

```
https://www.linkedin.com/jobs/search/?keywords={encoded_keywords}%20international%20worldwide&location=Worldwide&f_WT=2&f_AL=true&f_TPR=r604800
```

Geo rules and search defaults: [reference/candidate-geo-defaults.md](reference/candidate-geo-defaults.md). **Do not** default to `location=United States`.

| Filter | LinkedIn UI (EN) | LinkedIn UI (PT-BR) | URL param (when stable) |
| --- | --- | --- | --- |
| Remote | Remote | Remoto | `f_WT=2` |
| **Easy Apply** | **Easy Apply** | **Candidatura simplificada** | `f_AL=true` |
| Date posted | Past week | Última semana | `f_TPR=r604800` |
| Experience | Senior / Director | … | `f_E=4,5,6` (verify in UI) |

**Default:** only analyze jobs where apply is **on LinkedIn** (Easy Apply). Skip listings whose primary CTA is **Apply on company website** / **Candidatura no site da empresa** unless the user explicitly opts out.

When collecting results, prefer jobs whose action button reads **Easy Apply**, **Apply** (LinkedIn modal), or **Candidatura simplificada** — not **Apply on company website**.

For each results page (max 2 pages unless user asks for more):

1. `browser_snapshot` → collect job cards (title, company, location line, link)
2. Open each candidate → read **full description**
3. Run **remote geo check** (§5) — **before** scoring for apply
4. Append only jobs that pass geo gate (or mark Skip with evidence)

Stop early if LinkedIn shows captcha, rate limit, or repeated failures — report and switch to manual review of collected links.

### 5. Remote geo verification (required before apply)

Many listings say **Remote** but mean remote **only from the US, Dubai/UAE, EU, etc.** — not from Brazil.

Full rules: [reference/remote-geo-check.md](reference/remote-geo-check.md).

For **every** job, before Easy Apply:

1. Read location on card + full description.
2. Classify: `REMOTE_BRAZIL_OK` | `REMOTE_LATAM_OK` | `REMOTE_UNCLEAR` | `REMOTE_REGION_LOCKED` | `NOT_REMOTE`.
3. **Only** `REMOTE_BRAZIL_OK` or `REMOTE_LATAM_OK` may proceed to apply.
4. Log classification + short evidence quote in the report.

**Skip apply** examples:

- Remote Dubai / UAE / GCC only
- Remote US only + US work authorization
- Hybrid or on-site
- “Remote” with **no** geo policy → `REMOTE_UNCLEAR` (do not guess)

### 6. Score each job against the resume

For each job, compute:

| Dimension | Weight | Notes |
| --- | --- | --- |
| Title / seniority match | 25% | Align with resume titles |
| Skills overlap | 30% | Stack in description vs resume |
| Remote / geo fit | 30% | Must be `REMOTE_BRAZIL_OK` or `REMOTE_LATAM_OK` — see §5 |
| Compensation signal | 10% | USD mentioned, salary range if present |
| Company trust | 5% | From fake-company heuristics (below) |

Output tiers: **Strong match**, **Possible**, **Weak**, **Skip**.

**Hard rule:** if geo class is `REMOTE_REGION_LOCKED`, `REMOTE_UNCLEAR`, or `NOT_REMOTE` → tier **Skip** regardless of skills score.

Penalize heavily:

- Must be US citizen / no sponsorship
- On-site or hybrid required
- Clear timezone lock incompatible with Brazil
- High fake-company risk

See [reference/fake-company-heuristics.md](reference/fake-company-heuristics.md) and [reference/remote-geo-check.md](reference/remote-geo-check.md).

### 7. Fake / suspicious company check

Apply heuristics from [reference/fake-company-heuristics.md](reference/fake-company-heuristics.md). For each employer, assign:

- **Risk: low / medium / high**
- 1–3 bullet reasons

Optional: open company LinkedIn page in browser and note employee count, founded date, website link — only when high-value candidates need verification.

Never pay anything. Warn if posting asks for upfront payment or crypto.

### 8. Easy Apply — automatic submission (required)

**On every `/find-job` execution**, after §5 geo check + §6 scoring + §7 fake-company check, submit applications for eligible jobs.

Full browser steps: [reference/easy-apply-workflow.md](reference/easy-apply-workflow.md).

**Apply when all are true:**

- Geo class **`REMOTE_BRAZIL_OK`** or **`REMOTE_LATAM_OK`** (§5 — verified in description)
- Listing is **Easy Apply** / **Candidatura simplificada** (`f_AL=true` results only)
- Match tier **Strong match**, or **Possible** with company risk **low**
- Company risk is **not high**
- Screening questions can be answered **truthfully** from the resume (see [reference/screening-answers.md](reference/screening-answers.md))

**Screening answers (required):**

1. Snapshot each question text in the Easy Apply modal.
2. Using the loaded CV, write what the candidate would answer — **you are the model**; no `undefined`, no empty required fields, no invented facts.
3. Fill with `browser_type` or CDP; confirm value visible before Continue.
4. Skip job only when honesty requires info absent from CV (work auth, clearance, salary not provided).

**Skip apply** (log reason in report) when:

- **`REMOTE_REGION_LOCKED`** — e.g. remote US/Dubai/UAE/EU only
- **`REMOTE_UNCLEAR`** — “Remote” without Brazil/LATAM/worldwide evidence
- **`NOT_REMOTE`** — hybrid or on-site
- External / company-website apply only
- High fake-company risk
- US-only / no sponsorship if candidate is Brazil-based and posting excludes international
- Required question cannot be answered honestly
- Per-run submit cap reached (default **20** — stop applying and finish report)
- LinkedIn blocks with captcha or verification

**Flow:**

1. Sort candidates: Strong match first, then Possible (low risk).
2. For each job until cap reached (**max 20**, then **stop**): open → **§5 geo check** → Easy Apply modal → contact → resume → screening → Submit.
3. Track `applied` | `skipped` + reason per job.
4. If login expired, try credentials from `../secrets/linkedin/farukz.txt` only after user stored them there; otherwise pause for manual login.

### 9. Deliver report

Produce a **self-contained HTML** file (dark mode, inline CSS — same quality bar as `recap`):

Save to:

- `docs/job-search/YYYY-MM-DD-find-job-<slug>.html` when `docs/` exists
- otherwise `find-job-report.html` at workspace root

Include:

1. Search parameters used
2. Resume source path (no secrets)
3. Summary counts (found / strong / possible / skipped / high-risk companies)
4. Table or cards: rank, title, company, location, **geo class**, match tier, risk, LinkedIn URL, **apply status**, 2-line rationale
5. Section **Applications submitted this run** (applied vs skipped + reasons)
6. Section **High-risk postings to avoid**
7. Section **Recommended next steps** (profile tweaks, manual follow-ups)

Reply with file path, **apply summary** (e.g. 3 applied, 2 skipped), and top matches in markdown.

### 10. Optional follow-up

If user asks, prepare:

- Short LinkedIn Easy Apply talking points from resume
- Cover letter snippet for top match
- Saved search keywords for next session

Do not store LinkedIn session cookies in repo files.

## Browser discipline

Follow `cursor-ide-browser` rules:

- `browser_navigate` → `browser_lock` → actions → `browser_lock` unlock
- Prefer `browser_snapshot` over blind clicks
- Max 4 failed actions per step; then stop and report blocker

Detailed LinkedIn steps: [reference/linkedin-workflow.md](reference/linkedin-workflow.md).

## Default resume (Faruk)

When user says **default** or does not specify a path after you ask:

- PDF: `../faruk/frontend/public/assets/Faruk Zahra - CV - Resume.pdf`
- Source: `../faruk/frontend/src/views/ResumeView.vue`

Details: [reference/default-resume-paths.md](reference/default-resume-paths.md).

## Project slash command

Consumer repos should add `.cursor/commands/find-job.md`:

```markdown
# /find-job

User invoked **`/find-job`**. Read and follow the **`find-job`** skill
(`~/.cursor/skills/find-job/SKILL.md` or `.agents/skills/find-job/SKILL.md`).

If no resume path was given, ask where the CV is; if unspecified, use the default from `../faruk`.
Open LinkedIn in the Cursor browser; user logs in manually; **submit Easy Apply for eligible matches**; deliver ranked HTML report under `docs/job-search/`.
```

## Do not

- Commit LinkedIn cookies, session tokens, or passwords
- Claim guaranteed USD salary or visa sponsorship
- Lie on Easy Apply screening questions (work auth, clearance, etc.)
- Submit `undefined`, empty, or fabricated screening answers — use CV-grounded drafts per [reference/screening-answers.md](reference/screening-answers.md)
- Apply to high-risk fake postings or external-only listings
- Scrape more than needed for the report (default cap: 25 jobs analyzed)
