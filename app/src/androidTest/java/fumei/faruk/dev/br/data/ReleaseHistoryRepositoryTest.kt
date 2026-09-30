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

        assertEquals("Sessões em gramas", history.entries.first().title)
        assertEquals(
            "Mais com Configurações e Sobre. Gramas por sessão e total em g na Home e Estatísticas. Botão Fumei agora com padrão 0,3 g.",
            history.entries.first().summary,
        )
        assertEquals("Lançamento na Play Store", history.entries.last().title)
    }
}
