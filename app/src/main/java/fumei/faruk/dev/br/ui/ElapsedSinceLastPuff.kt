package fumei.faruk.dev.br.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * Contador de tempo desde o último registro, no formato "0h00m00s".
 *
 * Continua contando enquanto a tela está aberta, inclusive de um dia para o outro.
 */
object ElapsedSinceLastPuff {
    const val TICK_MILLIS = 1_000L

    fun format(elapsedMillis: Long): String {
        val totalSeconds = elapsedMillis.coerceAtLeast(0L) / 1_000L
        val hours = totalSeconds / 3_600L
        val minutes = (totalSeconds % 3_600L) / 60L
        val seconds = totalSeconds % 60L
        return "${hours}h${minutes.twoDigits()}m${seconds.twoDigits()}s"
    }

    private fun Long.twoDigits(): String = toString().padStart(2, '0')

    fun labels(
        lastPuffAtMillis: Flow<Long?>,
        nowMillis: () -> Long = System::currentTimeMillis,
        tickMillis: Long = TICK_MILLIS,
    ): Flow<String?> = combine(lastPuffAtMillis, ticks(nowMillis, tickMillis)) { lastPuff, now ->
        lastPuff?.let { format(now - it) }
    }

    private fun ticks(
        nowMillis: () -> Long,
        tickMillis: Long,
    ): Flow<Long> = flow {
        while (true) {
            emit(nowMillis())
            delay(tickMillis)
        }
    }
}
