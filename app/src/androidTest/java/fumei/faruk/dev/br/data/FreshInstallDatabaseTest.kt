package fumei.faruk.dev.br.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fumei.faruk.dev.br.startup.AppStartup
import fumei.faruk.dev.br.startup.AppStartupHook
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FreshInstallDatabaseTest {
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppDatabase.resetInstanceForTests()
        context.deleteDatabase("fumei.db")
        AppStartup.hook = AppStartupHook { _, _ -> }
    }

    @Test
    fun appStartup_leavesDatabaseEmpty() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = PuffRepository(AppDatabase.getInstance(context).puffDao())

        AppStartup.hook.onAppStart(context, repository)

        assertEquals(0, repository.countAllPuffs())
    }
}
