package fumei.faruk.dev.br.startup

import android.content.Context
import fumei.faruk.dev.br.data.PuffRepository

fun interface AppStartupHook {
    suspend fun onAppStart(context: Context, repository: PuffRepository)
}

object AppStartup {
    var hook: AppStartupHook = AppStartupHook { _, _ -> }
}
