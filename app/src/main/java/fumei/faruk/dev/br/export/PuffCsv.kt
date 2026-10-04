package fumei.faruk.dev.br.export

import fumei.faruk.dev.br.data.PuffEntity
import fumei.faruk.dev.br.stats.StatsScope
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ExportRange(
    val startMillis: Long,
    val endMillis: Long,
)

data class CsvExport(
    val fileName: String,
    val content: String,
)

private val ptBrLocale: Locale = Locale.forLanguageTag("pt-BR")

object PuffCsv {
    private const val HEADER = "data;hora;gramas"
    private const val LINE_SEPARATOR = "\r\n"
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", ptBrLocale)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", ptBrLocale)

    fun build(puffs: List<PuffEntity>, zone: ZoneId): String {
        val builder = StringBuilder(HEADER).append(LINE_SEPARATOR)
        puffs.sortedBy { it.timestamp }.forEach { puff ->
            val zoned = Instant.ofEpochMilli(puff.timestamp).atZone(zone)
            builder
                .append(zoned.format(dateFormatter))
                .append(';')
                .append(zoned.format(timeFormatter))
                .append(';')
                .append(gramsLabel(puff.grams))
                .append(LINE_SEPARATOR)
        }
        return builder.toString()
    }

    private fun gramsLabel(grams: Double): String =
        String.format(ptBrLocale, "%.2f", grams.coerceAtLeast(0.0))
            .trimEnd('0')
            .trimEnd(',')
}

fun exportRangeFor(
    scope: StatsScope,
    month: YearMonth,
    year: Int,
    zone: ZoneId,
): ExportRange? = when (scope) {
    StatsScope.MONTH -> ExportRange(
        startMillis = startOfMonthMillis(month, zone),
        endMillis = startOfMonthMillis(month.plusMonths(1), zone),
    )

    StatsScope.YEAR -> ExportRange(
        startMillis = startOfYearMillis(year, zone),
        endMillis = startOfYearMillis(year + 1, zone),
    )

    StatsScope.YEARS -> null
}

fun fileNameFor(scope: StatsScope, month: YearMonth, year: Int): String = when (scope) {
    StatsScope.MONTH -> String.format(ptBrLocale, "fumei-%04d-%02d.csv", month.year, month.monthValue)
    StatsScope.YEAR -> String.format(ptBrLocale, "fumei-%04d.csv", year)
    StatsScope.YEARS -> "fumei-completo.csv"
}

fun exportLabelFor(scope: StatsScope): String = when (scope) {
    StatsScope.MONTH -> "Exportar CSV do mês"
    StatsScope.YEAR -> "Exportar CSV do ano"
    StatsScope.YEARS -> "Exportar CSV de tudo"
}

private fun startOfMonthMillis(month: YearMonth, zone: ZoneId): Long =
    month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

private fun startOfYearMillis(year: Int, zone: ZoneId): Long =
    LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
