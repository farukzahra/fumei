package fumei.faruk.dev.br.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fumei.faruk.dev.br.data.PuffEntity
import fumei.faruk.dev.br.data.PuffRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainViewModel(
    private val repository: PuffRepository,
) : ViewModel() {
    private val zone = ZoneId.systemDefault()
    private val locale = Locale.forLanguageTag("pt-BR")
    private val entryFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", locale)

    val uiState: StateFlow<TodayUiState> = repository.observeTodayPuffs(zone)
        .map(::mapToUiState)
        .stateIn(
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

    private fun mapToUiState(puffs: List<PuffEntity>): TodayUiState {
        val today = Instant.now().atZone(zone).toLocalDate()
        val todayLabel = today.format(dayFormatter).replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(locale) else char.toString()
        }
        val entries = puffs.map { puff ->
            val label = Instant.ofEpochMilli(puff.timestamp)
                .atZone(zone)
                .format(entryFormatter)
            PuffListItem(
                id = puff.id,
                label = label,
                timestampMillis = puff.timestamp,
            )
        }
        return TodayUiState(
            count = puffs.size,
            todayLabel = todayLabel,
            entries = entries,
        )
    }
}

class MainViewModelFactory(
    private val repository: PuffRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
