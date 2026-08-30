package fumei.faruk.dev.br.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fumei.faruk.dev.br.data.DailyGoalStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dailyGoalStore: DailyGoalStore,
) : ViewModel() {
    val dailyGoal: StateFlow<Int> = dailyGoalStore.observeDailyGoal()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyProgress.DEFAULT_GOAL,
        )

    fun setDailyGoal(value: Int) {
        viewModelScope.launch {
            dailyGoalStore.setDailyGoal(value)
        }
    }

    fun incrementDailyGoal() {
        setDailyGoal(dailyGoal.value + 1)
    }

    fun decrementDailyGoal() {
        setDailyGoal(dailyGoal.value - 1)
    }
}

class SettingsViewModelFactory(
    private val dailyGoalStore: DailyGoalStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(dailyGoalStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
