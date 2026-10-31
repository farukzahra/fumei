package fumei.faruk.dev.br.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ElapsedSinceLastPuffTest {
    @Test
    fun format_padsMinutesAndSecondsToTwoDigits() {
        assertEquals("0h00m00s", ElapsedSinceLastPuff.format(0L))
        assertEquals("0h05m00s", ElapsedSinceLastPuff.format(5 * 60_000L))
        assertEquals("1h05m00s", ElapsedSinceLastPuff.format(65 * 60_000L))
        assertEquals("1h05m07s", ElapsedSinceLastPuff.format(65 * 60_000L + 7_000L))
    }

    @Test
    fun format_keepsCountingHoursAcrossDays() {
        assertEquals(
            "26h03m07s",
            ElapsedSinceLastPuff.format(26 * 3_600_000L + 3 * 60_000L + 7_000L),
        )
    }

    @Test
    fun format_discardsIncompleteSeconds() {
        assertEquals("0h00m00s", ElapsedSinceLastPuff.format(999L))
        assertEquals("0h00m59s", ElapsedSinceLastPuff.format(59_999L))
        assertEquals("0h59m59s", ElapsedSinceLastPuff.format(59 * 60_000L + 59_999L))
    }

    @Test
    fun format_clampsFutureTimestampsToZero() {
        assertEquals("0h00m00s", ElapsedSinceLastPuff.format(-90_000L))
    }

    @Test
    fun labels_areEmptyWithoutAnyRecord() = runTest {
        val lastPuff = MutableStateFlow<Long?>(null)
        val emissions = mutableListOf<String?>()
        val job = launch {
            ElapsedSinceLastPuff.labels(
                lastPuffAtMillis = lastPuff,
                nowMillis = { 10_000_000L },
                tickMillis = 1_000L,
            ).take(1).toList(emissions)
        }

        advanceTimeBy(1)
        job.join()

        assertEquals(listOf<String?>(null), emissions)
    }

    @Test
    fun labels_followTheTickingClock() = runTest {
        var now = 10_000_000L
        val lastPuff = MutableStateFlow<Long?>(now - 90 * 60_000L)
        val emissions = mutableListOf<String?>()
        val job = launch {
            ElapsedSinceLastPuff.labels(
                lastPuffAtMillis = lastPuff,
                nowMillis = { now },
                tickMillis = 1_000L,
            ).take(2).toList(emissions)
        }

        advanceTimeBy(1)
        assertEquals(listOf<String?>("1h30m00s"), emissions)

        now += 1_000L
        advanceTimeBy(1_001)
        job.join()

        assertEquals(listOf("1h30m00s", "1h30m01s"), emissions)
    }

    @Test
    fun labels_restartWhenANewRecordArrives() = runTest {
        var now = 10_000_000L
        val lastPuff = MutableStateFlow<Long?>(now - 3_600_000L)
        val emissions = mutableListOf<String?>()
        val job = launch {
            ElapsedSinceLastPuff.labels(
                lastPuffAtMillis = lastPuff,
                nowMillis = { now },
                tickMillis = 1_000L,
            ).take(2).toList(emissions)
        }

        advanceTimeBy(1)
        assertEquals(listOf<String?>("1h00m00s"), emissions)

        lastPuff.value = now
        advanceTimeBy(1_001)
        job.join()

        assertEquals(listOf("1h00m00s", "0h00m00s"), emissions)
    }
}
