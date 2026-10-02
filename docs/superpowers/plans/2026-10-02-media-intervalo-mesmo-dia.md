# Same-Day Session Interval Average Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Stop counting the overnight gap between the last session of one day and the first session of the next day in the monthly average, and say so in the stats card.

**Architecture:** `averageSessionIntervalMillis` keeps its signature and month filtering, but drops every consecutive pair whose two timestamps fall on different local dates. `StatsViewModel` only changes the display string. `MainViewModel` and the timeline stay untouched because `PuffRepository.observeTodayWithYesterdayCount` only delivers the current day, so no interval there can cross midnight; a new E2E locks that in.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, JUnit 4, AndroidJUnit4 Compose E2E, Gradle, Room in-memory for tests.

**Spec:** `docs/superpowers/specs/2026-10-02-media-intervalo-mesmo-dia-design.md`

## Global Constraints

- An interval only counts when both timestamps share the same local date in the device timezone.
- Sessions outside the selected month stay ignored.
- Return `null` when the month has no valid pair, including months with many sessions spread one per day.
- Keep `Registre mais sessões para calcular a média` as the fallback text.
- Card text: `Você levou em média <duração> entre as sessões do mesmo dia neste mês.`
- Format durations with `SessionIntervalFormat.formatElapsedMillis` (whole minutes, `3h`, `30m`, `3h20m`).
- Do not alter the database schema or any persisted structure.
- UI-visible change: `docs/release-history.json` and `app/src/main/assets/release-history.json` must be updated before shipping, with the version bump done by the project script.
- UI files require the Compose E2E first (`app/src/androidTest/.../ui/*E2ETest.kt`).
- No em dash in pt-BR copy.
- Gradle needs JDK 17: set `$env:JAVA_HOME='C:\Users\T-GAMER\.jdks\temurin-17.0.18'` in the shell, because the default JAVA_HOME points to Java 8 and AGP 8.9.1 refuses it.

---

## File map

- Modify `app/src/main/java/fumei/faruk/dev/br/stats/StatsAggregator.kt`: drop cross-day pairs, return null when nothing remains.
- Modify `app/src/test/java/fumei/faruk/dev/br/stats/StatsAggregatorTest.kt`: same-day rule, midnight boundaries, mixed pairs, month filter, null cases.
- Modify `app/src/main/java/fumei/faruk/dev/br/ui/StatsViewModel.kt`: new card sentence.
- Modify `app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt`: 3h average, fallback for a month without a same-day pair, timeline regression with a yesterday session.
- Modify `docs/release-history.json`, `app/src/main/assets/release-history.json`, `app/build.gradle.kts`, `docs/play-store/version-codes.json`: pending 1.1.2 entry, at ship time through `scripts/bump-version-code.ps1`.

---

### Task 1: Count only same-day intervals

**Files:**
- Modify: `app/src/test/java/fumei/faruk/dev/br/stats/StatsAggregatorTest.kt`
- Modify: `app/src/main/java/fumei/faruk/dev/br/stats/StatsAggregator.kt:85-108`

**Interfaces:**
- Consumes: `List<PuffEntity>`, `YearMonth`, `ZoneId`, existing `PuffEntity(id, timestamp, grams)` and the private test helper `puffAt(id, date, time)`.
- Produces: `averageSessionIntervalMillis(puffs: List<PuffEntity>, month: YearMonth, zone: ZoneId): Long?` with unchanged signature.

- [x] **Step 1: Replace the test that locks the old behaviour**

In `StatsAggregatorTest.kt`, delete `averageSessionIntervalMillis_sortsSessionsBeforeAveragingIntervals` and add:

