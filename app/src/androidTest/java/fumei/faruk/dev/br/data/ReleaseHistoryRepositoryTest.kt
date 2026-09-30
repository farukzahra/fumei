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

        assertEquals("Acentos no histórico de versões", history.entries.first().title)
        assertEquals(
            "O histórico de novidades é lido em UTF-8 para preservar os acentos em português.",
            history.entries.first().summary,
        )
        assertEquals("Sessões em gramas", history.entries[1].title)
        assertEquals("Lançamento na Play Store", history.entries.last().title)
    }
}
