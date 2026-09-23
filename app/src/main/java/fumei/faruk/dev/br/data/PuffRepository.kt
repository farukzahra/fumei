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

data class TodayPuffData(
    val puffs: List<PuffEntity>,
    val yesterdayCount: Int,
)

class PuffRepository(private val dao: PuffDao) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayWithYesterdayCount(zone: ZoneId = ZoneId.systemDefault()): Flow<TodayPuffData> {
        return currentDayFlow(zone).flatMapLatest { date ->
            val todayBounds = dayBounds(date, zone)
            val yesterdayBounds = dayBounds(date.minusDays(1), zone)
            dao.observePuffsBetween(todayBounds.startMillis, todayBounds.endMillis).flatMapLatest { puffs ->
                kotlinx.coroutines.flow.flow {
                    val yesterdayCount = dao.countBetween(
                        yesterdayBounds.startMillis,
                        yesterdayBounds.endMillis,
                    )
                    emit(TodayPuffData(puffs = puffs, yesterdayCount = yesterdayCount))
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayPuffs(zone: ZoneId = ZoneId.systemDefault()): Flow<List<PuffEntity>> {
        return currentDayFlow(zone).flatMapLatest { date ->
            val bounds = dayBounds(date, zone)
            dao.observePuffsBetween(bounds.startMillis, bounds.endMillis)
        }
    }

    suspend fun addPuff(at: Instant = Instant.now(), grams: Double = ConsumptionSettings.DEFAULT_GRAMS_PER_SESSION): Long {
        val normalizedGrams = ConsumptionSettings.normalizedGrams(grams)
        return dao.insert(PuffEntity(timestamp = at.toEpochMilli(), grams = normalizedGrams))
    }

    suspend fun insertAll(puffs: List<PuffEntity>) {
        if (puffs.isEmpty()) return
        dao.insertAll(puffs)
    }

    suspend fun deletePuff(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAllPuffs() {
        dao.deleteAll()
    }

    suspend fun updatePuff(id: Long, at: Instant, grams: Double) {
        dao.updatePuff(id, at.toEpochMilli(), ConsumptionSettings.normalizedGrams(grams))
    }

    suspend fun countAllPuffs(): Int = dao.countAll()

    fun observeAllPuffs(): Flow<List<PuffEntity>> = dao.observeAllPuffs()
}
