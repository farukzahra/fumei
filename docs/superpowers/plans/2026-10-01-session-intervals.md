# Session Intervals Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show each same-day gap between consecutive sessions as a readable horizontal label beside the continuous timeline line.

**Architecture:** Add a pure duration formatter and derive an optional interval label for each descending `PuffListItem` from the immediately older same-day item. Render the label beside the timeline rail, vertically centered in the connector segment, without changing persisted data or session ordering.

**Tech Stack:** Kotlin, Jetpack Compose, AndroidJUnit4 Compose E2E, JUnit, Gradle.

## Global Constraints

- Keep the timeline sorted newest first; a label describes the elapsed time to the next older session.
- Format whole elapsed minutes as `59m` below one hour, `1h` for an exact hour, and `1h42m` when hours and remaining minutes exist.
- Discard incomplete seconds; do not show an interval before the oldest session of the day.
- Keep the line continuous and place the horizontal label to its right, centered vertically between adjacent session time labels.
- Do not change database schema or include sessions from previous days.
- Validate by installing and opening on Pixel6 Android 14; do not push or publish in this task.

---

## File Map

- `app/src/main/java/fumei/faruk/dev/br/ui/SessionIntervalFormat.kt` — formats nonnegative elapsed milliseconds for the timeline.
- `app/src/test/java/fumei/faruk/dev/br/ui/SessionIntervalFormatTest.kt` — covers hour/minute boundaries and truncated seconds.
- `app/src/main/java/fumei/faruk/dev/br/ui/TodayUiState.kt` — carries an optional older-session interval label per row.
- `app/src/main/java/fumei/faruk/dev/br/ui/MainViewModel.kt` — computes each label from adjacent same-day records.
- `app/src/main/java/fumei/faruk/dev/br/ui/HomeScreen.kt` — draws the continuous rail and interval labels.
- `app/src/main/java/fumei/faruk/dev/br/ui/AboutScreen.kt` — describes the interval feature in the About screen.
- `docs/release-history.json` and `app/src/main/assets/release-history.json` — keep the in-development history synchronized.
- `app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt` — verifies labels, their width, and About copy.
- `app/src/androidTest/java/fumei/faruk/dev/br/data/ReleaseHistoryRepositoryTest.kt` — verifies the updated Portuguese history text.

## Task 1: Format session intervals

**Files:**
- Create: `app/src/test/java/fumei/faruk/dev/br/ui/SessionIntervalFormatTest.kt`
- Create: `app/src/main/java/fumei/faruk/dev/br/ui/SessionIntervalFormat.kt`

**Interfaces:**
- Consumes: nonnegative elapsed duration in milliseconds.
- Produces: `SessionIntervalFormat.formatElapsedMillis(elapsedMillis: Long): String`.

- [x] **Step 1: Write unit tests first**

Create:

```kotlin
package fumei.faruk.dev.br.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionIntervalFormatTest {
    @Test
    fun formatElapsedMillis_showsHoursAndRemainingMinutes() {
        assertEquals("1h42m", SessionIntervalFormat.formatElapsedMillis(102 * 60_000L))
        assertEquals("2h5m", SessionIntervalFormat.formatElapsedMillis(125 * 60_000L))
    }

    @Test
    fun formatElapsedMillis_showsMinutesBelowOneHour() {
        assertEquals("59m", SessionIntervalFormat.formatElapsedMillis(59 * 60_000L))
    }

    @Test
    fun formatElapsedMillis_omitsZeroRemainingMinutes() {
        assertEquals("1h", SessionIntervalFormat.formatElapsedMillis(60 * 60_000L))
    }

    @Test
    fun formatElapsedMillis_discardsIncompleteMinutes() {
        assertEquals("59m", SessionIntervalFormat.formatElapsedMillis(59 * 60_000L + 59_999L))
    }
}
```

- [x] **Step 2: Run the unit test and confirm it fails because the formatter is missing**

Run: `.\gradlew.bat testDebugUnitTest --tests fumei.faruk.dev.br.ui.SessionIntervalFormatTest`

Expected: compilation fails with unresolved reference `SessionIntervalFormat`.

- [x] **Step 3: Implement the formatter**

Create:

```kotlin
package fumei.faruk.dev.br.ui

object SessionIntervalFormat {
    fun formatElapsedMillis(elapsedMillis: Long): String {
        val totalMinutes = elapsedMillis.coerceAtLeast(0L) / 60_000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours == 0L -> "${minutes}m"
            minutes == 0L -> "${hours}h"
            else -> "${hours}h${minutes}m"
        }
    }
}
```

- [x] **Step 4: Run the unit test again**

Run: `.\gradlew.bat testDebugUnitTest --tests fumei.faruk.dev.br.ui.SessionIntervalFormatTest`

Expected: all four formatter tests pass.

## Task 2: Derive and display intervals between timeline rows

**Files:**
- Modify: `app/src/main/java/fumei/faruk/dev/br/ui/TodayUiState.kt`
- Modify: `app/src/main/java/fumei/faruk/dev/br/ui/MainViewModel.kt`
- Modify: `app/src/main/java/fumei/faruk/dev/br/ui/HomeScreen.kt`
- Modify: `app/src/main/java/fumei/faruk/dev/br/ui/AboutScreen.kt`
- Modify: `docs/release-history.json` and `app/src/main/assets/release-history.json`
- Modify: `app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt`
- Modify: `app/src/androidTest/java/fumei/faruk/dev/br/data/ReleaseHistoryRepositoryTest.kt`

