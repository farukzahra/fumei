package fumei.faruk.dev.br.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fumei.faruk.dev.br.MainActivity
import fumei.faruk.dev.br.data.AppDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionPersistenceE2ETest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppDatabase.resetInstanceForTests()
        context.deleteDatabase("fumei.db")
    }

    @Test
    fun entriesPersistAfterActivityRecreate() {
        addTwoEntries()

        composeTestRule.activityRule.scenario.recreate()

        waitForEntryCount(2)
        composeTestRule.onAllNodesWithTag("timeline_entry").assertCountEquals(2)
        composeTestRule.onNodeWithTag("entries_timeline").assertIsDisplayed()
    }

    private fun addTwoEntries() {
        composeTestRule.onNodeWithTag("fumei_button").performClick()
        composeTestRule.onNodeWithTag("fumei_button").performClick()
        waitForEntryCount(2)
    }

    private fun waitForEntryCount(count: Int) {
        val tag = if (count == 0) "empty_entries_message" else "entries_timeline"
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
