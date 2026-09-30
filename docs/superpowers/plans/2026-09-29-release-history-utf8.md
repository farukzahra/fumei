# Release History UTF-8 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Display Portuguese release-history text correctly by decoding the bundled JSON as UTF-8 and preventing charset ambiguity from recurring.

**Architecture:** Keep the existing JSON asset and `ReleaseHistoryRepository` data flow. Declare UTF-8 at Kotlin and PowerShell read boundaries. Validate that source and packaged history remain UTF-8, synchronized, and free of mojibake in the release script and CI.

**Tech Stack:** Kotlin, Android instrumentation tests, Gradle, Jetpack Compose app.

## Global Constraints

- Keep `docs/release-history.json` and `app/src/main/assets/release-history.json` synchronized.
- Files containing Portuguese text must be UTF-8; Kotlin and PowerShell readers must specify the charset explicitly.
- `scripts/check_release_history_encoding.py` must pass before bundle generation and in Android CI.
- The Play Store version bump and release-history entry are handled only in `/commit-push`.
- Verify on the Pixel6 Android 14 emulator and follow the repository’s `/commit-push` CI checks.

---

## File Map

- `app/src/main/java/fumei/faruk/dev/br/data/ReleaseHistoryRepository.kt` — reads and parses the bundled release-history JSON.
- `app/src/androidTest/java/fumei/faruk/dev/br/data/ReleaseHistoryRepositoryTest.kt` — verifies Portuguese strings loaded from the actual app asset.
- `scripts/check_release_history_encoding.py` — checks UTF-8, mojibake markers, and equality of docs and app asset.
- `scripts/tests/test_check_release_history_encoding.py` — covers valid text and rejection cases.
- `scripts/bump-version-code.ps1`, `scripts/release-check.ps1`, `scripts/play-release.ps1` — preserve and verify UTF-8 through release generation.
- `.github/workflows/android-ci.yml` — runs encoding tests and validation.
- `AGENTS.md` — persistent agent guidance for file encoding and charset checks.

The checked-in history and existing 2026-09-23 AAB contain valid UTF-8 text.
Windows PowerShell 5.1 reproduces corruption when it reads that JSON without
`-Encoding UTF8`, matching the mojibake in the screenshot.

### Task 1: Cover asset decoding and make charset explicit

**Files:**
- Create: `app/src/androidTest/java/fumei/faruk/dev/br/data/ReleaseHistoryRepositoryTest.kt`
- Modify: `app/src/main/java/fumei/faruk/dev/br/data/ReleaseHistoryRepository.kt`

**Interfaces:**
- Consumes: `ReleaseHistoryRepository(context: Context).load(): ReleaseHistory`
- Produces: `ReleaseHistory` whose title and summary values preserve UTF-8 Portuguese characters from the bundled asset.

- [ ] **Step 1: Write the real-asset integration test**

Create the test using the app context and the repository’s AndroidJUnit4 test pattern:

```kotlin
package fumei.faruk.dev.br.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseHistoryRepositoryTest {
    @Test
    fun load_preservesPortugueseAccentsFromBundledAsset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val history = ReleaseHistoryRepository(context).load()

        assertEquals("Sessões em gramas", history.entries.first().title)
        assertEquals(
            "Mais com Configurações e Sobre. Gramas por sessão e total em g na Home e Estatísticas. Botão Fumei agora com padrão 0,3 g.",
            history.entries.first().summary,
        )
        assertEquals("Lançamento na Play Store", history.entries.last().title)
    }
}
```

- [ ] **Step 2: Run the test to record baseline decoding**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=fumei.faruk.dev.br.data.ReleaseHistoryRepositoryTest`

Expected: the test passes because Kotlin's current `bufferedReader()` default is already UTF-8. The screenshot's defect is reproduced in Windows PowerShell 5.1 when it reads the JSON without `-Encoding UTF8`, covered in Task 2.

- [ ] **Step 3: Declare UTF-8 explicitly in the repository**

Change the reader to:

```kotlin
context.assets.open("release-history.json")
    .bufferedReader(Charsets.UTF_8)
    .use { it.readText() }
```

- [ ] **Step 4: Run the integration test again**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=fumei.faruk.dev.br.data.ReleaseHistoryRepositoryTest`

Expected: `BUILD SUCCESSFUL` and the instrumentation test passes with the exact title and summary strings.

- [ ] **Step 5: Include the reader and integration test in the release commit**

```powershell
git add app/src/androidTest/java/fumei/faruk/dev/br/data/ReleaseHistoryRepositoryTest.kt app/src/main/java/fumei/faruk/dev/br/data/ReleaseHistoryRepository.kt
```

### Task 2: Prevent PowerShell and release pipeline corruption

