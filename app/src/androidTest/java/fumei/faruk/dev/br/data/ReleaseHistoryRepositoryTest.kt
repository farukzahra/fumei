package fumei.faruk.dev.br.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseHistoryRepositoryTest {
    @Test
    fun load_preservesPortugueseAccentsFromBundledAsset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val history = ReleaseHistoryRepository(context).load()

        assertEquals("Veja os intervalos e a média mensal", history.entries.first().title)
        assertEquals(
            "A timeline mostra o tempo entre sessões centralizado nos horários. As estatísticas também calculam a média mensal entre sessões.",
            history.entries.first().summary,
        )
        assertEquals("Sessões em gramas", history.entries[2].title)
        assertEquals("Lançamento na Play Store", history.entries.last().title)
    }
}
