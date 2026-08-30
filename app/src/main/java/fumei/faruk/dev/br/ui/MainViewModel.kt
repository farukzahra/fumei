package fumei.faruk.dev.br.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fumei.faruk.dev.br.data.DailyGoalStore
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.data.TodayPuffData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainViewModel(
    private val repository: PuffRepository,
    private val dailyGoalStore: DailyGoalStore,
) : ViewModel() {
    private val zone = ZoneId.systemDefault()
    private val locale = Locale.forLanguageTag("pt-BR")
    private val entryFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dateHeaderFormatter = DateTimeFormatter.ofPattern("EEEE · d MMM", locale)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", locale)

    val uiState: StateFlow<TodayUiState> = combine(
        repository.observeTodayWithYesterdayCount(zone),
        dailyGoalStore.observeDailyGoal(),
    ) { data, dailyGoal ->
        mapToUiState(data, dailyGoal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    fun onFumeiClick() {
        viewModelScope.launch {
            repository.addPuff(Instant.now())
        }
    }

    fun onDeletePuff(id: Long) {
        viewModelScope.launch {
            repository.deletePuff(id)
        }
    }

    fun onEditPuff(id: Long, timestampMillis: Long) {
        viewModelScope.launch {
            repository.updatePuffTimestamp(id, Instant.ofEpochMilli(timestampMillis))
        }
    }

    private fun mapToUiState(data: TodayPuffData, dailyGoal: Int): TodayUiState {
        val puffs = data.puffs
        val today = Instant.now().atZone(zone).toLocalDate()
        val todayLabel = today.format(dayFormatter).replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(locale) else char.toString()
        }
        val dateHeader = today.format(dateHeaderFormatter)
            .uppercase(locale)
        val count = puffs.size
        val delta = count - data.yesterdayCount
        val vsYesterdayLabel = when {
            delta > 0 -> "↗ +$delta vs ontem"
            delta < 0 -> "↘ $delta vs ontem"
            else -> "= igual a ontem"
        }
        val entries = puffs.map { puff ->
            val zoned = Instant.ofEpochMilli(puff.timestamp).atZone(zone)
            PuffListItem(
                id = puff.id,
                label = zoned.format(entryFormatter),
                timeLabel = zoned.format(timeFormatter),
                contextLabel = "Hoje",
                timestampMillis = puff.timestamp,
            )
        }
        return TodayUiState(
            count = count,
            todayLabel = todayLabel,
            dateHeader = dateHeader,
            vsYesterdayLabel = vsYesterdayLabel,
            dailyGoal = DailyProgress.normalizedGoal(dailyGoal),
            progressLabel = DailyProgress.label(count, dailyGoal),
            progressFraction = DailyProgress.fraction(count, dailyGoal),
            entries = entries,
        )
    }
}

class MainViewModelFactory(
    private val repository: PuffRepository,
    private val dailyGoalStore: DailyGoalStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, dailyGoalStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
