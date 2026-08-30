package fumei.faruk.dev.br.stats

import fumei.faruk.dev.br.data.PuffEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class StatsAggregatorTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun aggregateCountsByDay_groupsPuffsByLocalDate() {
        val day = LocalDate.of(2026, 8, 30)
        val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val puffs = listOf(
            PuffEntity(id = 1, timestamp = start + 1_000),
            PuffEntity(id = 2, timestamp = start + 2_000),
            PuffEntity(id = 3, timestamp = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()),
        )

        val counts = aggregateCountsByDay(puffs, zone)

        assertEquals(2, counts[day])
        assertEquals(1, counts[day.plusDays(1)])
    }

    @Test
    fun buildMonthCalendar_includesLeadingAndTrailingDays() {
        val month = YearMonth.of(2026, 8)
        val counts = mapOf(
            LocalDate.of(2026, 8, 1) to 2,
            LocalDate.of(2026, 8, 15) to 1,
        )

        val cells = buildMonthCalendar(month, counts, java.util.Locale.forLanguageTag("pt-BR"))

        assertEquals(42, cells.size)
        assertEquals(2, cells.first { it.date == LocalDate.of(2026, 8, 1) }.count)
        assertEquals(false, cells.first().inCurrentMonth)
    }

    @Test
    fun monthTotal_sumsOnlySelectedMonth() {
        val month = YearMonth.of(2026, 8)
        val counts = mapOf(
            LocalDate.of(2026, 7, 31) to 5,
            LocalDate.of(2026, 8, 1) to 2,
            LocalDate.of(2026, 8, 2) to 3,
            LocalDate.of(2026, 9, 1) to 9,
        )

        assertEquals(5, monthTotal(month, counts))
    }
}
