package fumei.faruk.dev.br.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentDayFlowTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun currentDayFlow_emitsTodayImmediately() = runTest {
        val day = LocalDate.of(2026, 8, 29)
        val emissions = mutableListOf<LocalDate>()
        val job = launch {
            currentDayFlow(
                zone = zone,
                nowDate = { day },
                nowZoned = { ZonedDateTime.of(2026, 8, 29, 14, 0, 0, 0, zone) },
            ).take(1).toList(emissions)
        }

        advanceTimeBy(1)
        job.join()

        assertEquals(listOf(day), emissions)
    }

    @Test
    fun currentDayFlow_emitsAgainAfterMidnight() = runTest {
        var day = LocalDate.of(2026, 8, 29)
        val emissions = mutableListOf<LocalDate>()

        val job = launch {
            currentDayFlow(
                zone = zone,
                nowDate = { day },
                nowZoned = { day.atTime(23, 59, 50).atZone(zone) },
            ).take(2).toList(emissions)
        }

        advanceTimeBy(1)
        assertEquals(LocalDate.of(2026, 8, 29), emissions.first())

        day = LocalDate.of(2026, 8, 30)
        advanceTimeBy(12_000)
        job.join()

        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 29),
                LocalDate.of(2026, 8, 30),
            ),
            emissions,
        )
    }
}
