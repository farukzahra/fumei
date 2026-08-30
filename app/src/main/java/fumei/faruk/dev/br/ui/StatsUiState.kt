package fumei.faruk.dev.br.ui

import fumei.faruk.dev.br.stats.CalendarDayCell
import fumei.faruk.dev.br.stats.MonthCell
import fumei.faruk.dev.br.stats.StatsScope
import fumei.faruk.dev.br.stats.YearCell

data class StatsUiState(
    val scope: StatsScope = StatsScope.MONTH,
    val periodLabel: String = "",
    val periodTotal: Int = 0,
    val periodTotalLabel: String = "",
    val canGoNext: Boolean = false,
    val canGoPrevious: Boolean = true,
    val zoomHint: String = "",
    val weekDayLabels: List<String> = emptyList(),
    val calendarDays: List<CalendarDayCell> = emptyList(),
    val monthCells: List<MonthCell> = emptyList(),
    val yearCells: List<YearCell> = emptyList(),
)
