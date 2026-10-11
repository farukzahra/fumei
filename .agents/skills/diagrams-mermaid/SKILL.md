---
name: diagrams-mermaid
description: >-
  Use Mermaid for flow and architecture diagrams; validate syntax before showing
  the user. Never hand-drawn ASCII diagrams. Use in specs, ADRs, README, or
  when explaining flows.
disable-model-invocation: false
---

# Diagrams — Mermaid

## Rules

1. Use **Mermaid** (`flowchart`, `sequenceDiagram`, `stateDiagram`, `erDiagram`) in Markdown and specs.
2. **Never** hand-draw ASCII diagrams (boxes with `|---|`, trees in plain text).
3. Update diagrams when the flow changes.
4. Exception: terminal commands and config snippets are not diagrams.
5. Prefer `flowchart` / `sequenceDiagram`; avoid `block-beta` (poor viewer support).

## Validate before showing the user

**Every Mermaid block must parse successfully** before you send the doc or message.

### Workflow

1. Write the Mermaid block.
2. **Validate syntax:**
   ```bash
   # Write block to a temp .mmd file, then:
   npx -y @mermaid-js/mermaid-cli -i diagram.mmd -o diagram.svg
   ```
   On Windows if `-o` fails, use `-o nul` or `-o diagram.svg` in a temp folder.
3. If validation **fails**, fix the diagram and re-run — do not show broken Mermaid to the user.
4. Optional quick check: [mermaid.live](https://mermaid.live) only when CLI is unavailable; still fix errors before final output.

## In specs

Store diagrams in `docs/superpowers/specs/` or `docs/` alongside prose — they are part of the approved design.
