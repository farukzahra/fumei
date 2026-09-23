package fumei.faruk.dev.br.debug

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoDebugSeedProviderTest {
    @Test
    fun mergedManifest_doesNotRegisterDebugSeedProvider() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertThrows(PackageManager.NameNotFoundException::class.java) {
            context.packageManager.getProviderInfo(
                android.content.ComponentName(
                    context,
                    "fumei.faruk.dev.br.debug.DebugInitProvider",
                ),
                PackageManager.GET_META_DATA,
            )
        }
    }
}
