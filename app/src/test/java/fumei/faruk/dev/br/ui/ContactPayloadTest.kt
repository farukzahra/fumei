package fumei.faruk.dev.br.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactPayloadTest {
    private fun payload(
        message: String = "oi",
        subject: String = "Fumei 1.4.0 - sugestao",
    ) = ContactPayload.json(
        accessKey = "chave-teste",
        subject = subject,
        message = message,
        fields = mapOf(
            "app" to "Fumei",
            "version" to "1.4.0 (24)",
            "android" to "14",
            "device" to "Pixel 6",
            "language" to "pt-BR",
        ),
    )

    @Test
    fun includesAccessKeySubjectAndMessage() {
        val json = payload()

        assertTrue(json.contains("\"access_key\":\"chave-teste\""))
        assertTrue(json.contains("\"subject\":\"Fumei 1.4.0 - sugestao\""))
        assertTrue(json.contains("\"message\":\"oi\""))
        assertTrue(json.contains("\"from_name\":\"Fumei\""))
        assertTrue(json.contains("\"botcheck\":false"))
    }

    @Test
    fun escapesQuotesAndBackslashes() {
        val json = payload(message = "ele disse \"oi\" e \\ fim")

        assertTrue(json.contains("ele disse \\\"oi\\\" e \\\\ fim"))
        assertFalse(json.contains("\"message\":\"ele disse \"oi\""))
    }

    @Test
    fun turnsLineBreaksAndTabsIntoEscapes() {
        val json = payload(message = "linha 1\nlinha 2\tfim")

        assertTrue(json.contains("linha 1\\nlinha 2\\tfim"))
        assertFalse(json.contains("linha 1\nlinha 2"))
    }

    @Test
    fun keepsPortugueseAccents() {
        val json = payload(message = "sugestão, reclamação e ideia")

        assertTrue(json.contains("sugestão, reclamação e ideia"))
    }

    @Test
    fun includesEveryDetailField() {
        val json = payload()

        assertTrue(json.contains("\"version\":\"1.4.0 (24)\""))
        assertTrue(json.contains("\"device\":\"Pixel 6\""))
        assertTrue(json.contains("\"language\":\"pt-BR\""))
    }
}
