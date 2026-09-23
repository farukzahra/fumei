package fumei.faruk.dev.br.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuffDao {
    @Insert
    suspend fun insert(puff: PuffEntity): Long

    @Insert
    suspend fun insertAll(puffs: List<PuffEntity>): List<Long>

    @Query("DELETE FROM puffs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM puffs")
    suspend fun deleteAll()

    @Query("UPDATE puffs SET timestamp = :timestamp, grams = :grams WHERE id = :id")
    suspend fun updatePuff(id: Long, timestamp: Long, grams: Double)

    @Query(
        """
        SELECT * FROM puffs
        WHERE timestamp >= :start AND timestamp < :end
        ORDER BY timestamp DESC
        """,
    )
    suspend fun getPuffsBetween(start: Long, end: Long): List<PuffEntity>

    @Query(
        """
        SELECT * FROM puffs
        WHERE timestamp >= :start AND timestamp < :end
        ORDER BY timestamp DESC
        """,
    )
    fun observePuffsBetween(start: Long, end: Long): Flow<List<PuffEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM puffs
        WHERE timestamp >= :start AND timestamp < :end
        """,
    )
    suspend fun countBetween(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM puffs")
    suspend fun countAll(): Int

    @Query("SELECT * FROM puffs ORDER BY timestamp ASC")
    fun observeAllPuffs(): Flow<List<PuffEntity>>
}
