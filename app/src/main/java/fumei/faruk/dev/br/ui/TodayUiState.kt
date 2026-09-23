package fumei.faruk.dev.br.ui

data class PuffListItem(
    val id: Long,
    val label: String,
    val timeLabel: String,
    val gramsLabel: String,
    val contextLabel: String,
    val timestampMillis: Long,
    val grams: Double,
)

data class TodayUiState(
    val count: Int = 0,
    val todayLabel: String = "",
    val dateHeader: String = "",
    val vsYesterdayLabel: String? = null,
    val dailyGoal: Int = DailyProgress.DEFAULT_GOAL,
    val progressLabel: String = "",
    val progressFraction: Float = 0f,
    val gramsTodayLabel: String = "",
    val entries: List<PuffListItem> = emptyList(),
)
