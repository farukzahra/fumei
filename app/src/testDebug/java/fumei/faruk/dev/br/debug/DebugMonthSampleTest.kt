package fumei.faruk.dev.br.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

class DebugMonthSampleTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun generateMonthSample_coversThirtyDaysEndingToday() {
        val end = LocalDate.of(2026, 9, 23)
        val puffs = generateMonthSample(zone = zone, random = Random(7), end = end)

        val byDay = puffs.groupingBy { puff ->
            Instant.ofEpochMilli(puff.timestamp).atZone(zone).toLocalDate()
        }.eachCount()

        val start = end.minusDays(29)
        assertTrue(byDay.keys.all { !it.isBefore(start) && !it.isAfter(end) })
        assertEquals(30, byDay.size)
        assertTrue(byDay[end]!! >= 1)
    }
}
