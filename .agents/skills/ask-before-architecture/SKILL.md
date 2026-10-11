---
name: ask-before-architecture
description: >-
  Do not choose stack, framework, or architecture alone — ask the user with
  2–4 concrete options and a recommendation. Use for greenfield work, major
  dependencies, or specs with open implementation decisions.
disable-model-invocation: false
---

# Ask before architecture

Before implementing or locking **technical decisions**, do not choose alone. Ask and wait.

## Counts as a decision (must ask)

- Language, framework, UI library, state management
- ORM, migration strategy, API style (REST vs RPC)
- Test stack, monorepo layout when alternatives exist
- Default ports or proxy vs CORS when not already in ADR/spec

## Does not need asking

- Following an **approved** spec or ADR in this repo
- Reading existing conventions from `AGENTS.md`, `docs/stack.md`, code

## How to ask

1. Use **structured choices** (`AskQuestion` when available): 2–4 options, mark one **(Recommended)** with one-line why.
2. **One decision per message** unless the user asked to decide everything at once.
3. **Stop** until the user answers — do not implement in the same turn.
4. Record the answer in `docs/superpowers/specs/` or an ADR before coding.

## Forbidden shortcuts

- "Same as the last project" without confirmation
- Publishing a spec with invented Implementation Decisions after no discussion
- Inferring stack from a single example file
