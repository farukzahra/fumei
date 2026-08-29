# Commit and push

Commit project changes and push to the tracked remote. Follow **fumei** conventions (Android/Kotlin, not financeiro web defaults).

## Preconditions

- User explicitly invoked `/commit-push` (or asked to commit and push).
- Never commit secrets (`C:\repo\secrets\`, keystores, `local.properties`, credentials).
- Never force-push to `main`/`master`.
- Never skip hooks unless the user explicitly asked.

## Step 1 — Inspect (parallel)

```powershell
git status
git diff
git diff --cached
git log -5 --oneline
git branch -vv
```

## Step 2 — Version bump (user-visible changes only)

**Only bump on `/commit-push` (or explicit release request).**

If the diff is **user-visible** (UI, behavior users notice, Play Store release):

1. Bump in `app/build.gradle.kts`:
   - `versionCode` → increment by 1 (required for Play Store)
   - `versionName` → semver (`1.2.0` feat, `1.1.1` fix)
2. Update `docs/release-history.json`:
   - `currentVersion`, `versionCode`, `updatedAt`
   - New entry at top with Portuguese `title`/`summary`, `type` (`feat`|`fix`|`chore`), `commit: null`
3. Update `docs/play-store/release-notes-pt-BR.txt` when shipping to Play Store.
4. Run `powershell -File scripts/release-check.ps1` (also runs in pre-commit hook).

Skip bump for internal-only refactors, docs-only, test-only, or CI-only with no user-facing change.

## Step 3 — Tests (TDD / verification)

Before commit, run:

```powershell
.\gradlew test
```

If `ui/` or user flows changed, also:

```powershell
.\gradlew connectedDebugAndroidTest
```

(emulator or device connected)

## Step 4 — Commit message

- Conventional Commits, **English**
- Subject ≤72 chars when possible

## Step 5 — Commit

```powershell
git add <relevant files>
git commit -m "<subject>" -m "<optional body>"
```

PowerShell: use multiple `-m` flags or a here-string. Do not use bash HEREDOC.

If nothing to commit, stop — do not push.

## Step 6 — Link release entry to commit

If `docs/release-history.json` was updated and the newest entry has `"commit": null`:

1. `git rev-parse --short HEAD`
2. Set `commit` on that entry
3. Second commit: `chore: link release entry to commit <sha>`

## Step 7 — Push

```powershell
git push origin HEAD
```

## Step 8 — Verify GitHub Actions CI

After push to `main`/`master`:

1. Read PAT from `C:\repo\secrets\github\pat.txt` (line starting with `ghp_`; never commit).
2. Poll `GET https://api.github.com/repos/farukzahra/fumei/actions/runs?per_page=3` with `Authorization: Bearer <PAT>`.
3. Wait until the run for this SHA completes (workflow **Android CI**).
4. If **test** or **build** fails: reproduce with `.\gradlew test assembleDebug` locally, fix, new commit/push, recheck.
5. Report run URL and final status to the user.

## Step 9 — Confirm

Report: commit SHA(s), messages, branch, new version (if bumped), Actions URL/status.
