package fumei.faruk.dev.br.debug

import fumei.faruk.dev.br.data.ConsumptionSettings
import fumei.faruk.dev.br.data.PuffEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

object DebugSampleData {
    const val YEARS = 5L
    const val MAX_PER_DAY = 10
    const val DEV_MONTH_DAYS = 30
    const val DEV_MAX_PER_DAY = 8
}

internal fun generateMonthSample(
    zone: ZoneId = ZoneId.systemDefault(),
    random: Random = Random(42),
    end: LocalDate = LocalDate.now(zone),
    days: Int = DebugSampleData.DEV_MONTH_DAYS,
    maxPerDay: Int = DebugSampleData.DEV_MAX_PER_DAY,
    gramsPerSession: Double = ConsumptionSettings.DEFAULT_GRAMS_PER_SESSION,
): List<PuffEntity> {
    require(days >= 1)
    val start = end.minusDays((days - 1).toLong())
    val puffs = mutableListOf<PuffEntity>()
    var day = start

    while (!day.isAfter(end)) {
        val dailyCount = random.nextInt(1, maxPerDay + 1)
        repeat(dailyCount) {
            val minuteOfDay = random.nextInt(6 * 60, 23 * 60)
            val time = LocalTime.ofSecondOfDay(minuteOfDay * 60L)
            val instant = day.atTime(time).atZone(zone).toInstant()
            puffs += PuffEntity(
                timestamp = instant.toEpochMilli(),
                grams = gramsPerSession,
            )
        }
        day = day.plusDays(1)
    }

    return puffs
}

internal fun generateFiveYearSample(
    zone: ZoneId = ZoneId.systemDefault(),
    random: Random = Random(42),
    end: LocalDate = LocalDate.now(zone),
    years: Long = DebugSampleData.YEARS,
    maxPerDay: Int = DebugSampleData.MAX_PER_DAY,
): List<PuffEntity> {
    val start = end.minusYears(years)
    val puffs = mutableListOf<PuffEntity>()
    var day = start

    while (!day.isAfter(end)) {
        val dailyCount = random.nextInt(0, maxPerDay + 1)
        repeat(dailyCount) {
            val minuteOfDay = random.nextInt(6 * 60, 24 * 60)
            val time = LocalTime.ofSecondOfDay(minuteOfDay * 60L)
            val instant = day.atTime(time).atZone(zone).toInstant()
            puffs += PuffEntity(timestamp = instant.toEpochMilli())
        }
        day = day.plusDays(1)
    }

    return puffs
}
