package fumei.faruk.dev.br.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fumei.faruk.dev.br.data.AppDatabase
import fumei.faruk.dev.br.data.ConsumptionSettings
import fumei.faruk.dev.br.data.DefaultGramsStore
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.data.TodayPuffData
import fumei.faruk.dev.br.data.UserSettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainViewModel(
    private val repository: PuffRepository,
    private val userSettings: UserSettingsStore,
) : ViewModel() {
    private val zone = ZoneId.systemDefault()
    private val locale = Locale.forLanguageTag("pt-BR")
    private val entryFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dateHeaderFormatter = DateTimeFormatter.ofPattern("EEEE · d MMM", locale)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", locale)

    val uiState: StateFlow<TodayUiState> = combine(
        repository.observeTodayWithYesterdayCount(zone),
        userSettings.observeDailyGoal(),
    ) { data, dailyGoal ->
        mapToUiState(data, dailyGoal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    fun onFumeiClick() {
        viewModelScope.launch {
            val defaultGrams = userSettings.observeDefaultGramsPerSession().first()
            repository.addPuff(Instant.now(), defaultGrams)
        }
    }

    fun onDeletePuff(id: Long) {
        viewModelScope.launch {
            repository.deletePuff(id)
        }
    }

    fun onEditPuff(id: Long, timestampMillis: Long, grams: Double) {
        viewModelScope.launch {
            repository.updatePuff(id, Instant.ofEpochMilli(timestampMillis), grams)
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
        val totalGrams = puffs.sumOf { it.grams }
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
                gramsLabel = ConsumptionFormat.formatGramsWithUnit(puff.grams),
                contextLabel = "Hoje",
                timestampMillis = puff.timestamp,
                grams = puff.grams,
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
            gramsTodayLabel = if (count > 0) {
                ConsumptionFormat.todayTotalLabel(totalGrams)
            } else {
                ""
            },
            entries = entries,
        )
    }
}

class MainViewModelFactory(
    private val appContext: Context,
    private val userSettings: UserSettingsStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val repository = PuffRepository(AppDatabase.getInstance(appContext).puffDao())
            return MainViewModel(repository, userSettings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
