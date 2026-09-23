package fumei.faruk.dev.br.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class StatsGramsAggregatorTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun aggregateGramsByDay_sumsGramsPerDay() {
        val day = LocalDate.of(2026, 9, 23)
        val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val puffs = listOf(
            fumei.faruk.dev.br.data.PuffEntity(id = 1, timestamp = start + 1_000, grams = 0.3),
            fumei.faruk.dev.br.data.PuffEntity(id = 2, timestamp = start + 2_000, grams = 0.5),
            fumei.faruk.dev.br.data.PuffEntity(
                id = 3,
                timestamp = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),
                grams = 0.3,
            ),
        )

        val grams = aggregateGramsByDay(puffs, zone)

        assertEquals(0.8, grams[day]!!, 0.001)
        assertEquals(0.3, grams[day.plusDays(1)]!!, 0.001)
    }

    @Test
    fun monthGramsTotal_sumsOnlySelectedMonth() {
        val month = YearMonth.of(2026, 9)
        val gramsByDay = mapOf(
            LocalDate.of(2026, 8, 31) to 1.0,
            LocalDate.of(2026, 9, 1) to 0.6,
            LocalDate.of(2026, 9, 2) to 0.9,
            LocalDate.of(2026, 10, 1) to 2.0,
        )

        assertEquals(1.5, monthGramsTotal(month, gramsByDay), 0.001)
    }

    @Test
    fun periodGramsLabel_formatsSuffixForMonth() {
        assertEquals("13,5 g no mês", periodGramsLabel(13.5, StatsScope.MONTH))
    }
}
