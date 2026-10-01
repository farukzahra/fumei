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

        assertEquals(
            "Correção no histórico de novidades",
            history.entries.first().title,
        )
        assertEquals("Veja os intervalos e a média mensal", history.entries[1].title)
        assertEquals("Acentos no histórico de versões", history.entries[2].title)
        assertEquals(
            "O histórico de novidades é lido em UTF-8 para preservar os acentos em português.",
            history.entries[2].summary,
        )
        assertEquals("Sessões em gramas", history.entries[3].title)
        assertEquals("Lançamento na Play Store", history.entries.last().title)
    }
}
