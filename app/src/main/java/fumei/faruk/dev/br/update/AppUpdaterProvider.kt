package fumei.faruk.dev.br.update

import android.content.Context

object AppUpdaterProvider {
    var factory: (Context) -> AppUpdater = { context -> PlayAppUpdater(context) }

    fun create(context: Context): AppUpdater = factory(context.applicationContext)
}
