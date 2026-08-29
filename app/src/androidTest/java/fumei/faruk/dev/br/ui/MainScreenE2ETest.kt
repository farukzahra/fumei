package fumei.faruk.dev.br.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fumei.faruk.dev.br.data.AppDatabase
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.ui.theme.FumeiTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenE2ETest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: PuffRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PuffRepository(database.puffDao())
        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fumeiButton_addsEntryAndUpdatesCounter() {
        setMainScreen()

        composeTestRule.onNodeWithTag("fumei_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("1 registro hoje").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("entries_card").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Editar registro").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Excluir registro").assertIsDisplayed()
    }

    @Test
    fun editButton_opensEditDialog() {
        setMainScreen()

        composeTestRule.onNodeWithTag("fumei_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Editar registro").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Editar registro").performClick()

        composeTestRule.onNodeWithText("Editar registro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar").assertIsDisplayed()
    }

    @Test
    fun deleteButton_removesEntryAfterConfirmation() {
        setMainScreen()

        composeTestRule.onNodeWithTag("fumei_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Excluir registro").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Excluir registro").performClick()
        composeTestRule.onNodeWithText("Excluir registro?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_delete_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("empty_entries_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("0 registros hoje").assertIsDisplayed()
    }

    private fun setMainScreen() {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsState()
            FumeiTheme(darkTheme = false) {
                MainScreen(
                    uiState = uiState,
                    onFumeiClick = viewModel::onFumeiClick,
                    onEditPuff = viewModel::onEditPuff,
                    onDeletePuff = viewModel::onDeletePuff,
                )
            }
        }
    }
}
