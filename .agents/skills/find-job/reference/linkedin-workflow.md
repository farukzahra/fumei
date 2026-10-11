# LinkedIn Jobs — browser workflow

Use with `cursor-ide-browser` MCP. LinkedIn changes often; prefer snapshot-driven steps over hard-coded selectors.

## Entry URLs

| Goal | URL |
| --- | --- |
| Jobs home | `https://www.linkedin.com/jobs/` |
| Search (template) | `https://www.linkedin.com/jobs/search/?keywords=KEYWORDS+international+worldwide&location=Worldwide&f_WT=2&f_AL=true` |
| Saved searches | `https://www.linkedin.com/jobs/collections/recommended` |

**Term:** **Easy Apply** (English) · **Candidatura simplificada** (Portuguese LinkedIn). Apply stays inside LinkedIn — no external careers site redirect.

## Login gate

1. Navigate to jobs home.
2. Snapshot: look for "Sign in", login form, or authenticated nav (Messaging, Me).
3. If unauthenticated → stop and ask user to log in.
4. User replies "logged in" → snapshot again before searching.

## Filters to apply (UI)

After search results load:

1. **Remote** — All filters → Remote → Apply
2. **Easy Apply** — All filters → Easy Apply / Candidatura simplificada → Apply (default for `/find-job`)
3. **Date posted** — Past week (or Past 24 hours for fresh listings)
4. **Experience level** — Match resume seniority (e.g. Associate through Executive for senior roles)
5. **Location** — **Worldwide** (default); avoid US-only search unless user asks
6. Before apply on any job — run [remote-geo-check.md](remote-geo-check.md) on full description (Dubai/US/EU-only remote → skip)

URL shortcuts (verify in current LinkedIn UI):

- `f_WT=2` — Remote
- `f_AL=true` — Easy Apply / Candidatura simplificada
- `f_TPR=r604800` — Past week
- `f_TPR=r86400` — Past 24 hours
- `f_E=4,5,6` — Mid-Senior, Director, Executive (numeric codes may shift)

## Collecting job cards

From snapshot YAML:

- Job title (link text)
- Company name
- Location line (Remote, United States, etc.)
- Posted time
- Job URL (`/jobs/view/...`)

Open each candidate job → snapshot description section.

Extract from description:

- Required skills
- Work authorization / location restrictions
- Salary range if shown
- Employment type (Full-time, Contract)

## Brazil-based candidate — geo signals

**Good:**

- Remote worldwide, work from anywhere
- Remote — Americas, LATAM-friendly
- International candidates welcome
- EOR partners (Deel, Remote.com) when hiring outside US

**Bad (usually skip unless user wants US-only):**

- Must be located in the United States
- US work authorization required, no sponsorship
- Security clearance
- On-site / hybrid mandatory

## Failure modes

| Symptom | Action |
| --- | --- |
| CAPTCHA | Stop automation; ask user to solve; retry once |
| Empty results | Broaden keywords; try Worldwide location |
| Login loop | User re-authenticates; clear tab and navigate again |
| Rate limit / 429 | Pause 2–5 min; reduce to 10 jobs |
| UI not in snapshot | Screenshot + describe blocker to user |

## Sample keyword bundles (adapt from resume)

For a senior Java/fullstack profile:

```
senior java remote
fullstack engineer remote typescript
solutions architect remote
staff engineer java kubernetes
```

Run 2–3 searches if the first query is too narrow.
