# In-App Update Prompt Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Tell users installed through Google Play that a new version exists, download it in the background and offer a restart, with a manual entry point in the More tab.

**Architecture:** A new `update` package wraps Play In-App Updates behind `AppUpdater`. Pure Kotlin policy and label builders decide dialog and card state, so they are unit testable. `UpdateViewModel` combines the updater with a persisted dismissal flag. UI adds two dialogs plus a card in the More tab, wired from `MainActivity`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Play In-App Updates (`com.google.android.play:app-update:2.1.0`), JUnit 4, AndroidJUnit4 Compose E2E, Gradle.

**Spec:** `docs/superpowers/specs/2026-10-03-atualizacao-in-app-design.md`

## Global Constraints

- The app must not gain `android.permission.INTERNET`. Play Store does the networking.
- Play types stay inside `PlayAppUpdater.kt`. ViewModel and UI use `UpdateSignals`, `UpdateDialogKind`, `UpdateCardState`.
- Dialog copy: title "Tem atualização disponível", body "Baixa em segundo plano, sem sair do app. Depois você reinicia quando quiser.", buttons "Atualizar" and "Agora não".
- Restart copy: title "Atualização pronta", body "O download terminou. Reinicie o app para usar a versão nova.", buttons "Reiniciar" and "Depois".
- Card states: hidden when Play does not answer, "Você está na versão mais recente", "Versão nova disponível" + "Atualizar", "Baixando NN%" (plain "Baixando" when total is unknown), "Pronta para instalar" + "Reiniciar".
- Dismissal is one flag, cleared when Play reports no pending update. No re-arm.
- UI files require the Compose E2E first.
- No em dash in pt-BR copy.
- Gradle needs JDK 17: set `$env:JAVA_HOME='C:\Users\T-GAMER\.jdks\temurin-17.0.18'`.
- Instrumented tests filter with `-Pandroid.testInstrumentationRunnerArguments.class=<fqn>`; `connectedDebugAndroidTest` rejects `--tests`.

---

## File map

- Create `app/src/main/java/fumei/faruk/dev/br/update/UpdateSignals.kt`: enums, signals, pure policy and card labels.
- Create `app/src/main/java/fumei/faruk/dev/br/update/AppUpdater.kt`: interface, no Play imports.
- Create `app/src/main/java/fumei/faruk/dev/br/update/PlayAppUpdater.kt`: only file importing `com.google.android.play.*`.
- Create `app/src/main/java/fumei/faruk/dev/br/update/AppUpdaterProvider.kt`: default factory, swappable for debug.
- Create `app/src/main/java/fumei/faruk/dev/br/update/UpdateViewModel.kt` and `UpdateUiState.kt`.
- Modify `app/src/main/java/fumei/faruk/dev/br/data/UserPreferencesRepository.kt`: `UpdatePromptStore` interface and flag.
- Create `app/src/main/java/fumei/faruk/dev/br/ui/UpdateDialogs.kt`.
- Modify `MoreScreen.kt`, `FumeiApp.kt`, `MainActivity.kt`.
- Create `app/src/debug/java/fumei/faruk/dev/br/debug/FakeAppUpdater.kt`, modify `FumeiDebugApplication.kt`.
- Create `app/src/test/java/fumei/faruk/dev/br/update/UpdatePromptPolicyTest.kt`.
- Create `app/src/androidTest/java/fumei/faruk/dev/br/ui/UpdateFlowE2ETest.kt`.
- Create `scripts/check_no_internet_permission.py`, call it in `.github/workflows/android-ci.yml`.
- Modify `app/build.gradle.kts`, privacy policy, release notes, both release history files.

## Task 1: Policy and labels (unit TDD)

- [x] Write `UpdatePromptPolicyTest` covering: no dialog when nothing available; available dialog when not dismissed; no dialog when dismissed; restart dialog when downloaded; restart dialog suppressed when postponed; card hidden on Unknown; card labels per state; "Baixando 50%"; dismissal cleared only on None.
- [x] Run `.\gradlew.bat testDebugUnitTest --tests "fumei.faruk.dev.br.update.UpdatePromptPolicyTest"` and confirm it fails to compile, which is the RED.
- [x] Implement `UpdateSignals.kt` with `UpdateAvailability`, `UpdateSignals`, `UpdateDialogKind`, `UpdateCardState`, `UpdatePromptPolicy`, `UpdateCardLabels`, `UpdateCardUi`.
- [x] Rerun the focused test and confirm GREEN.

## Task 2: Updater abstraction (no test first, thin wrapper)

- [x] Add `implementation("com.google.android.play:app-update:2.1.0")` to `app/build.gradle.kts`.
- [x] Create `AppUpdater` interface: `check()`, `observeInstallState()`, `startFlexibleUpdate(activity)`, `completeUpdate()`.
- [x] Create `PlayAppUpdater` mapping Play enums to `UpdateSignals`, catching every failure as `UpdateSignals()` (Unknown).
- [x] Create `AppUpdaterProvider` with a swappable factory.
- [x] Run `.\gradlew.bat assembleDebug` and confirm the merged manifest still has no `android.permission.INTERNET`.

## Task 3: ViewModel and persistence

- [x] Add `UpdatePromptStore` and the `update_prompt_dismissed` flag to `UserPreferencesRepository`.
- [x] Create `UpdateUiState` and `UpdateViewModel` combining signals, dismissal and restart postponement.
- [x] Compile with `.\gradlew.bat compileDebugKotlin`.

## Task 4: UI (E2E first)

- [x] Write `UpdateFlowE2ETest` with `FakeAppUpdater`: available dialog appears and "Agora não" does not come back after reopening; "Atualizar" calls the flexible flow and the card shows "Baixando 50%"; downloaded state shows the restart dialog and "Reiniciar" calls `completeUpdate`; Unknown hides the card.
- [x] Run the focused E2E and confirm RED.
- [x] Create `FakeAppUpdater` in `src/debug`, override the provider in `FumeiDebugApplication` on emulators.
- [x] Create `UpdateDialogs.kt` with the testTags from the spec.
- [x] Add the card to `MoreScreen`, the dialog params to `FumeiApp`, and wire everything from `MainActivity`.
- [x] Rerun the focused E2E and confirm GREEN.

## Task 5: Guard the offline promise

- [x] Create `scripts/check_no_internet_permission.py` reading the merged manifest and failing when `android.permission.INTERNET` appears.
- [x] Call it in `.github/workflows/android-ci.yml` after `assembleDebug`.

## Task 6: Docs, version and ship

- [x] Privacy policy paragraph, then `.\scripts\deploy-privacy.ps1`.
- [x] `powershell -File scripts/bump-version-code.ps1 -VersionName 1.2.0`, rewrite the generated entry as `feat`, mirror to assets, update `docs/play-store/release-notes-pt-BR.txt`.
- [x] `.\gradlew.bat test connectedDebugAndroidTest`, `scripts/release-check.ps1`, `python scripts/check_release_history_encoding.py`.
- [x] `.\gradlew.bat installDebug`, open the app on Pixel6, confirm the update card appears in the More tab (fake updater on emulator).
- [x] Commit, push, check Android CI.
- [x] `powershell -File scripts/play-release.ps1` built versionCode 21, then `python scripts/play-upload.py --track production --status completed` published it with the service account in `C:\repo\secrets\google-play\service-account.json`.
