# Easy Apply — screening question answers

Never submit `undefined`, empty strings, or placeholder text. Every free-text answer must be **grounded in the loaded resume**.

## Rule

For **each** screening question in the Easy Apply modal:

1. **Read** the exact question from `browser_snapshot` (label, `aria-name`, or nearby heading).
2. **Load** the resume profile built in step §1 (`ResumeView.vue` or PDF).
3. **Draft the answer** as: *“What would {candidate name} answer to this question, based only on this CV?”*
   - Use the **agent’s own reasoning** (you are the model) — do not call external APIs unless the user configured one.
   - Write in the **language of the question** (English for US roles unless question is PT).
   - **2–5 sentences** for textareas; **one line** for short fields.
   - Cite **real** employers, stacks, and projects from the CV — no invented employers or degrees.
4. **Fill** the field with `browser_type` or CDP (`textarea.value` + `input` event). Verify snapshot shows non-empty value before Continue.
5. If the question requires facts **not in the CV** (visa status, clearance, exact salary expectation, legal authorization):
   - **Do not guess** and **do not** type `undefined` / `N/A` / `—`.
   - **Skip** the job; log: `Skipped — cannot answer honestly: {question}`.

## Prompt pattern (internal)

Use this mentally for every question:

```
Candidate: {name} — {title from CV}
Resume summary: {2–3 bullets from CV}
Job: {company} — {role title}

Question: "{exact question text}"

Write what this candidate would honestly answer, using only the resume. Professional tone. No fabrication.
```

## Example (Faruk default CV)

**Q:** *What about Lyra’s mission interests you?*

**A:** Lyra’s mission to expand evidence-based mental health care aligns with the kind of impact I look for in platform engineering: reliable systems that help people access care at scale. I’m motivated by work where strong APIs and data integrity directly support better outcomes.

**Q:** *Which primary technology do you have the most hands-on experience with?*

**A:** Java and Spring Boot — 20+ years, most recently a multi-tenant Orders microservice (Java 17, Spring Boot) with contract-first REST APIs, PostgreSQL, GCP Pub/Sub, GKE, OAuth2, and Elasticsearch-backed search.

**Q:** *Project you’re most proud of?*

**A:** The retail e-commerce Orders platform at BairesDev: I owned backend architecture and cross-stack delivery — event-driven integrations, multi-tenant schema design, production search at scale, and full SDLC including tests (JUnit, Playwright).

**Q:** *What is your current location?*

**A:** Curitiba, Paraná, Brazil

**Q:** *LinkedIn* (profile URL field)

**A:** https://www.linkedin.com/in/farukz

**Q:** *Current company*

**A:** BairesDev

**Q:** *Pretensão de honorários mensais em reais (PJ)?* / *Are you willing to work as PJ?*

**A:** Yes · R$ 35.000/month (adjust if user sets expectation in session)

**Q:** *Expected annual compensation (USD)?*

**A:** 110000 (mid-range for USD remote roles; adjust if user sets expectation)

**Q:** *Are you currently based in a Latin American country?*

**A:** Yes

## Multiple choice / dropdown

- Pick the option that **matches the CV** (years of experience, degree, English level).
- If no option fits without lying → skip job.

**Yes/No radios:** read the full question text from snapshot (often above the Yes/No pair). Example: *“Are you legally authorized to work in the United States?”* — if the CV does **not** state US work authorization, select **No** only when still applying; prefer **skip job** when authorization is a hard requirement and CV is Brazil-based with no visa/EOR mention.

## Hard stops (never auto-answer)

- “Are you authorized to work in the US?” — skip unless CV/user profile explicitly states authorization
- Security clearance, background check attestations you cannot verify
- Salary numeric fields — skip unless user set expectation in the same `/find-job` session

## Verification before Submit

After filling all visible required fields, snapshot once:

- No field value is empty, `undefined`, or generic filler (“See resume”).
- Then Continue → Review → Submit application.
