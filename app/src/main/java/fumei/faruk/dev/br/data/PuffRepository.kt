package fumei.faruk.dev.br.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DayBounds(
    val startMillis: Long,
    val endMillis: Long,
)

fun dayBounds(date: LocalDate, zone: ZoneId): DayBounds {
    val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return DayBounds(startMillis = start, endMillis = end)
}

class PuffRepository(private val dao: PuffDao) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayPuffs(zone: ZoneId = ZoneId.systemDefault()): Flow<List<PuffEntity>> {
        return currentDayFlow(zone).flatMapLatest { date ->
            val bounds = dayBounds(date, zone)
            dao.observePuffsBetween(bounds.startMillis, bounds.endMillis)
        }
    }

    suspend fun addPuff(at: Instant = Instant.now()): Long {
        return dao.insert(PuffEntity(timestamp = at.toEpochMilli()))
    }

    suspend fun deletePuff(id: Long) {
        dao.deleteById(id)
    }

    suspend fun updatePuffTimestamp(id: Long, at: Instant) {
        dao.updateTimestamp(id, at.toEpochMilli())
    }
}
