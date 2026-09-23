package fumei.faruk.dev.br.stats

import fumei.faruk.dev.br.data.PuffEntity
import fumei.faruk.dev.br.ui.ConsumptionFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

enum class StatsScope {
    MONTH,
    YEAR,
    YEARS,
}

data class CalendarDayCell(
    val date: LocalDate,
    val count: Int,
    val grams: Double = 0.0,
    val inCurrentMonth: Boolean,
)

data class MonthCell(
    val yearMonth: YearMonth,
    val count: Int,
    val grams: Double = 0.0,
)

data class YearCell(
    val year: Int,
    val count: Int,
    val grams: Double = 0.0,
)

fun aggregateCountsByDay(
    puffs: List<PuffEntity>,
    zone: ZoneId,
): Map<LocalDate, Int> {
    return puffs.groupingBy { puff ->
        Instant.ofEpochMilli(puff.timestamp).atZone(zone).toLocalDate()
    }.eachCount()
}

fun aggregateGramsByDay(
    puffs: List<PuffEntity>,
    zone: ZoneId,
): Map<LocalDate, Double> {
    return puffs.groupBy { puff ->
        Instant.ofEpochMilli(puff.timestamp).atZone(zone).toLocalDate()
    }.mapValues { (_, dayPuffs) -> dayPuffs.sumOf { it.grams } }
}

fun aggregateGramsByMonth(
    puffs: List<PuffEntity>,
    zone: ZoneId,
): Map<YearMonth, Double> {
    return puffs.groupBy { puff ->
        YearMonth.from(Instant.ofEpochMilli(puff.timestamp).atZone(zone).toLocalDate())
    }.mapValues { (_, monthPuffs) -> monthPuffs.sumOf { it.grams } }
}

fun aggregateGramsByYear(
    puffs: List<PuffEntity>,
    zone: ZoneId,
): Map<Int, Double> {
    return puffs.groupBy { puff ->
        Instant.ofEpochMilli(puff.timestamp).atZone(zone).year
    }.mapValues { (_, yearPuffs) -> yearPuffs.sumOf { it.grams } }
}

fun aggregateCountsByMonth(
    puffs: List<PuffEntity>,
    zone: ZoneId,
): Map<YearMonth, Int> {
    return puffs.groupingBy { puff ->
        YearMonth.from(Instant.ofEpochMilli(puff.timestamp).atZone(zone).toLocalDate())
    }.eachCount()
}

fun aggregateCountsByYear(
    puffs: List<PuffEntity>,
    zone: ZoneId,
): Map<Int, Int> {
    return puffs.groupingBy { puff ->
        Instant.ofEpochMilli(puff.timestamp).atZone(zone).year
    }.eachCount()
}

fun buildMonthCalendar(
    yearMonth: YearMonth,
    countsByDay: Map<LocalDate, Int>,
    locale: Locale,
    gramsByDay: Map<LocalDate, Double> = emptyMap(),
): List<CalendarDayCell> {
    val weekFields = WeekFields.of(locale)
    val firstDayOfWeek = weekFields.firstDayOfWeek
    var start = yearMonth.atDay(1)
    while (start.dayOfWeek != firstDayOfWeek) {
        start = start.minusDays(1)
    }

    val lastDayOfWeek = firstDayOfWeek.plus(6)
    var end = yearMonth.atEndOfMonth()
    while (end.dayOfWeek != lastDayOfWeek) {
        end = end.plusDays(1)
    }

    val cells = mutableListOf<CalendarDayCell>()
    var current = start
    while (!current.isAfter(end)) {
        cells += CalendarDayCell(
            date = current,
            count = countsByDay[current] ?: 0,
            grams = gramsByDay[current] ?: 0.0,
            inCurrentMonth = YearMonth.from(current) == yearMonth,
        )
        current = current.plusDays(1)
    }
    return cells
}

fun monthTotal(
    yearMonth: YearMonth,
    countsByDay: Map<LocalDate, Int>,
): Int {
    return countsByDay.entries
        .filter { (date, _) -> YearMonth.from(date) == yearMonth }
        .sumOf { it.value }
}

fun monthGramsTotal(
    yearMonth: YearMonth,
    gramsByDay: Map<LocalDate, Double>,
): Double {
    return gramsByDay.entries
        .filter { (date, _) -> YearMonth.from(date) == yearMonth }
        .sumOf { it.value }
}

fun yearTotal(
    year: Int,
    countsByMonth: Map<YearMonth, Int>,
): Int {
    return countsByMonth.entries
        .filter { (month, _) -> month.year == year }
        .sumOf { it.value }
}

fun yearGramsTotal(
    year: Int,
    gramsByMonth: Map<YearMonth, Double>,
): Double {
    return gramsByMonth.entries
        .filter { (month, _) -> month.year == year }
        .sumOf { it.value }
}

fun totalGrams(puffs: List<PuffEntity>): Double = puffs.sumOf { it.grams }

fun periodGramsLabel(totalGrams: Double, scope: StatsScope): String {
    val formatted = ConsumptionFormat.formatGramsTotal(totalGrams)
    val suffix = when (scope) {
        StatsScope.MONTH -> "no mês"
        StatsScope.YEAR -> "no ano"
        StatsScope.YEARS -> "no total"
    }
    return "$formatted g $suffix"
}

fun yearsRange(
    puffs: List<PuffEntity>,
    zone: ZoneId,
    currentYear: Int,
): IntRange {
    val firstYear = puffs.minOfOrNull { puff ->
        Instant.ofEpochMilli(puff.timestamp).atZone(zone).year
    } ?: currentYear
    return firstYear..currentYear
}
