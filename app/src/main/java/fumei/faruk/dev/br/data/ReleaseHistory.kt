package fumei.faruk.dev.br.data

data class ReleaseHistoryEntry(
    val version: String,
    val title: String,
    val summary: String,
)

data class ReleaseHistory(
    val currentVersion: String,
    val versionCode: Int,
    val entries: List<ReleaseHistoryEntry>,
)