```kotlin
    @Test
    fun averageSessionIntervalMillis_averagesOnlyIntervalsInsideTheSameDay() {
        val month = YearMonth.of(2026, 8)
        val puffs = listOf(
            puffAt(1, LocalDate.of(2026, 8, 2), LocalTime.of(15, 0)),
            puffAt(2, LocalDate.of(2026, 8, 1), LocalTime.of(10, 0)),
            puffAt(3, LocalDate.of(2026, 8, 2), LocalTime.of(13, 0)),
        )

        assertEquals(2 * 60 * 60 * 1_000L, averageSessionIntervalMillis(puffs, month, zone))
    }

    @Test
    fun averageSessionIntervalMillis_ignoresIntervalsCrossingMidnight() {
        val month = YearMonth.of(2026, 8)
        val puffs = listOf(
            puffAt(1, LocalDate.of(2026, 8, 1), LocalTime.of(23, 50)),
            puffAt(2, LocalDate.of(2026, 8, 2), LocalTime.of(0, 10)),
        )

        assertEquals(null, averageSessionIntervalMillis(puffs, month, zone))
    }

    @Test
    fun averageSessionIntervalMillis_countsIntervalEndingBeforeMidnight() {
        val month = YearMonth.of(2026, 8)
        val puffs = listOf(
            puffAt(1, LocalDate.of(2026, 8, 1), LocalTime.of(23, 50)),
            puffAt(2, LocalDate.of(2026, 8, 1), LocalTime.of(23, 59)),
        )

        assertEquals(9 * 60 * 1_000L, averageSessionIntervalMillis(puffs, month, zone))
    }

    @Test
    fun averageSessionIntervalMillis_skipsCrossDayPairsButKeepsSameDayPairs() {
        val month = YearMonth.of(2026, 8)
        val puffs = listOf(
            puffAt(1, LocalDate.of(2026, 8, 3), LocalTime.of(9, 0)),
            puffAt(2, LocalDate.of(2026, 8, 2), LocalTime.of(10, 30)),
            puffAt(3, LocalDate.of(2026, 8, 1), LocalTime.of(20, 0)),
            puffAt(4, LocalDate.of(2026, 8, 2), LocalTime.of(10, 0)),
        )

        assertEquals(30 * 60 * 1_000L, averageSessionIntervalMillis(puffs, month, zone))
    }

    @Test
    fun averageSessionIntervalMillis_returnsNullWithoutASameDayPair() {
        val month = YearMonth.of(2026, 8)
        val puffs = listOf(
            puffAt(1, LocalDate.of(2026, 8, 1), LocalTime.of(10, 0)),
            puffAt(2, LocalDate.of(2026, 8, 2), LocalTime.of(10, 0)),
            puffAt(3, LocalDate.of(2026, 8, 3), LocalTime.of(10, 0)),
        )

        assertEquals(null, averageSessionIntervalMillis(puffs, month, zone))
    }
```

- [x] **Step 2: Update the month-filter test**

Replace the body of `averageSessionIntervalMillis_ignoresSessionsOutsideSelectedMonth` with:

```kotlin
        val month = YearMonth.of(2026, 8)
        val puffs = listOf(
            puffAt(1, LocalDate.of(2026, 9, 1), LocalTime.of(10, 0)),
            puffAt(2, LocalDate.of(2026, 7, 31), LocalTime.of(22, 0)),
            puffAt(3, LocalDate.of(2026, 8, 1), LocalTime.of(10, 0)),
            puffAt(4, LocalDate.of(2026, 8, 1), LocalTime.of(13, 0)),
        )

        assertEquals(3 * 60 * 60 * 1_000L, averageSessionIntervalMillis(puffs, month, zone))
```

- [x] **Step 3: Run the unit tests and confirm RED**

Run: `.\gradlew testDebugUnitTest --tests "fumei.faruk.dev.br.stats.StatsAggregatorTest"`

Expected: FAIL on `averageSessionIntervalMillis_averagesOnlyIntervalsInsideTheSameDay`, `averageSessionIntervalMillis_ignoresIntervalsCrossingMidnight`, `averageSessionIntervalMillis_skipsCrossDayPairsButKeepsSameDayPairs` and `averageSessionIntervalMillis_returnsNullWithoutASameDayPair`, all because the current code includes cross-day pairs.

- [x] **Step 4: Implement the same-day filter**

Replace the body of `averageSessionIntervalMillis` in `StatsAggregator.kt` with:

```kotlin
fun averageSessionIntervalMillis(
    puffs: List<PuffEntity>,
    month: YearMonth,
    zone: ZoneId,
): Long? {
    val timestamps = puffs.asSequence()
        .filter { puff ->
            YearMonth.from(Instant.ofEpochMilli(puff.timestamp).atZone(zone)) == month
        }
        .map { it.timestamp }
        .sorted()
        .toList()

    val intervals = timestamps.zipWithNext { earlier, later ->
        if (localDate(earlier, zone) == localDate(later, zone)) later - earlier else null
    }.filterNotNull()

    if (intervals.isEmpty()) return null

    return intervals.sum() / intervals.size
}

private fun localDate(timestampMillis: Long, zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(timestampMillis).atZone(zone).toLocalDate()
```

