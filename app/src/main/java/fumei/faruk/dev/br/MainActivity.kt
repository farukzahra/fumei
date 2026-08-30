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
import fumei.faruk.dev.br.ui.theme.FumeiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        PuffRepository(AppDatabase.getInstance(applicationContext).puffDao())
    }

    private val dailyGoalStore by lazy {
        UserPreferencesRepository(applicationContext)
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository, dailyGoalStore)
    }

    private val statsViewModel: StatsViewModel by viewModels {
        StatsViewModelFactory(repository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(dailyGoalStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            AppStartup.hook.onAppStart(applicationContext, repository)
        }
        setContent {
            FumeiTheme {
                val homeState by mainViewModel.uiState.collectAsState()
                val statsState by statsViewModel.uiState.collectAsState()
                val dailyGoal by settingsViewModel.dailyGoal.collectAsState()
                val aboutState = remember(dailyGoal) {
                    val history = ReleaseHistoryRepository(applicationContext).load()
                    AboutUiState(
                        versionName = BuildConfig.VERSION_NAME,
                        versionCode = BuildConfig.VERSION_CODE,
                        entries = history.entries,
                        dailyGoal = dailyGoal,
                    )
                }

                FumeiApp(
                    homeState = homeState,
                    statsState = statsState,
                    aboutState = aboutState,
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
                )
            }
        }
    }
}
