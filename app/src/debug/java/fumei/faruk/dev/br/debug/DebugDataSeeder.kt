package fumei.faruk.dev.br.debug

import android.content.Context
import fumei.faruk.dev.br.data.PuffEntity
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.startup.AppStartupHook
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

class DebugAppStartup : AppStartupHook {
    override suspend fun onAppStart(context: Context, repository: PuffRepository) {
        if (isInstrumentedTest()) return
        DebugDataSeeder(context, repository).runIfNeeded()
    }

    private fun isInstrumentedTest(): Boolean {
        return try {
            Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}

internal fun generateFiveYearSample(
    zone: ZoneId = ZoneId.systemDefault(),
    random: Random = Random(42),
    end: LocalDate = LocalDate.now(zone),
    years: Long = DebugDataSeeder.YEARS,
    maxPerDay: Int = DebugDataSeeder.MAX_PER_DAY,
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

class DebugDataSeeder(
    private val context: Context,
    private val repository: PuffRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val random: Random = Random(42),
) {
    suspend fun runIfNeeded() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DONE, false)) return

        val puffs = generateFiveYearSample(zone = zone, random = random)
        puffs.chunked(BATCH_SIZE).forEach { batch ->
            repository.insertAll(batch)
        }

        prefs.edit().putBoolean(KEY_DONE, true).apply()
    }

    companion object {
        const val YEARS = 5L
        const val MAX_PER_DAY = 10
        private const val PREFS_NAME = "fumei_debug_seed"
        private const val KEY_DONE = "five_year_sample_v1"
        private const val BATCH_SIZE = 500
    }
}
