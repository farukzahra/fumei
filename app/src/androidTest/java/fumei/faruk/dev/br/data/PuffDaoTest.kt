package fumei.faruk.dev.br.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PuffDaoTest {
    private lateinit var dao: PuffDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.puffDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertUpdateAndDelete() = runBlocking {
        val now = System.currentTimeMillis()
        val id = dao.insert(PuffEntity(timestamp = now))

        val updated = now - 3_600_000L
        dao.updateTimestamp(id, updated)

        val bounds = dayBounds(java.time.LocalDate.now(), java.time.ZoneId.systemDefault())
        val afterUpdate = dao.getPuffsBetween(bounds.startMillis, bounds.endMillis)
        assertEquals(1, afterUpdate.size)
        assertEquals(updated, afterUpdate.first().timestamp)

        dao.deleteById(id)
        val afterDelete = dao.getPuffsBetween(bounds.startMillis, bounds.endMillis)
        assertEquals(0, afterDelete.size)
    }
}
