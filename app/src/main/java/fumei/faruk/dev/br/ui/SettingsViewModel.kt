package fumei.faruk.dev.br.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fumei.faruk.dev.br.data.ConsumptionSettings
import fumei.faruk.dev.br.data.UserSettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userSettings: UserSettingsStore,
) : ViewModel() {
    val dailyGoal: StateFlow<Int> = userSettings.observeDailyGoal()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyProgress.DEFAULT_GOAL,
        )

    val defaultGramsPerSession: StateFlow<Double> = userSettings.observeDefaultGramsPerSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConsumptionSettings.DEFAULT_GRAMS_PER_SESSION,
        )

    fun setDailyGoal(value: Int) {
        viewModelScope.launch {
            userSettings.setDailyGoal(value)
        }
    }

    fun incrementDailyGoal() {
        setDailyGoal(dailyGoal.value + 1)
    }

    fun decrementDailyGoal() {
        setDailyGoal(dailyGoal.value - 1)
    }

    fun setDefaultGramsPerSession(value: Double) {
        viewModelScope.launch {
            userSettings.setDefaultGramsPerSession(value)
        }
    }

    fun incrementDefaultGrams() {
        setDefaultGramsPerSession(defaultGramsPerSession.value + ConsumptionSettings.GRAMS_STEP)
    }

    fun decrementDefaultGrams() {
        setDefaultGramsPerSession(defaultGramsPerSession.value - ConsumptionSettings.GRAMS_STEP)
    }
}

class SettingsViewModelFactory(
    private val userSettings: UserSettingsStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(userSettings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
