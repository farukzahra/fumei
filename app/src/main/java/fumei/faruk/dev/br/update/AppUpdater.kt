package fumei.faruk.dev.br.update

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface AppUpdater {
    suspend fun check(): UpdateSignals

    fun observeInstallState(): Flow<UpdateSignals>

    suspend fun startFlexibleUpdate(activity: Activity): Boolean

    suspend fun completeUpdate()
}