- [x] **Step 5: Run the unit tests and confirm GREEN**

Run: `.\gradlew testDebugUnitTest --tests "fumei.faruk.dev.br.stats.StatsAggregatorTest"`

Expected: PASS.

- [x] **Step 6: Commit**

```bash
git add app/src/main/java/fumei/faruk/dev/br/stats/StatsAggregator.kt app/src/test/java/fumei/faruk/dev/br/stats/StatsAggregatorTest.kt
git commit -m "fix: ignore cross-day gaps in session interval average"
```

---

### Task 2: Say "do mesmo dia" in the stats card

**Files:**
- Modify: `app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt:170-200`
- Modify: `app/src/main/java/fumei/faruk/dev/br/ui/StatsViewModel.kt:153-155`

**Interfaces:**
- Consumes: `StatsUiState.monthlyAverageIntervalLabel: String?`, `StatsViewModel.onMonthSelected(YearMonth)`, `averageSessionIntervalMillis` from Task 1.
- Produces: label text `Você levou em média <duração> entre as sessões do mesmo dia neste mês.` or `Registre mais sessões para calcular a média`.

- [x] **Step 1: Update the E2E for the average and add the no-same-day-pair E2E**

Replace `statsTab_displaysAverageSessionIntervalForSelectedMonth` and add a new test after it:

```kotlin
    @Test
    fun statsTab_displaysAverageSessionIntervalForSelectedMonth() {
        val zone = ZoneId.systemDefault()
        runBlocking {
            repository.addPuff(LocalDateTime.of(2026, 8, 2, 10, 0).atZone(zone).toInstant())
            repository.addPuff(LocalDateTime.of(2026, 8, 2, 13, 0).atZone(zone).toInstant())
        }
        statsViewModel.onMonthSelected(YearMonth.of(2026, 8))
        setFumeiApp()

        composeTestRule.onNodeWithTag("nav_stats").performClick()
        composeTestRule.onNodeWithText(
            "Você levou em média 3h entre as sessões do mesmo dia neste mês.",
            substring = true,
        ).assertIsDisplayed()
    }

    @Test
    fun statsTab_displaysAveragePromptWhenMonthHasNoSameDayPair() {
        val zone = ZoneId.systemDefault()
        runBlocking {
            repository.addPuff(LocalDateTime.of(2026, 8, 1, 10, 0).atZone(zone).toInstant())
            repository.addPuff(LocalDateTime.of(2026, 8, 2, 13, 0).atZone(zone).toInstant())
        }
        statsViewModel.onMonthSelected(YearMonth.of(2026, 8))
        setFumeiApp()

        composeTestRule.onNodeWithTag("nav_stats").performClick()
        composeTestRule.onNodeWithText(
            "Registre mais sessões para calcular a média",
            substring = true,
        ).assertIsDisplayed()
    }
```

- [x] **Step 2: Run the E2E and confirm RED**

Start the Pixel6 emulator if `adb devices` lists nothing, then run:

`.\gradlew connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=fumei.faruk.dev.br.ui.FumeiAppE2ETest"`

Expected: FAIL in `statsTab_displaysAverageSessionIntervalForSelectedMonth` (still shows the old sentence) and in `statsTab_displaysAveragePromptWhenMonthHasNoSameDayPair` (no such node), while the timeline tests still pass.

- [x] **Step 3: Change the card sentence**

In `StatsViewModel.kt`:

```kotlin
                    monthlyAverageIntervalLabel = averageInterval?.let {
                        "Você levou em média ${SessionIntervalFormat.formatElapsedMillis(it)} entre as sessões do mesmo dia neste mês."
                    } ?: "Registre mais sessões para calcular a média",
```

- [x] **Step 4: Run the E2E and confirm GREEN**

Run: `.\gradlew connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=fumei.faruk.dev.br.ui.FumeiAppE2ETest"`

Expected: PASS.

- [x] **Step 5: Commit**

