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
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fumei.faruk.dev.br.data.AppDatabase
import fumei.faruk.dev.br.data.PuffEntity
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.data.UserPreferencesRepository
import fumei.faruk.dev.br.ui.theme.FumeiTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class FumeiAppE2ETest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: PuffRepository
    private lateinit var dailyGoalStore: UserPreferencesRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var statsViewModel: StatsViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PuffRepository(database.puffDao())
        dailyGoalStore = UserPreferencesRepository(context)
        runBlocking { dailyGoalStore.setDailyGoal(8) }
        mainViewModel = MainViewModel(repository, dailyGoalStore)
        statsViewModel = StatsViewModel(repository)
        settingsViewModel = SettingsViewModel(dailyGoalStore)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fumeiButton_addsEntryAndUpdatesCounter() {
        setFumeiApp()

        composeTestRule.onNodeWithTag("fumei_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("entries_timeline").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("entries_timeline").assertIsDisplayed()
    }

    @Test
    fun editButton_opensEditDialog() {
        setFumeiApp()

        composeTestRule.onNodeWithTag("fumei_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("timeline_entry").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithTag("timeline_entry").onFirst().performClick()
        composeTestRule.onNodeWithText("Editar registro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar").assertIsDisplayed()
    }

    @Test
    fun timelineEntry_tapOpensEditDialog() {
        setFumeiApp()

        composeTestRule.onNodeWithTag("fumei_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Editar registro").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Editar registro").performClick()
        composeTestRule.onNodeWithText("Editar registro").assertIsDisplayed()
    }

    @Test
    fun deleteButton_removesEntryAfterConfirmation() {
        setFumeiApp()

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
    }

    @Test
    fun statsTab_showsMonthCalendarWithCounts() {
        seedPuffsForCurrentMonth(count = 2)
        refreshViewModels()
        setFumeiApp()

        composeTestRule.onNodeWithTag("nav_stats").performClick()
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodesWithTag("stats_period_title").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("stats_month_grid").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 no mês", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("g no mês", substring = true).assertIsDisplayed()
    }

    @Test
    fun statsTitle_zoomsToYearAndYearsViews() {
        seedPuffsForCurrentMonth(count = 2)
        refreshViewModels()
        setFumeiApp()

        composeTestRule.onNodeWithTag("nav_stats").performClick()
        composeTestRule.onNodeWithTag("stats_period_title").performClick()
        composeTestRule.onNodeWithTag("stats_year_grid").assertIsDisplayed()
        composeTestRule.onNodeWithText("(2)", substring = true).assertIsDisplayed()

        composeTestRule.onNodeWithTag("stats_period_title").performClick()
        composeTestRule.onNodeWithTag("stats_years_grid").assertIsDisplayed()
    }

    @Test
    fun aboutTab_showsDailyGoalAndLatestHistory() {
        setFumeiApp(aboutState = sampleAboutState())

        composeTestRule.onNodeWithTag("nav_about").performClick()
        composeTestRule.onNodeWithTag("more_nav_settings").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_goal_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_goal_value").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Voltar").performClick()
        composeTestRule.onNodeWithTag("more_nav_about").performClick()
        composeTestRule.onNodeWithTag("about_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_version_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_history_1.0.0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_pix_card").performScrollTo()
        composeTestRule.onNodeWithTag("about_pix_copy").assertIsDisplayed()
    }

    @Test
    fun useiAgora_showsGramsOnTimelineAndSummary() {
        setFumeiApp()

        composeTestRule.onNodeWithTag("fumei_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("daily_grams_label").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("0,3 g fumadas hoje").assertIsDisplayed()
        composeTestRule.onNodeWithText("0,3 g").assertIsDisplayed()
    }

    @Test
    fun bottomNav_selectedTab_hasNoIndicatorDot() {
        setFumeiApp()

        composeTestRule.onNodeWithTag("nav_stats").performClick()
        composeTestRule.onNodeWithTag("nav_about").performClick()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule.onAllNodesWithTag("nav_selected_indicator").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun dailyGoalChange_updatesHomeProgressLabel() {
        setFumeiApp(aboutState = sampleAboutState())

        composeTestRule.onNodeWithTag("nav_about").performClick()
        composeTestRule.onNodeWithTag("more_nav_settings").performClick()
        composeTestRule.onNodeWithTag("daily_goal_increase").performClick()
        composeTestRule.onNodeWithTag("daily_goal_increase").performClick()

        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.onNodeWithTag("fumei_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("1 de 10").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("1 de 10").assertIsDisplayed()
    }

    private fun seedPuffsForCurrentMonth(count: Int) {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        runBlocking {
            repeat(count) { index ->
                repository.addPuff(java.time.Instant.ofEpochMilli(start + index * 1_000L))
            }
        }
    }

    private fun refreshViewModels() {
        mainViewModel = MainViewModel(repository, dailyGoalStore)
        statsViewModel = StatsViewModel(repository)
        settingsViewModel = SettingsViewModel(dailyGoalStore)
    }

    private fun sampleAboutState(): AboutUiState {
        return AboutUiState(
            versionName = "1.0.0",
            versionCode = 12,
            entries = listOf(
                fumei.faruk.dev.br.data.ReleaseHistoryEntry(
                    version = "1.0.0",
                    title = "Lançamento na Play Store",
                    summary = "Contador diário com meta, estatísticas e dados offline.",
                ),
            ),
        )
    }

    private fun setFumeiApp(aboutState: AboutUiState = sampleAboutState()) {
        composeTestRule.setContent {
            val homeState by mainViewModel.uiState.collectAsState()
            val statsState by statsViewModel.uiState.collectAsState()
            val dailyGoal by settingsViewModel.dailyGoal.collectAsState()
            val defaultGrams by settingsViewModel.defaultGramsPerSession.collectAsState()
            FumeiTheme {
                FumeiApp(
                    homeState = homeState,
                    statsState = statsState,
                    aboutState = aboutState,
                    dailyGoal = dailyGoal,
                    defaultGramsPerSession = defaultGrams,
                    onFumeiClick = mainViewModel::onFumeiClick,
                    onEditPuff = mainViewModel::onEditPuff,
                    onDeletePuff = mainViewModel::onDeletePuff,
                    onStatsPrevious = statsViewModel::onPreviousPeriod,
                    onStatsNext = statsViewModel::onNextPeriod,
                    onStatsTitleClick = statsViewModel::onPeriodTitleClick,
                    onStatsMonthSelected = statsViewModel::onMonthSelected,
                    onStatsYearSelected = statsViewModel::onYearSelected,
                    onDailyGoalIncrement = settingsViewModel::incrementDailyGoal,
                    onDailyGoalDecrement = settingsViewModel::decrementDailyGoal,
                    onDefaultGramsIncrement = settingsViewModel::incrementDefaultGrams,
                    onDefaultGramsDecrement = settingsViewModel::decrementDefaultGrams,
                )
            }
        }
    }
}
