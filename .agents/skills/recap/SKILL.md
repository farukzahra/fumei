---
name: recap
description: >-
  Summarize all user requests from the current AI chat session and produce a
  concise rich HTML recap (requested vs delivered). Use when the user invokes
  /recap or asks for a session summary, delivery recap, or handoff document.
disable-model-invocation: false
---

# /recap — Session delivery recap

Produce a **single self-contained HTML file** that recaps the conversation: what the user asked for (brief paraphrase) and what was actually delivered.

## When to use

- User invokes **`/recap`**
- User asks for session summary, recap, handoff, or "what was asked vs delivered"
- End of a long multi-topic agent session before closing

## Workflow

1. **Read the full conversation** (all user messages, chronological). Group related turns into topics; do not list every micro-message.
2. **For each topic**, write at most two short blocks:
   - **Requested** — user intent in plain language (1–2 sentences). Use the project's UI language (Portuguese for TerapIA unless the project is English-only).
   - **Delivered** — concrete outcomes: files, routes, scripts, rules, tests; include paths when helpful.
3. **Skip** failed or abandoned attempts unless the user should know about them.
4. **Generate HTML** using the template below (inline CSS only, no external dependencies).
5. **Ensure local dev server** when the project rules require it (e.g. `AGENTS.md`, user rules):
   - Read repo docs for start commands (TerapIA: `npm run db:up` + `npm run dev` → default `http://localhost:3000`).
   - If down, start it before finishing the recap.
   - Validate HTTP 200 on health or main route; record the **effective URL** (port may differ).
   - Include test login hints only if documented in the repo (never secrets from `.env`).
6. **Save** to `docs/session-recaps/YYYY-MM-DD-<slug>.html` when the project has a `docs/` folder; otherwise `session-recap.html` at workspace root or a path the user specifies.
7. **Reply** with the file path, local URL, and a short markdown table summary (optional).

## UI design (required)

Before generating HTML, apply **anti-ui-slop** and **frontend-design**:

- **No generic slop**: avoid cream `#F4F1EA` templates, default Inter/system-only stacks, and numbered cards without meaning.
- **Dark mode by default** for recap HTML (`color-scheme: dark`, `meta color-scheme`).
- **Ground in the product**: when the repo has design tokens (e.g. TerapIA `dashboard-theme-presets.ts` → `petroleo-atual`), reuse primary, bg, ink, muted — do not invent a unrelated palette.
- **Typography**: pair a restrained serif display (e.g. Literata) with a clean sans body (e.g. IBM Plex Sans); not Arial/Inter alone.
- **Hierarchy**: eyebrow → title → local test panel (pill link grid + health dot) → **clickable topic cards** (mark as seen) → pending footer.
- **Accessibility**: visible `:focus-visible`, `prefers-reduced-motion`, sufficient contrast on dark surfaces.

## Topic cards — marcar como visto (required)

Each topic card is a **conversation chunk** the user can click to mark as read. State persists in `localStorage` (per file path + topic id).

1. **Markup** — every topic:
   ```html
   <article class="topic" data-topic-id="slug-curto-do-topico">
     <h2>Título do tópico</h2>
     ...
   </article>
   ```
   Use a stable kebab-case `data-topic-id` (slug from title). Required for persistence.

2. **Progress chip** in header stats:
   ```html
   <span class="chip chip--progress" data-recap-progress hidden>0/0 vistos</span>
   ```

3. **CSS** — include styles from `reference/topic-seen.css` (or equivalent inline). Seen state: `.topic--seen` — lower opacity, desaturated, muted left bar, **"Visto"** pill top-right.

4. **JS** — before `</body>`, inline the script from `reference/topic-seen.js`:
   - Click or Enter/Space toggles seen
   - `role="button"`, `aria-pressed`, `tabindex="0"`
   - Storage key: `recap-seen:{pathname}:{data-topic-id}`
   - Updates `[data-recap-progress]` counter

5. **UX copy** (optional hint below topics title): *Clique em um card para marcar como visto.*

Reference files (same folder as this skill): `reference/topic-seen.css`, `reference/topic-seen.js`.

## HTML template

Keep copy short; max ~8–12 topic cards. Label text follows project locale (PT-BR: **Pedido** / **Entregue** / **Pendências**).

Use this dark token set when no project tokens exist:

| Token | Default |
|-------|---------|
| `--bg-page` | `#102223` |
| `--bg-card` | `#16302f` |
| `--primary` | `#2d6a6b` |
| `--secondary` | `#7fb5b0` |
| `--ink` | `#f2f5f4` |
| `--muted` | `#9bb0ae` |

Structure (inline CSS + optional Google Fonts):

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <meta name="color-scheme" content="dark" />
  <title>Recap — {Project} — {Date}</title>
  <!-- Literata + IBM Plex Sans or project-appropriate pair -->
  <style>/* dark tokens, radial glow on body::before, card shadows */</style>
</head>
<body>
  <div class="wrap">
    <p class="eyebrow">{Project} · sessão agente</p>
    <header>...</header>
    <section class="local-env"><!-- pill link grid + health dot when dev server required --></section>
    <div class="topics">
      <p class="topics-hint">Clique em um card para marcar como visto.</p>
      <article class="topic" data-topic-id="example-topic">...</article>
    </div>
    <section class="pending">...</section>
    <footer>Generated by /recap</footer>
  </div>
  <script>/* inline topic-seen.js */</script>
</body>
</html>
```

## Quality bar

- **Concise**: each topic fits one card; no code dumps in HTML.
- **Accurate**: only claim delivery for work that was implemented or explicitly output.
- **No secrets**: never include tokens, passwords, or `.env` values.

## Optional follow-up

If the recap reveals unfinished work, add a **Pending** section at the bottom (plain list).
