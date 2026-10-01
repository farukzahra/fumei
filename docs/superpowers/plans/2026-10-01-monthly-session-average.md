# Monthly Session Average Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show the average elapsed time between consecutive sessions in the selected month.

**Architecture:** Add a pure statistics helper that filters sessions to the selected local month, sorts timestamps, and returns the arithmetic mean interval. `StatsViewModel` formats that duration into `StatsUiState`; `StatsScreen` displays the optional monthly line and the insufficient-data message.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit, AndroidJUnit4 Compose E2E, Gradle.

## Global Constraints

- Consider only sessions whose timestamp, in the device timezone, belongs to the selected month.
- Include consecutive intervals across midnight, but never across a month boundary.
- Discard seconds and fractional minutes with the existing interval formatter.
- Show “Registre mais sessões para calcular a média” with fewer than two sessions.
- Do not show the average in yearly or all-years scopes.
- Do not alter the database schema.
- UI changes require a Compose E2E written before production UI changes.

---

## File map

- Modify `app/src/main/java/fumei/faruk/dev/br/stats/StatsAggregator.kt`: add pure monthly average duration calculation.
- Modify `app/src/test/java/fumei/faruk/dev/br/stats/StatsAggregatorTest.kt`: cover filtering, ordering, cross-day intervals, and insufficient data.
- Modify `app/src/main/java/fumei/faruk/dev/br/ui/StatsUiState.kt`: expose an optional monthly average label.
- Modify `app/src/main/java/fumei/faruk/dev/br/ui/StatsViewModel.kt`: produce the monthly label using the helper and existing `SessionIntervalFormat`.
- Modify `app/src/main/java/fumei/faruk/dev/br/ui/StatsScreen.kt`: render the average inside the monthly period summary only.
- Modify `app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt`: verify a computed monthly average and insufficient-data text.
- Modify `docs/release-history.json` and `app/src/main/assets/release-history.json`: include the monthly average in the existing pending 1.1.0 entry.

## Task 1: Calculate monthly average intervals

**Interfaces:**
- Consumes `List<PuffEntity>`, `YearMonth`, and device `ZoneId`.
- Produces `averageSessionIntervalMillis(puffs: List<PuffEntity>, month: YearMonth, zone: ZoneId): Long?`; returns null with fewer than two sessions.

- [x] Add tests to `StatsAggregatorTest` for shuffled sessions at 10:00 on Aug 1, 13:00 on Aug 2, and 15:00 on Aug 2. Assert the average is 14h30m (mean of 27h and 2h), independent of input order.
- [x] Add sessions on Jul 31 and Sep 1 and verify they do not contribute to August's average.
- [x] Verify zero and one August session return null.
- [x] Run `.\gradlew.bat testDebugUnitTest --tests fumei.faruk.dev.br.stats.StatsAggregatorTest`; confirm the new helper is missing.
- [x] Implement the helper by filtering timestamps in the requested zone, sorting ascending, taking differences of adjacent timestamps, and returning the arithmetic mean as whole milliseconds.
- [x] Rerun the focused unit test class and confirm all tests pass.

## Task 2: Show the average in monthly statistics

**Interfaces:**
- `StatsUiState.monthlyAverageIntervalLabel: String?` is populated only for `StatsScope.MONTH`.
- The label is either `Você levou em média <duration> entre as sessões neste mês.` or `Registre mais sessões para calcular a média`.

- [x] Add a Compose E2E that inserts sessions at Aug 1 10:00 and Aug 2 13:00 in the device zone, selects August 2026 if needed, and asserts `Você levou em média 27h entre as sessões neste mês.` in monthly scope.
- [x] Add an E2E assertion that a month with fewer than two sessions displays `Registre mais sessões para calcular a média`.
- [x] Run the focused E2E and confirm it fails because monthly average is not displayed yet.
- [x] Add `monthlyAverageIntervalLabel` to `StatsUiState`, defaulting to null.
- [x] In `StatsViewModel`, use `averageSessionIntervalMillis` and `SessionIntervalFormat.formatElapsedMillis`; use the insufficient-data string when the helper returns null. Leave the property null for YEAR and YEARS.
- [x] Render the non-null property beneath the existing total and grams in the stats period summary, keeping existing summary text and annual views unchanged.
- [x] Rerun the focused E2E and confirm the computed and insufficient-data cases pass.

## Task 3: Align release history and verify

- [x] Update the pending 1.1.0 title/summary in `docs/release-history.json` to mention the monthly average, preserving its versionCode 17 and uncommitted commit field.
- [x] Run `.\gradlew.bat test connectedDebugAndroidTest` and confirm unit and all Compose E2E tests pass.
- [x] Run `python scripts/check_release_history_encoding.py` and confirm the JSON and packaged asset are valid UTF-8 and synchronized.
- [x] Install and open with `.\gradlew.bat installDebug`, then `adb shell am start -n fumei.faruk.dev.br/.MainActivity`; verify MainActivity is resumed on Pixel6.
- [x] Rebuild the not-yet-uploaded version 1.1.0 AAB using `.\gradlew.bat test bundleRelease` without incrementing the versionCode again.