**Interfaces:**
- Consumes: `SessionIntervalFormat.formatElapsedMillis(Long): String` and `TodayUiState.entries` in descending timestamp order.
- Produces: `PuffListItem.intervalSincePreviousLabel: String?`, null only when no older same-day entry exists.

- [x] **Step 1: Write the Compose E2E case before changing production UI**

Add the following test to `FumeiAppE2ETest`:

```kotlin
@Test
fun timeline_displaysElapsedTimeBetweenAdjacentSessions() {
    val zone = ZoneId.systemDefault()
    val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant()
    runBlocking {
        repository.addPuff(startOfToday.plusSeconds(13 * 3_600L + 27 * 60L))
        repository.addPuff(startOfToday.plusSeconds(14 * 3_600L + 26 * 60L))
        repository.addPuff(startOfToday.plusSeconds(16 * 3_600L + 8 * 60L))
    }
    setFumeiApp()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule.onAllNodesWithTag("entries_timeline").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onAllNodesWithTag("timeline_interval_label").assertCountEquals(2)
    composeTestRule.onAllNodesWithTag("timeline_interval_label")
        .onFirst()
        .assertWidthIsAtLeast(30.dp)
    composeTestRule.onNodeWithText("1h42m").assertIsDisplayed()
    composeTestRule.onNodeWithText("59m").assertIsDisplayed()
}
```

- [x] **Step 2: Run the E2E and confirm it fails because interval labels are not rendered**

Run: `.\gradlew.bat connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=fumei.faruk.dev.br.ui.FumeiAppE2ETest#timeline_displaysElapsedTimeBetweenAdjacentSessions'`

Expected: the test fails because the timeline has no `timeline_interval_label` nodes.

- [x] **Step 3: Add the optional interval field and derive it from the next older entry**

Add this property at the end of `PuffListItem` in `TodayUiState.kt`:

```kotlin
val intervalSincePreviousLabel: String? = null,
```

In `MainViewModel.mapToUiState`, replace `puffs.map { puff ->` with `puffs.mapIndexed { index, puff ->`. For each item, compute:

```kotlin
val intervalSincePreviousLabel = puffs.getOrNull(index + 1)?.let { olderPuff ->
    SessionIntervalFormat.formatElapsedMillis(puff.timestamp - olderPuff.timestamp)
}
```

Pass `intervalSincePreviousLabel` to `PuffListItem`. The DAO already returns the day's entries newest first, so the last item naturally has no older timestamp.

- [x] **Step 4: Render the continuous rail and optional horizontal label**

In `RegistroRow`, reserve a 50 dp rail-and-label gutter on every row. Place the 10 dp dot and 2 dp connector at the gutter's start; on non-last rows, the connector begins below the dot and continues to the next row. Put the optional 12 sp label 14 dp from the gutter start and offset it down 15 dp from the midpoint between dot centers. This aligns it with the midpoint between adjacent timestamp text centers. Keep the 4 dp spacer after the gutter. The gutter gives the label enough width without covering the clickable time/grams row or the edit/delete actions.

Use `AppColors.Smoke400`, `FumeiType.body.copy(fontSize = 12.sp)`, and `.testTag("timeline_interval_label")`. Render no label when `intervalSincePreviousLabel == null`. Update the About hero and current working history summary without changing the app version or publishing.

- [x] **Step 5: Run the targeted E2E and formatter tests**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests fumei.faruk.dev.br.ui.SessionIntervalFormatTest
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=fumei.faruk.dev.br.ui.FumeiAppE2ETest#timeline_displaysElapsedTimeBetweenAdjacentSessions'
```

Expected: the four unit tests and the timeline E2E pass; exactly two interval labels appear for three entries.

## Task 3: Verify the app locally without publishing

**Files:**
- Verify: `app/src/main/java/fumei/faruk/dev/br/ui/HomeScreen.kt`
- Verify: `app/src/main/java/fumei/faruk/dev/br/ui/MainViewModel.kt`
- Verify: `app/src/androidTest/java/fumei/faruk/dev/br/ui/FumeiAppE2ETest.kt`

**Interfaces:**
- Consumes: completed formatter, view-model mapping, and Compose layout.
- Produces: verified local app with continuous timeline line, centered interval labels, and no external release.

- [x] **Step 1: Run all unit and instrumented tests**

Run: `.\gradlew.bat test connectedDebugAndroidTest`

Expected: `BUILD SUCCESSFUL`, with zero failed unit or instrumented tests.

- [x] **Step 2: Install and launch the app on Pixel6**

Run:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
.\gradlew.bat installDebug
& $adb shell am start -n fumei.faruk.dev.br/fumei.faruk.dev.br.MainActivity
& $adb shell dumpsys activity activities | Select-String "ResumedActivity"
```

Expected: `fumei.faruk.dev.br.MainActivity` is resumed on Pixel6. Open Hoje and visually confirm labels sit beside the continuous line midway between session dots.

- [x] **Step 3: Confirm no publication occurred**

Run: `git status --short --branch`

Expected: implementation and test changes remain local; no push, AAB release generation, or Play Console upload was performed.
