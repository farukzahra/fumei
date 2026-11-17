package fumei.faruk.dev.br.ui

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import fumei.faruk.dev.br.ui.theme.FumeiTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactDialogE2ETest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private class FixedSender(private val result: Boolean) : ContactSender {
        var received: String? = null
        override suspend fun send(message: String): Boolean {
            received = message
            return result
        }
    }

    private class GatedSender : ContactSender {
        val gate = CompletableDeferred<Boolean>()
        override suspend fun send(message: String): Boolean = gate.await()
    }

    @Test
    fun cardOpensDialogWithSendDisabled() {
        setAboutScreen()

        composeTestRule.onNodeWithTag("about_screen").performScrollToNode(hasTestTag("about_contact_card"))
        composeTestRule.onNodeWithTag("about_contact_card").performClick()

        composeTestRule.onNodeWithTag("contact_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contact_counter", useUnmergedTree = true)
            .assertTextEquals("0/2000")
        composeTestRule.onNodeWithTag("contact_send").assertIsNotEnabled()
    }

    @Test
    fun typingEnablesSendAndCancelCloses() {
        setAboutScreen()

        composeTestRule.onNodeWithTag("about_screen").performScrollToNode(hasTestTag("about_contact_card"))
        composeTestRule.onNodeWithTag("about_contact_card").performClick()

        val message = "quero um gráfico semanal"
        composeTestRule.onNodeWithTag("contact_field").performTextInput(message)
        composeTestRule.onNodeWithTag("contact_counter", useUnmergedTree = true)
            .assertTextEquals("${message.length}/2000")
        composeTestRule.onNodeWithTag("contact_send").assertIsEnabled()

        composeTestRule.onNodeWithTag("contact_cancel").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("contact_dialog").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun sendFailureKeepsDialogAndShowsEmail() {
        val sender = FixedSender(false)
        composeTestRule.setContent {
            FumeiTheme {
                ContactDialog(onDismiss = {}, onSend = sender)
            }
        }

        composeTestRule.onNodeWithTag("contact_field").performTextInput("faltou uma coisa")
        composeTestRule.onNodeWithTag("contact_send").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("contact_failed").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("contact_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contact_failed").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contact_email").assertTextEquals(CONTACT_EMAIL)
        composeTestRule.onNodeWithTag("contact_retry").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contact_use_email").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contact_copy_email").assertIsDisplayed()
    }

    @Test
    fun sendSuccessClosesDialog() {
        val sender = FixedSender(true)
        var visible by mutableStateOf(true)
        composeTestRule.setContent {
            FumeiTheme {
                if (visible) {
                    ContactDialog(onDismiss = { visible = false }, onSend = sender)
                }
            }
        }

        composeTestRule.onNodeWithTag("contact_field").performTextInput("quero um gráfico semanal")
        composeTestRule.onNodeWithTag("contact_send").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("contact_dialog").fetchSemanticsNodes().isEmpty()
        }
        assertEquals("quero um gráfico semanal", sender.received)
    }

    @Test
    fun sendingShowsProgressAndDisablesSend() {
        val sender = GatedSender()
        composeTestRule.setContent {
            FumeiTheme {
                ContactDialog(onDismiss = {}, onSend = sender)
            }
        }

        composeTestRule.onNodeWithTag("contact_field").performTextInput("quero um gráfico semanal")
        composeTestRule.onNodeWithTag("contact_send").performClick()

        composeTestRule.onNodeWithText("Enviando…").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contact_send").assertIsNotEnabled()

        sender.gate.complete(false)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("contact_failed").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun intentCarriesMailtoWithEncodedBody() {
        val intent = contactEmailIntent("Fumei 1.4.0 - sugestão", "oi\nfim")

        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto", intent.data?.scheme)
        val data = intent.data.toString()
        assertTrue(data.contains("subject="))
        assertTrue(data.contains("body=oi%0Afim"))
    }

    private fun setAboutScreen() {
        composeTestRule.setContent {
            FumeiTheme {
                AboutScreen(
                    uiState = AboutUiState(versionName = "1.4.0", versionCode = 24),
                    onBack = {},
                )
            }
        }
    }
}
