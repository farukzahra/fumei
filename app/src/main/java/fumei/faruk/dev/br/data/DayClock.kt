package fumei.faruk.dev.br.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Emits the current local date whenever it changes (including at midnight while the app is open).
 */
internal fun currentDayFlow(
    zone: ZoneId,
    nowDate: () -> LocalDate = { LocalDate.now(zone) },
    nowZoned: () -> ZonedDateTime = { ZonedDateTime.now(zone) },
): Flow<LocalDate> = flow {
    var last: LocalDate? = null
    while (true) {
        val today = nowDate()
        if (today != last) {
            last = today
            emit(today)
        }
        val now = nowZoned()
        val nextMidnight = today.plusDays(1).atStartOfDay(zone)
        val millisUntilMidnight = Duration.between(now, nextMidnight).toMillis()
        val delayMs = when {
            millisUntilMidnight <= 0L -> 1_000L
            millisUntilMidnight < 60_000L -> millisUntilMidnight + 1_000L
            else -> 60_000L
        }
        delay(delayMs)
    }
}
