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

    @Query("UPDATE puffs SET timestamp = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: Long, timestamp: Long)

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

    @Query("SELECT * FROM puffs ORDER BY timestamp ASC")
    fun observeAllPuffs(): Flow<List<PuffEntity>>
}
