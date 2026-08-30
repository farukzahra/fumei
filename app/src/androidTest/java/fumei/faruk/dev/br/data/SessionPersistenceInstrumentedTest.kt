package fumei.faruk.dev.br.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SessionPersistenceInstrumentedTest {
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppDatabase.resetInstanceForTests()
        context.deleteDatabase("fumei.db")
    }

    @Test
    fun puffsPersistOnDiskAfterProcessRestart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = PuffRepository(AppDatabase.getInstance(context).puffDao())

        repository.addPuff(Instant.now())
        repository.addPuff(Instant.now())

        AppDatabase.resetInstanceForTests()

        val restoredRepository = PuffRepository(AppDatabase.getInstance(context).puffDao())
        val puffs = restoredRepository.observeTodayPuffs().first()

        assertEquals(2, puffs.size)
    }
}
