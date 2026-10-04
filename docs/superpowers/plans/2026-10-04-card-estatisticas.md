# Stats Card Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Replace the stacked text summary in the stats card with two labelled numbers and a labelled average, using the app palette instead of the inherited Material purple.

**Architecture:** `stats` exposes scope captions instead of pre-formatted sentences. `StatsUiState` carries three `StatsSummaryStat` blocks. `StatsPeriodHeader` renders value above caption, with a vertical divider, a horizontal divider before the average, and explicit `AppColors`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, JUnit 4, AndroidJUnit4 Compose E2E.

**Spec:** `docs/superpowers/specs/2026-10-04-card-estatisticas-design.md`

## Global Constraints

- Captions: `sessões no mês`, `sessões no ano`, `sessões no total`, `gramas no mês`, `gramas no ano`, `gramas no total`, `média entre sessões do mesmo dia`.
- Captions render uppercase with letter spacing; values use the Fraunces display style.
- Average only in the monthly scope. Without a same-day pair the value is `sem dados`.
- Card colors: `Ash850` container, `Ash750` border, values `Paper100` and `Ember500`, average `Ember300`, captions `Smoke400`.
- Keep testTags `stats_period_title`, `stats_prev_button`, `stats_next_button`; add `stats_summary_sessions`, `stats_summary_grams`, `stats_summary_average`.
- No em dash in pt-BR copy.
- UI change needs the Compose E2E first.
- Gradle needs JDK 17: `$env:JAVA_HOME='C:\Users\T-GAMER\.jdks\temurin-17.0.18'`.
- Instrumented filter: `-Pandroid.testInstrumentationRunnerArguments.class=<fqn>`.

## Task 1: Scope captions and summary values

- [x] Update `StatsGramsAggregatorTest`: replace the `periodGramsLabel` test with caption tests for the three scopes.
- [x] Run `.\gradlew.bat testDebugUnitTest --tests "fumei.faruk.dev.br.stats.StatsGramsAggregatorTest"` and confirm RED.
- [x] In `StatsAggregator.kt` replace `periodGramsLabel` with `periodCountCaption(scope)` and `periodGramsCaption(scope)`.
- [x] Add `StatsSummaryStat` and switch `StatsUiState` fields; build the three blocks in `StatsViewModel`.
- [x] Rerun the focused test and confirm GREEN.

## Task 2: Layout (E2E first)

- [x] Update `FumeiAppE2ETest`: month summary shows the session value with caption `SESSÕES NO MÊS` and grams with `GRAMAS NO MÊS`; the monthly average is a value with caption `MÉDIA ENTRE SESSÕES DO MESMO DIA`; the no-pair month shows `sem dados`.
- [x] Run the focused E2E and confirm RED.
- [x] Rewrite `StatsPeriodHeader` with `SummaryStatBlock`, dividers and explicit `AppColors`.
- [x] Rerun the focused E2E and confirm GREEN.

## Task 3: Docs, verify and ship

- [x] Bump with `powershell -File scripts/bump-version-code.ps1 -VersionName 1.3.0`, rewrite the generated entry as `feat`, mirror to assets, update `docs/play-store/release-notes-pt-BR.txt`.
- [x] `.\gradlew.bat test connectedDebugAndroidTest`, `scripts/release-check.ps1`, `python scripts/check_release_history_encoding.py`.
- [x] `.\gradlew.bat installDebug`, open the app, screenshot the stats card on Pixel6.
- [x] Commit, push, check Android CI. Ask before publishing to Play.
