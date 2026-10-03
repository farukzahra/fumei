package fumei.faruk.dev.br.debug

import android.app.Application
import fumei.faruk.dev.br.startup.AppStartup
import fumei.faruk.dev.br.update.AppUpdaterProvider

class FumeiDebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStartup.hook = DebugDevSeeder.startupHook
        if (DebugDevSeeder.isAndroidEmulator()) {
            AppUpdaterProvider.factory = { FakeAppUpdater() }
        }
    }
}
