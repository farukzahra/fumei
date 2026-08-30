package fumei.faruk.dev.br.ui

data class PuffListItem(
    val id: Long,
    val label: String,
    val timeLabel: String,
    val contextLabel: String,
    val timestampMillis: Long,
)

data class TodayUiState(
    val count: Int = 0,
    val todayLabel: String = "",
    val dateHeader: String = "",
    val vsYesterdayLabel: String? = null,
    val progressLabel: String = "",
    val progressFraction: Float = 0f,
    val entries: List<PuffListItem> = emptyList(),
)
