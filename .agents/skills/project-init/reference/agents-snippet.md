## Agent workflow (mandatory skills)

| Phase | Skill / command | Rule |
|-------|-----------------|------|
| Design | `brainstorming` | No feature code until spec approved → `docs/superpowers/specs/` |
| Plan | `writing-plans` | `docs/superpowers/plans/YYYY-MM-DD-*.md` |
| Recurring guardrails | `dont-forget` | Prefer CI/hooks/codegen over "remember to…" |
| Automation | `automate-before-manual` | `../secrets/` before manual steps |
| Share links | `validate-before-share` | HTTP 200 + content before sending URL |
| Local test | `finish-with-dev-server` | Documented ports; conflict → kill own / next port |
| Architecture | `ask-before-architecture` | Ask before stack decisions |
| Diagrams | `diagrams-mermaid` | Mermaid + validate before show |
| UI / bugs | `ui-change-e2e` | E2E on screen change or bugfix |
| Build | stack skills + `tdd` | Per project stack |
| Verify | `verification-before-completion` | Evidence before "done" |
| Debug | `systematic-debugging` | Root cause before fix |
| Ship | `/commit-push` | `semantic-version` → `caveman-commit` → push → Actions → prod URL |
| Handoff | `/recap` + `recap` | Session summary HTML when closing a long chat |

**Gates:** no feature without approved spec; no "done" without verification; version bump only on `/commit-push`; obligations that must not be forgotten → automate (`dont-forget`).

Invoke `/init` to (re)bootstrap skills and folders.
