# LinkedIn Easy Apply — automated submission

Run **every time** `/find-job` executes, after scoring. Term: **Easy Apply** (EN) · **Candidatura simplificada** (PT-BR).

## Which jobs to apply

| Tier | Action |
| --- | --- |
| **Strong match** + risk **low/medium** | **Apply** |
| **Possible** + risk **low** | **Apply** (if quota remains) |
| **Possible** + risk **medium** | Skip apply; list in report for manual review |
| **Weak**, **Skip**, risk **high** | Never apply |
| High fake-company risk | Never apply |
| External apply only | Never apply (not in `f_AL=true` results) |

Default **max submissions per run: 20** — apply eligible jobs in rank order, then **stop** (do not exceed 20). Stop earlier if LinkedIn shows captcha, verification, or repeated submit failures.

## Browser steps (per job)

1. Open job detail (already on page or click card).
2. Read **full description** + location line on card.
3. **Remote geo check** — [remote-geo-check.md](remote-geo-check.md). Only proceed if `REMOTE_BRAZIL_OK` or `REMOTE_LATAM_OK`.
4. Confirm CTA is **Easy Apply** / **LinkedIn Apply to …** / **Candidatura simplificada** — not company website.
5. Click apply button → modal opens.
4. Walk the modal with `browser_snapshot` after each step:
   - **Contact info** — use LinkedIn profile defaults; fix only if empty.
   - **Resume** — select latest PDF already on LinkedIn; if upload required, use resume path from skill (default `../faruk/.../Faruk Zahra - CV - Resume.pdf`).
   - **Cover letter** — optional; skip unless required field → use 3–4 sentence snippet from resume summary (English for US roles).
   - **Screening questions** — see [screening-answers.md](screening-answers.md): read each question, draft *what the candidate would answer from the CV* (never `undefined`/empty/guess on work auth). Skip job if honest answer impossible.
5. **Review** → **Submit application**.
6. Snapshot confirmation (“Application sent” / “Candidatura enviada”).
7. Record: job title, company, job ID/URL, status `applied` or `skipped`, reason.

## Submit button labels (EN / PT)

- Next / Avançar
- Review / Revisar
- Submit application / Enviar candidatura
- Done / Concluir

## Stop applying (whole run)

- CAPTCHA or “unusual activity”
- LinkedIn asks for phone/email verification mid-flow
- Same error 3 times in a row
- User session expired

Report partial results; do not retry blindly.

## Apply log in HTML report

Section **Applications submitted this run**:

| # | Company | Role | Status | Note |
| --- | --- | --- | --- | --- |
| 1 | Lyra Health | Sr. Software Engineer | Applied | Easy Apply |
| 2 | … | … | Skipped | US work auth required |

## Do not

- Pay fees, buy equipment, or follow crypto “interview” links
- Lie on work authorization, clearance, or visa questions
- Submit `undefined`, blank, or placeholder answers to required questions — see [screening-answers.md](screening-answers.md)
- Submit to more than the per-run cap without user approval
- Store confirmation screenshots with PII in the git repo
