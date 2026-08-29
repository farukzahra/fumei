package fumei.faruk.dev.br.ui

data class PuffListItem(
    val id: Long,
    val label: String,
    val timestampMillis: Long,
)

data class TodayUiState(
    val count: Int = 0,
    val todayLabel: String = "",
    val entries: List<PuffListItem> = emptyList(),
)