**Files:**
- Modify: `scripts/bump-version-code.ps1`
- Modify: `scripts/release-check.ps1`
- Modify: `scripts/play-release.ps1`
- Create: `scripts/check_release_history_encoding.py`
- Create: `scripts/tests/test_check_release_history_encoding.py`
- Modify: `.github/workflows/android-ci.yml`

**Interfaces:**
- Consumes: release history JSON and its app asset.
- Produces: a nonzero result for invalid UTF-8, mojibake, or unsynchronized JSON; release and CI stop on failure.

- [ ] **Step 1: Write tests for matching history and failure cases**

Cover valid accented JSON, mojibake, invalid UTF-8 bytes, and docs/asset mismatch. Run:

```powershell
python -m unittest discover -s scripts/tests -p "test_*.py" -v
```

Expected before implementation: import failure because `scripts.check_release_history_encoding` does not exist.

- [ ] **Step 2: Implement the validator and explicit PowerShell reads**

The validator must decode with `bytes.decode("utf-8")`, parse both JSON files, reject `Ã`, `Â`, and `�`, and compare parsed values. Set `-Encoding UTF8` on JSON reads in the bump and release-check scripts.

- [ ] **Step 3: Wire validation before bundle generation and in CI**

In `play-release.ps1`, sync the asset with `:app:copyReleaseHistory`, run the validator, and throw on nonzero exit before building. In `android-ci.yml`, run the Python tests and validator before Gradle.

- [ ] **Step 4: Run the Python tests and validator**

Run the same commands from Step 1, then `python scripts/check_release_history_encoding.py`.

Expected: all tests pass and output is `Release history UTF-8 and asset sync OK`.

- [ ] **Step 5: Commit the guard**

```powershell
git add scripts/check_release_history_encoding.py scripts/tests/test_check_release_history_encoding.py scripts/bump-version-code.ps1 scripts/release-check.ps1 scripts/play-release.ps1 .github/workflows/android-ci.yml
git commit -m "fix: guard release history UTF-8"
```

### Task 3: Record the charset rule for future changes

**Files:**
- Modify: `AGENTS.md`

**Interfaces:**
- Consumes: repository-specific agent guidance.
- Produces: a persistent instruction requiring UTF-8 source text and explicit decoding at read boundaries.

- [ ] **Step 1: Add the charset prevention rule**

Add this subsection under `## Idioma`, including a pointer to the validator:

```markdown
### Charset

- Arquivos Kotlin, JSON e textos com conteúdo em português devem ser salvos em UTF-8.
- Ao ler assets ou outros streams de texto, declarar `Charsets.UTF_8` explicitamente.
- O CI e `scripts/play-release.ps1` validam o histórico em `scripts/check_release_history_encoding.py`.
```

- [ ] **Step 2: Check the documentation diff**

Run: `git diff --check`

Expected: no whitespace errors; the added instruction appears under `## Idioma`.

- [ ] **Step 3: Commit the agent guidance**

```powershell
git add AGENTS.md
git commit -m "docs: add UTF-8 agent guidance"
```

### Task 4: Verify the release package and app behavior

**Files:**
- Verify: `docs/release-history.json`
- Verify: `app/src/main/assets/release-history.json`
- Verify: `app/src/main/java/fumei/faruk/dev/br/ui/AboutScreen.kt`

**Interfaces:**
- Consumes: the committed decoder fix and repository encoding guidance.
- Produces: a verified Play Store bundle and app display with intact Portuguese text.

- [ ] **Step 1: Run project tests**

Run: `.\gradlew.bat test`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Generate the Play Store bundle through the documented release script**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\play-release.ps1`

Expected: successful release validation and a generated `app\build\outputs\bundle\release\app-release.aab` with an incremented `versionCode`.

- [ ] **Step 3: Install and launch the debug app on Pixel6**

Run:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
.\gradlew.bat installDebug
& $adb shell am start -n fumei.faruk.dev.br/fumei.faruk.dev.br.MainActivity
& $adb shell dumpsys activity activities | Select-String "mResumedActivity"
```

Expected: the foreground activity is `fumei.faruk.dev.br.MainActivity`; inspect the Mais tab and confirm release titles/summaries show valid Portuguese accents.

- [ ] **Step 4: Execute the documented `/commit-push` workflow**

Run the repository command at `.cursor/commands/commit-push.md`: check the diff, apply the release-history/version steps for this user-visible fix, push the branch without force-pushing, wait for `android-ci.yml`, and verify the configured Play Store page.

Expected: commits are pushed, Android CI succeeds, and the Play Store verification matches the repository configuration. If the Play Console upload step is gated by credentials/approval, report the exact blocker rather than claiming publication succeeded.
