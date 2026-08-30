package fumei.faruk.dev.br.data

import android.content.Context
import org.json.JSONObject

class ReleaseHistoryRepository(
    private val context: Context,
) {
    fun load(): ReleaseHistory {
        val jsonText = context.assets.open("release-history.json")
            .bufferedReader()
            .use { it.readText() }
        val root = JSONObject(jsonText)
        val entriesJson = root.getJSONArray("entries")
        val entries = buildList {
            for (index in 0 until entriesJson.length()) {
                val item = entriesJson.getJSONObject(index)
                add(
                    ReleaseHistoryEntry(
                        version = item.getString("version"),
                        title = item.getString("title"),
                        summary = item.getString("summary"),
                    ),
                )
            }
        }
        return ReleaseHistory(
            currentVersion = root.getString("currentVersion"),
            versionCode = root.getInt("versionCode"),
            entries = entries,
        )
    }
}