```bash
git add app/src/main/java/fumei/faruk/dev/br/ui/StatsViewModel.kt app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt
git commit -m "fix: clarify monthly average counts same-day sessions only"
```

---

### Task 3: Lock the timeline behaviour with an E2E

**Files:**
- Modify: `app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt` (new test near `timeline_displaysElapsedTimeBetweenAdjacentSessions`)

**Interfaces:**
- Consumes: `MainViewModel` through `setFumeiApp()`, tags `timeline_entry` and `timeline_interval_label`, `PuffRepository.addPuff(Instant)`.
- Produces: no production change; a regression test proving the oldest entry of the day carries no interval label.

- [x] **Step 1: Add the import and the test**

Add `import java.time.LocalTime` next to the existing `java.time` imports and add:

```kotlin
    @Test
    fun timeline_showsNoIntervalForTheFirstEntryOfTheDay() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        runBlocking {
            repository.addPuff(
                LocalDateTime.of(today.minusDays(1), LocalTime.of(23, 0)).atZone(zone).toInstant(),
            )
            repository.addPuff(
                LocalDateTime.of(today, LocalTime.of(10, 0)).atZone(zone).toInstant(),
            )
            repository.addPuff(
                LocalDateTime.of(today, LocalTime.of(13, 0)).atZone(zone).toInstant(),
            )
        }
        setFumeiApp()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("timeline_interval_label").fetchSemanticsNodes().size == 1
        }
        assertEquals(
            2,
            composeTestRule.onAllNodesWithTag("timeline_entry").fetchSemanticsNodes().size,
        )
        composeTestRule.onNodeWithText("3h").assertIsDisplayed()
    }
```

- [x] **Step 2: Run the E2E and confirm it passes without production change**

Run: `.\gradlew connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=fumei.faruk.dev.br.ui.FumeiAppE2ETest"`

Expected: PASS. If it fails with two interval labels, the timeline does cross the day boundary and Task 3 needs a `MainViewModel` fix in the same shape as Task 1.

- [x] **Step 3: Commit**

```bash
git add app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt
git commit -m "test: cover timeline interval label on the first entry of the day"
```

---

### Task 4: Ship prep and full verification

**Files:**
- Modify: `docs/release-history.json`, `app/src/main/assets/release-history.json`, `app/build.gradle.kts`, `docs/play-store/version-codes.json`

- [x] **Step 1: Bump through the project script**

Run: `powershell -File scripts/bump-version-code.ps1 -VersionName 1.1.2`

Expected: `Bumping versionCode: 18 -> 19 (versionName 1.1.2)` and a new history entry `Atualização 1.1.2`.

- [x] **Step 2: Rewrite the generated entry**

In `docs/release-history.json`, replace that entry's `title`, `summary` and `type` with the fix wording, keeping `version`, `versionCode` and `date` as generated:

```json
                        "title":  "Média entre sessões corrigida",
                        "summary":  "A média mensal entre sessões não conta mais o intervalo entre o último registro de um dia e o primeiro do dia seguinte.",
                        "type":  "fix"
```

Mirror the same entry into `app/src/main/assets/release-history.json`.

- [x] **Step 3: Check the About copy**

Read `AboutScreen.kt` hero text. It says `Veja o tempo entre sessões, a média mensal`. That sentence stays true, so only touch it if it claims something the fix contradicts.

- [x] **Step 4: Validate versions and encoding**

Run: `powershell -File scripts/release-check.ps1` and `python scripts/check_release_history_encoding.py`

Expected: `release-check OK: 1.1.2 (19)` and an encoding check with no corrupted sequences.

- [x] **Step 5: Full validation**

Run: `.\gradlew test connectedDebugAndroidTest`

Expected: unit and instrumented suites pass.

- [x] **Step 6: Install and open on the Pixel6 emulator**

Run:

```powershell
.\gradlew installDebug
adb shell am start -n fumei.faruk.dev.br/fumei.faruk.dev.br.MainActivity
adb shell dumpsys activity activities | Select-String "mResumedActivity"
```

Expected: `mResumedActivity` lists `fumei.faruk.dev.br.MainActivity`.

- [ ] **Step 7: Commit and ship**

Use `/commit-push`, then confirm the Android CI workflow on GitHub.
