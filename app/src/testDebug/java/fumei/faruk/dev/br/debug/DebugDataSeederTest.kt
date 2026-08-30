package fumei.faruk.dev.br.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

class DebugDataSeederTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun generateFiveYearSample_respectsDailyCapAndRange() {
        val end = LocalDate.of(2026, 8, 30)
        val puffs = generateFiveYearSample(
            zone = zone,
            random = Random(7),
            end = end,
        )

        val start = end.minusYears(DebugDataSeeder.YEARS)
        val countsByDay = puffs.groupingBy { puff ->
            Instant.ofEpochMilli(puff.timestamp).atZone(zone).toLocalDate()
        }.eachCount()

        assertTrue(puffs.isNotEmpty())
        assertTrue(countsByDay.keys.all { !it.isBefore(start) && !it.isAfter(end) })
        assertTrue(countsByDay.values.all { it in 0..DebugDataSeeder.MAX_PER_DAY })
    }

    @Test
    fun generateFiveYearSample_isDeterministicForSeed() {
        val end = LocalDate.of(2026, 8, 30)
        val first = generateFiveYearSample(zone = zone, random = Random(99), end = end)
        val second = generateFiveYearSample(zone = zone, random = Random(99), end = end)
        assertEquals(first.size, second.size)
    }
}
