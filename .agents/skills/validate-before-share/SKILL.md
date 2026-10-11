---
name: validate-before-share
description: >-
  Verify URLs, previews, and endpoints yourself (HTTP 200, correct content)
  before sharing with the user. Use when about to send a link, deploy URL,
  local dev URL, or "open this to test".
disable-model-invocation: false
---

# Validate before share

Before sending the user any URL, preview, endpoint, build, or screen to open:

## 1. Verify yourself

- HTTP **GET** (or equivalent) on the URL
- Confirm **200** (or expected redirect chain ends OK)
- Confirm **content** matches intent: page title, hero text, JSON shape — not a generic health stub alone when the user cares about the product UI

## 2. Only share after pass

Do not say "try this link" or "should work" without evidence from step 1.

## 3. On failure

Fix or restart the environment, re-validate, then share. Report the **effective** URL (including port or path if different from default).

## 4. Production vs local

| Context | Minimum check |
|---------|----------------|
| Local dev | Title or documented health route |
| Production | User-facing URL from `docs/commit-push.json` or project docs; match `expectTitle` / `expectInBody` when configured |
| API only | Response body matches contract — not empty 200 |

Pair with **`finish-with-dev-server`** for local URLs after coding tasks.
