package fumei.faruk.dev.br.export

import fumei.faruk.dev.br.data.PuffEntity
import fumei.faruk.dev.br.stats.StatsScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

class PuffCsvTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun build_startsWithTheHeader() {
        val csv = PuffCsv.build(emptyList(), zone)

        assertEquals("data;hora;gramas\r\n", csv)
    }

    @Test
    fun build_writesOneLinePerSessionOldestFirst() {
        val puffs = listOf(
            puffAt(1, LocalDate.of(2026, 8, 2), LocalTime.of(13, 0), 0.5),
            puffAt(2, LocalDate.of(2026, 8, 1), LocalTime.of(10, 0), 0.3),
        )

        val csv = PuffCsv.build(puffs, zone)

        assertEquals(
            "data;hora;gramas\r\n" +
                "01/08/2026;10:00;0,3\r\n" +
                "02/08/2026;13:00;0,5\r\n",
            csv,
        )
    }

    @Test
    fun build_writesWholeGramsWithoutDecimals() {
        val puffs = listOf(puffAt(1, LocalDate.of(2026, 8, 1), LocalTime.of(9, 30), 1.0))

        val csv = PuffCsv.build(puffs, zone)

        assertEquals("data;hora;gramas\r\n01/08/2026;09:30;1\r\n", csv)
    }

    @Test
    fun build_usesTheGivenZoneForDateAndTime() {
        val puffs = listOf(
            PuffEntity(
                id = 1,
                timestamp = LocalDate.of(2026, 8, 1).atTime(23, 30)
                    .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli(),
                grams = 0.4,
            ),
        )

        val csv = PuffCsv.build(puffs, zone)

        assertEquals("data;hora;gramas\r\n01/08/2026;20:30;0,4\r\n", csv)
    }

    @Test
    fun exportRangeFor_monthCoversTheWholeMonth() {
        val range = exportRangeFor(StatsScope.MONTH, YearMonth.of(2026, 8), 2026, zone)!!

        assertEquals(
            LocalDate.of(2026, 8, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            range.startMillis,
        )
        assertEquals(
            LocalDate.of(2026, 9, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            range.endMillis,
        )
    }

    @Test
    fun exportRangeFor_yearCoversTwelveMonths() {
        val range = exportRangeFor(StatsScope.YEAR, YearMonth.of(2026, 8), 2025, zone)!!

        assertEquals(
            LocalDate.of(2025, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            range.startMillis,
        )
        assertEquals(
            LocalDate.of(2026, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            range.endMillis,
        )
    }

    @Test
    fun exportRangeFor_everyYearHasNoBounds() {
        assertNull(exportRangeFor(StatsScope.YEARS, YearMonth.of(2026, 8), 2026, zone))
    }

    @Test
    fun fileNameFor_describesTheScope() {
        assertEquals("fumei-2026-08.csv", fileNameFor(StatsScope.MONTH, YearMonth.of(2026, 8), 2026))
        assertEquals("fumei-2026.csv", fileNameFor(StatsScope.YEAR, YearMonth.of(2026, 8), 2026))
        assertEquals("fumei-completo.csv", fileNameFor(StatsScope.YEARS, YearMonth.of(2026, 8), 2026))
    }

    @Test
    fun buttonLabelFor_namesTheScope() {
        assertEquals("Exportar CSV do mês", exportLabelFor(StatsScope.MONTH))
        assertEquals("Exportar CSV do ano", exportLabelFor(StatsScope.YEAR))
        assertEquals("Exportar CSV de tudo", exportLabelFor(StatsScope.YEARS))
    }

    private fun puffAt(id: Long, date: LocalDate, time: LocalTime, grams: Double): PuffEntity =
        PuffEntity(
            id = id,
            timestamp = date.atTime(time).atZone(zone).toInstant().toEpochMilli(),
            grams = grams,
        )
}
