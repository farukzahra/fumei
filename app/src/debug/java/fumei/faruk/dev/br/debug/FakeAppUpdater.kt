package fumei.faruk.dev.br.debug

import android.app.Activity
import fumei.faruk.dev.br.update.AppUpdater
import fumei.faruk.dev.br.update.UpdateAvailability
import fumei.faruk.dev.br.update.UpdateSignals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppUpdater(
    initial: UpdateSignals = UpdateSignals(UpdateAvailability.Available),
) : AppUpdater {
    private val state = MutableStateFlow(initial)

    var startRequested: Boolean = false
        private set

    var completeRequested: Boolean = false
        private set

    override suspend fun check(): UpdateSignals = state.value

    override fun observeInstallState(): Flow<UpdateSignals> = state

    override suspend fun startFlexibleUpdate(activity: Activity): Boolean {
        startRequested = true
        state.value = UpdateSignals(UpdateAvailability.Downloading, 50, 100)
        return true
    }

    override suspend fun completeUpdate() {
        completeRequested = true
        state.value = UpdateSignals(UpdateAvailability.None)
    }

    fun emit(signals: UpdateSignals) {
        state.value = signals
    }
}
