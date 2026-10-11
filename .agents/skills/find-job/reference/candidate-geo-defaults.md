# Candidate geo defaults (Faruk)

Persist these unless the user overrides in the same `/find-job` session.

## What the candidate wants

| Preference | Value |
| --- | --- |
| Work mode | **Remote only** — never on-site or hybrid |
| Physical location | **Does not want to work in the USA** — remote from Brazil (Curitiba) |
| Target market | International / worldwide remote; USD compensation is a plus |
| US work authorization | **No** — not legally authorized to work in the US (user confirmed: “acho que não”) |

## Skip before apply (description or screening)

Auto-**Skip** tier and **do not Easy Apply** when the posting or modal includes:

- Must be located in the United States / must reside in the US
- US work authorization required / no sponsorship
- US citizen or green card only
- Security clearance (US)
- On-site or hybrid in the US
- “Remote — USA only” without international/LATAM/worldwide wording
- **Remote locked to other regions** — Dubai/UAE/GCC, UK-only, EU-only, APAC-only, “must be based in {country}”
- Card says **Remote** but description never allows Brazil/LATAM/worldwide → `REMOTE_UNCLEAR`

Full checklist: [remote-geo-check.md](remote-geo-check.md).

## Good signals (boost score)

- Remote worldwide / work from anywhere
- Remote — Americas / LATAM-friendly / international candidates welcome
- Employer of record (Deel, Remote.com) hiring outside US
- NAMER / global distributed team

## LinkedIn search (default)

Primary search — **not** `location=United States`:

```
keywords=senior java spring remote international worldwide
location=Worldwide
f_WT=2&f_AL=true
```

Secondary pass if thin results:

```
keywords=remote software engineer LATAM international
location=Worldwide
```

## Screening — US authorization question

When Easy Apply asks *“Are you legally authorized to work in the United States?”* (or similar):

- Answer: **No** (matches user default)
- If the form **requires Yes** to continue → **Dismiss** modal; log `Skipped — US work authorization required`
- Do **not** answer Yes to force submission

## Compensation

Prefer USD ranges when listed; absence of salary is not a skip reason.
