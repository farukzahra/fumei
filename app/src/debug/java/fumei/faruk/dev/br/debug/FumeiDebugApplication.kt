package fumei.faruk.dev.br.debug

import android.app.Application
import fumei.faruk.dev.br.startup.AppStartup

class FumeiDebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStartup.hook = DebugDevSeeder.startupHook
    }
}
