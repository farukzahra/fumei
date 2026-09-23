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
class DatabaseUpgradePersistenceTest {
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppDatabase.resetInstanceForTests()
        context.deleteDatabase("fumei.db")
    }

    @Test
    fun reopenAfterSimulatedAppUpdate_preservesAllPuffs() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = PuffRepository(AppDatabase.getInstance(context).puffDao())

        repository.addPuff(Instant.parse("2026-08-29T10:00:00Z"))
        repository.addPuff(Instant.parse("2026-08-30T12:00:00Z"))
        repository.addPuff(Instant.parse("2026-08-30T18:00:00Z"))

        AppDatabase.resetInstanceForTests()

        val restoredRepository = PuffRepository(AppDatabase.getInstance(context).puffDao())
        val puffs = restoredRepository.observeAllPuffs().first()

        assertEquals(3, puffs.size)
        assertEquals(ConsumptionSettings.DEFAULT_GRAMS_PER_SESSION, puffs.first().grams, 0.001)
    }
}
