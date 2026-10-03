package fumei.faruk.dev.br

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import fumei.faruk.dev.br.data.AppDatabase
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.data.UserPreferencesRepository
import fumei.faruk.dev.br.startup.AppStartup
import fumei.faruk.dev.br.ui.AboutUiState
import fumei.faruk.dev.br.ui.FumeiApp
import fumei.faruk.dev.br.ui.MainViewModel
import fumei.faruk.dev.br.ui.MainViewModelFactory
import fumei.faruk.dev.br.ui.StatsViewModel
import fumei.faruk.dev.br.ui.StatsViewModelFactory
import fumei.faruk.dev.br.data.ReleaseHistoryRepository
import fumei.faruk.dev.br.ui.SettingsViewModel
import fumei.faruk.dev.br.ui.SettingsViewModelFactory
import fumei.faruk.dev.br.ui.UpdateViewModel
import fumei.faruk.dev.br.ui.UpdateViewModelFactory
import fumei.faruk.dev.br.ui.theme.FumeiTheme
import fumei.faruk.dev.br.update.AppUpdaterProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val userSettings by lazy {
        UserPreferencesRepository(applicationContext)
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(applicationContext, userSettings)
    }

    private val statsViewModel: StatsViewModel by viewModels {
        StatsViewModelFactory(applicationContext)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(userSettings)
    }

    private val updateViewModel: UpdateViewModel by viewModels {
        UpdateViewModelFactory(AppUpdaterProvider.create(applicationContext), userSettings)
    }

    override fun onResume() {
        super.onResume()
        updateViewModel.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            val repository = PuffRepository(
                fumei.faruk.dev.br.data.AppDatabase.getInstance(applicationContext).puffDao(),
            )
            AppStartup.hook.onAppStart(applicationContext, repository)
        }
        setContent {
            FumeiTheme {
                val homeState by mainViewModel.uiState.collectAsState()
                val statsState by statsViewModel.uiState.collectAsState()
                val dailyGoal by settingsViewModel.dailyGoal.collectAsState()
                val defaultGrams by settingsViewModel.defaultGramsPerSession.collectAsState()
                val updateState by updateViewModel.uiState.collectAsState()
                val aboutState = remember {
                    val history = ReleaseHistoryRepository(applicationContext).load()
                    AboutUiState(
                        versionName = BuildConfig.VERSION_NAME,
                        versionCode = BuildConfig.VERSION_CODE,
                        entries = history.entries,
                    )
                }

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
                    updateState = updateState,
                    onUpdateClick = { updateViewModel.onUpdateClick(this) },
                    onUpdateDismiss = updateViewModel::onPromptDismissed,
                    onRestartClick = updateViewModel::onRestartClick,
                    onRestartPostpone = updateViewModel::onRestartPostponed,
                )
            }
        }
    }
}
