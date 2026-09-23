package fumei.faruk.dev.br.debug

import android.os.Build
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.startup.AppStartup
import fumei.faruk.dev.br.startup.AppStartupHook
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object DebugDevSeeder {
    private val zone: ZoneId = ZoneId.systemDefault()

    val startupHook: AppStartupHook = AppStartupHook { _, repository ->
        ensureEmulatorMonthSample(repository)
    }

    suspend fun ensureEmulatorMonthSample(repository: PuffRepository) {
        if (!isAndroidEmulator()) return

        val today = LocalDate.now(zone)
        val puffs = repository.observeAllPuffs().first()
        if (puffs.isEmpty()) {
            reseedMonth(repository, today)
            return
        }

        val latestDay = puffs.maxOf { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        val earliestDay = puffs.minOf { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        val windowStart = today.minusDays((DebugSampleData.DEV_MONTH_DAYS - 1).toLong())
        val stale = latestDay.isBefore(today) ||
            earliestDay.isAfter(windowStart) ||
            earliestDay.isBefore(windowStart)
        if (stale) {
            reseedMonth(repository, today)
        }
    }

    suspend fun reseedMonth(repository: PuffRepository, end: LocalDate = LocalDate.now(zone)) {
        repository.clearAllPuffs()
        repository.insertAll(generateMonthSample(zone = zone, end = end))
    }

    internal fun isAndroidEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk_gphone", ignoreCase = true)
    }
}
