package fumei.faruk.dev.br.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fumei.faruk.dev.br.data.UpdatePromptStore
import fumei.faruk.dev.br.update.AppUpdater
import fumei.faruk.dev.br.update.UpdateAvailability
import fumei.faruk.dev.br.update.UpdateCardLabels
import fumei.faruk.dev.br.update.UpdatePromptPolicy
import fumei.faruk.dev.br.update.UpdateSignals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UpdateViewModel(
    private val updater: AppUpdater,
    private val promptStore: UpdatePromptStore,
) : ViewModel() {
    private val signals = MutableStateFlow(UpdateSignals())
    private val restartPostponed = MutableStateFlow(false)

    val uiState: StateFlow<UpdateUiState> = combine(
        signals,
        promptStore.observePromptDismissed(),
        restartPostponed,
    ) { currentSignals, dismissed, postponed ->
        UpdateUiState(
            dialog = UpdatePromptPolicy.dialogFor(currentSignals, dismissed, postponed),
            card = UpdateCardLabels.of(currentSignals),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateUiState(),
    )

    init {
        viewModelScope.launch {
            updater.observeInstallState().collect { applySignals(it) }
        }
        viewModelScope.launch { refresh() }
    }

    fun onResume() {
        viewModelScope.launch { refresh() }
    }

    fun onUpdateClick(activity: Activity) {
        viewModelScope.launch { updater.startFlexibleUpdate(activity) }
    }

    fun onPromptDismissed() {
        viewModelScope.launch { promptStore.setPromptDismissed(true) }
    }

    fun onRestartClick() {
        viewModelScope.launch { updater.completeUpdate() }
    }

    fun onRestartPostponed() {
        restartPostponed.value = true
    }

    private suspend fun refresh() {
        applySignals(updater.check())
    }

    private suspend fun applySignals(state: UpdateSignals) {
        signals.value = state
        if (state.availability != UpdateAvailability.Downloaded) {
            restartPostponed.value = false
        }
        if (UpdatePromptPolicy.clearsDismissal(state)) {
            promptStore.setPromptDismissed(false)
        }
    }
}

class UpdateViewModelFactory(
    private val updater: AppUpdater,
    private val promptStore: UpdatePromptStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateViewModel::class.java)) {
            return UpdateViewModel(updater, promptStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
