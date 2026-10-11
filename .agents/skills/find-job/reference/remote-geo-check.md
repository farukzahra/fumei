# Remote geo check — before every Easy Apply

**Mandatory gate:** read the **full job description** (and location line on the card) **before** opening Easy Apply. LinkedIn’s **Remote** filter is not enough — many listings are remote **only from a specific country or region** (Dubai, US, UK, EU), not from Brazil/LATAM.

Candidate base: **Curitiba, Brazil**. Target: **true remote** hireable from **Brazil or LATAM**.

## Step-by-step (per job)

1. **Card location line** — e.g. `United States (Remote)`, `Dubai (Remote)`, `NAMER (Remote)`, `Worldwide`.
2. **Description** — scan for geo / work-mode phrases (lists below).
3. **Classify** remote eligibility:

| Classification | Apply? |
| --- | --- |
| **REMOTE_BRAZIL_OK** — worldwide, anywhere, LATAM, Americas incl. Brazil | Yes (if other gates pass) |
| **REMOTE_LATAM_OK** — LATAM / South America explicit | Yes |
| **REMOTE_UNCLEAR** — says “Remote” but no geo clarity | **Do not apply** — log `Skipped — remote scope unclear`; optional manual review in report |
| **REMOTE_REGION_LOCKED** — remote only from Dubai, US, UK, EU, specific country | **No** |
| **NOT_REMOTE** — hybrid, on-site, office days | **No** |

4. Record in report: `geo: REMOTE_*` + 1-line evidence quote from posting.

## Red flags — skip (region-locked “fake global remote”)

Phrases indicating remote **not** from Brazil:

| Pattern | Example |
| --- | --- |
| Must be in {country} | Must be located in the United States, UAE, Dubai, UK, Germany |
| {Country} remote only | Remote — US only, Remote USA, UK remote only |
| Work authorization | Must be authorized to work in the US / UK / EU |
| No sponsorship | No visa sponsorship, US citizens only |
| Office / hybrid | 2 days on-site, hybrid, within commuting distance |
| Timezone lock (narrow) | Must work US Eastern business hours only **without** international mention |
| MENA / GCC only | Based in Dubai, UAE residents, GCC countries only |
| EMEA-only | Must reside in EMEA (Brazil is not EMEA) |
| APAC-only | Must be in Singapore, India, etc. |

**Dubai / UAE example:** “Remote — Dubai” or “must be based in UAE” → **REMOTE_REGION_LOCKED** → skip.

**US example:** “Remote, USA #remoteLI” + US work auth → skip.

## Green flags — eligible from Brazil

| Pattern | Notes |
| --- | --- |
| Remote worldwide / work from anywhere | Strong yes |
| Global remote / any location | Strong yes |
| Remote — Americas / LATAM / Brazil | Strong yes |
| International candidates welcome | Good |
| Distributed team, location agnostic | Good |
| EOR (Deel, Remote.com, Oyster) + international hiring | Good signal |
| NAMER (Remote) | Usually OK for Brazil — verify description doesn’t say US-only |

## Yellow flags — do not apply automatically

- **Only** “United States (Remote)” on card with **no** worldwide/LATAM/international text in description → **REMOTE_UNCLEAR** or **REMOTE_REGION_LOCKED** → skip
- “Remote” + salary in USD but silent on location → **REMOTE_UNCLEAR** → skip unless description later confirms worldwide
- Multiple regions listed but Brazil/LATAM not included → skip

## LinkedIn card vs reality

| Card says | Often means | Action |
| --- | --- | --- |
| United States (Remote) | US-only remote | Skip unless description says international |
| Worldwide (Remote) | Often OK | Read description anyway |
| NAMER (Remote) | North America — check for US-only | Read description |
| Dubai, UAE (Remote) | UAE-based | Skip |
| European Union (Remote) | EU-only | Skip |

## Integration with scoring (§5)

- **REMOTE_BRAZIL_OK** → geo dimension full points
- **REMOTE_LATAM_OK** → geo dimension high
- **REMOTE_UNCLEAR** → tier **Skip** — never Easy Apply
- **REMOTE_REGION_LOCKED** / **NOT_REMOTE** → tier **Skip**

Increase **Remote / geo fit** weight to **30%** when classifying; a perfect skills match **cannot** override **Skip** geo.

## Order of gates (before Apply button)

```
Easy Apply filter (f_AL=true)
  → Read full description
  → Remote geo check (this doc)     ← MUST PASS
  → Fake company check
  → Resume score tier Strong/Possible
  → Screening questions answerable
  → Submit
```

## Report column

Add to each job row: **Geo class** + **Evidence** (short quote).

Example skipped rows:

- Lyra Health — `REMOTE_REGION_LOCKED` — “legally authorized to work in the United States”
- Acme — `REMOTE_REGION_LOCKED` — “Remote from Dubai office hub”
- Foo — `REMOTE_UNCLEAR` — “Remote” with no location policy in description
