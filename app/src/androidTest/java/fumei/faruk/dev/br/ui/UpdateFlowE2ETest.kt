package fumei.faruk.dev.br.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fumei.faruk.dev.br.data.UpdatePromptStore
import fumei.faruk.dev.br.debug.FakeAppUpdater
import fumei.faruk.dev.br.ui.theme.FumeiTheme
import fumei.faruk.dev.br.update.UpdateAvailability
import fumei.faruk.dev.br.update.UpdateSignals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateFlowE2ETest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun updateDialog_appearsWithAnUpdateAndGoesAwayOnDismiss() {
        val store = FakePromptStore()
        setApp(FakeAppUpdater(UpdateSignals(UpdateAvailability.Available)), store)

        composeTestRule.onNodeWithText("Tem atualização disponível").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_dialog_later_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("update_dialog").fetchSemanticsNodes().isEmpty()
        }
        assertTrue(store.dismissed)
    }

    @Test
    fun updateDialog_staysHiddenWhenThePromptWasDismissedBefore() {
        val store = FakePromptStore(initiallyDismissed = true)
        setApp(FakeAppUpdater(UpdateSignals(UpdateAvailability.Available)), store)

        composeTestRule.onNodeWithTag("update_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithTag("nav_about").performClick()
        composeTestRule.onNodeWithText("Versão nova disponível").assertIsDisplayed()
    }

    @Test
    fun updateCard_startsTheFlexibleUpdateAndShowsProgress() {
        val store = FakePromptStore(initiallyDismissed = true)
        val updater = FakeAppUpdater(UpdateSignals(UpdateAvailability.Available))
        setApp(updater, store)

        composeTestRule.onNodeWithTag("nav_about").performClick()
        composeTestRule.onNodeWithTag("update_card_action").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Baixando 50%").fetchSemanticsNodes().isNotEmpty()
        }
        assertTrue(updater.startRequested)
    }

    @Test
    fun restartDialog_completesTheUpdate() {
        val store = FakePromptStore(initiallyDismissed = true)
        val updater = FakeAppUpdater(UpdateSignals(UpdateAvailability.Downloaded, 100, 100))
        setApp(updater, store)

        composeTestRule.onNodeWithText("Atualização pronta").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restart_dialog_restart_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { updater.completeRequested }
    }

    @Test
    fun restartDialog_laterKeepsTheCardAction() {
        val store = FakePromptStore(initiallyDismissed = true)
        setApp(FakeAppUpdater(UpdateSignals(UpdateAvailability.Downloaded, 100, 100)), store)

        composeTestRule.onNodeWithTag("restart_dialog_later_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("restart_dialog").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag("nav_about").performClick()
        composeTestRule.onNodeWithText("Pronta para instalar").assertIsDisplayed()
    }

    @Test
    fun updateCard_isHiddenWhenPlayDoesNotAnswer() {
        val store = FakePromptStore()
        setApp(FakeAppUpdater(UpdateSignals(UpdateAvailability.Unknown)), store)

        composeTestRule.onNodeWithTag("nav_about").performClick()
        composeTestRule.onNodeWithTag("more_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_card").assertDoesNotExist()
    }

    @Test
    fun updateCard_showsTheUpToDateMessage() {
        val store = FakePromptStore()
        setApp(FakeAppUpdater(UpdateSignals(UpdateAvailability.None)), store)

        composeTestRule.onNodeWithTag("nav_about").performClick()
        composeTestRule.onNodeWithText("Você está na versão mais recente").assertIsDisplayed()
    }

    private fun setApp(updater: FakeAppUpdater, store: FakePromptStore) {
        val updateViewModel = UpdateViewModel(updater, store)
        composeTestRule.setContent {
            val updateState by updateViewModel.uiState.collectAsState()
            FumeiTheme {
                FumeiApp(
                    homeState = TodayUiState(),
                    statsState = StatsUiState(),
                    aboutState = AboutUiState(
                        versionName = "1.0.0",
                        versionCode = 12,
                        entries = emptyList(),
                    ),
                    dailyGoal = 8,
                    defaultGramsPerSession = 0.3,
                    onFumeiClick = {},
                    onEditPuff = { _, _, _ -> },
                    onDeletePuff = {},
                    onStatsPrevious = {},
                    onStatsNext = {},
                    onStatsTitleClick = {},
                    onStatsMonthSelected = { _ -> },
                    onStatsYearSelected = { _ -> },
                    onDailyGoalIncrement = {},
                    onDailyGoalDecrement = {},
                    onDefaultGramsIncrement = {},
                    onDefaultGramsDecrement = {},
                    onUpdateClick = { updateViewModel.onUpdateClick(composeTestRule.activity) },
                    onUpdateDismiss = updateViewModel::onPromptDismissed,
                    onRestartClick = updateViewModel::onRestartClick,
                    onRestartPostpone = updateViewModel::onRestartPostponed,
                    updateState = updateState,
                )
            }
        }
    }
}

private class FakePromptStore(
    initiallyDismissed: Boolean = false,
) : UpdatePromptStore {
    private val state = MutableStateFlow(initiallyDismissed)

    val dismissed: Boolean get() = state.value

    override fun observePromptDismissed(): Flow<Boolean> = state

    override suspend fun setPromptDismissed(value: Boolean) {
        state.value = value
    }
}
