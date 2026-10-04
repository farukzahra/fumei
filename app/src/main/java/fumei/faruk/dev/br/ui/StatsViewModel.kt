package fumei.faruk.dev.br.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fumei.faruk.dev.br.data.AppDatabase
import fumei.faruk.dev.br.data.PuffEntity
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.export.CsvExport
import fumei.faruk.dev.br.export.PuffCsv
import fumei.faruk.dev.br.export.exportLabelFor
import fumei.faruk.dev.br.export.exportRangeFor
import fumei.faruk.dev.br.export.fileNameFor
import fumei.faruk.dev.br.stats.MonthCell
import fumei.faruk.dev.br.stats.StatsScope
import fumei.faruk.dev.br.stats.YearCell
import fumei.faruk.dev.br.stats.aggregateCountsByDay
import fumei.faruk.dev.br.stats.aggregateCountsByMonth
import fumei.faruk.dev.br.stats.aggregateCountsByYear
import fumei.faruk.dev.br.stats.aggregateGramsByDay
import fumei.faruk.dev.br.stats.aggregateGramsByMonth
import fumei.faruk.dev.br.stats.aggregateGramsByYear
import fumei.faruk.dev.br.stats.averageSessionIntervalMillis
import fumei.faruk.dev.br.stats.buildMonthCalendar
import fumei.faruk.dev.br.stats.monthGramsTotal
import fumei.faruk.dev.br.stats.monthTotal
import fumei.faruk.dev.br.stats.periodCountCaption
import fumei.faruk.dev.br.stats.periodGramsCaption
import fumei.faruk.dev.br.stats.totalGrams
import fumei.faruk.dev.br.stats.yearGramsTotal
import fumei.faruk.dev.br.stats.yearTotal
import fumei.faruk.dev.br.stats.yearsRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class StatsViewModel(
    private val repository: PuffRepository,
) : ViewModel() {
    private val zone = ZoneId.systemDefault()
    private val locale = Locale.forLanguageTag("pt-BR")
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", locale)
    private val monthShortFormatter = DateTimeFormatter.ofPattern("MMM", locale)

    private val scopeState = MutableStateFlow(StatsScope.MONTH)
    private val anchorMonth = MutableStateFlow(YearMonth.now())
    private val anchorYear = MutableStateFlow(LocalDate.now(zone).year)
    private val exportState = MutableStateFlow<CsvExport?>(null)

    val exportRequest: StateFlow<CsvExport?> = exportState.asStateFlow()

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeAllPuffs(),
        scopeState,
        anchorMonth,
        anchorYear,
    ) { puffs, scope, month, year ->
        buildUiState(puffs, scope, month, year)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    fun onPreviousPeriod() {
        when (scopeState.value) {
            StatsScope.MONTH -> anchorMonth.update { it.minusMonths(1) }
            StatsScope.YEAR -> anchorYear.update { it - 1 }
            StatsScope.YEARS -> Unit
        }
    }

    fun onNextPeriod() {
        val today = LocalDate.now(zone)
        when (scopeState.value) {
            StatsScope.MONTH -> {
                val next = anchorMonth.value.plusMonths(1)
                if (!next.isAfter(YearMonth.from(today))) {
                    anchorMonth.value = next
                }
            }
            StatsScope.YEAR -> {
                if (anchorYear.value < today.year) {
                    anchorYear.update { it + 1 }
                }
            }
            StatsScope.YEARS -> Unit
        }
    }

    fun onPeriodTitleClick() {
        when (scopeState.value) {
            StatsScope.MONTH -> {
                anchorYear.value = anchorMonth.value.year
                scopeState.value = StatsScope.YEAR
            }
            StatsScope.YEAR -> scopeState.value = StatsScope.YEARS
            StatsScope.YEARS -> {
                anchorMonth.value = YearMonth.now()
                scopeState.value = StatsScope.MONTH
            }
        }
    }

    fun onMonthSelected(yearMonth: YearMonth) {
        anchorMonth.value = yearMonth
        scopeState.value = StatsScope.MONTH
    }

    fun onYearSelected(year: Int) {
        anchorYear.value = year
        scopeState.value = StatsScope.YEAR
    }

    fun onExportClick() {
        viewModelScope.launch {
            val scope = scopeState.value
            val range = exportRangeFor(scope, anchorMonth.value, anchorYear.value, zone)
            val puffs = if (range == null) {
                repository.allPuffs()
            } else {
                repository.puffsBetween(range.startMillis, range.endMillis)
            }
            exportState.value = CsvExport(
                fileName = fileNameFor(scope, anchorMonth.value, anchorYear.value),
                content = PuffCsv.build(puffs, zone),
            )
        }
    }

    fun onExportHandled() {
        exportState.value = null
    }

    private fun buildUiState(
        puffs: List<PuffEntity>,
        scope: StatsScope,
        month: YearMonth,
        year: Int,
    ): StatsUiState {
        val today = LocalDate.now(zone)
        val countsByDay = aggregateCountsByDay(puffs, zone)
        val countsByMonth = aggregateCountsByMonth(puffs, zone)
        val countsByYear = aggregateCountsByYear(puffs, zone)
        val gramsByDay = aggregateGramsByDay(puffs, zone)
        val gramsByMonth = aggregateGramsByMonth(puffs, zone)
        val gramsByYear = aggregateGramsByYear(puffs, zone)
        val weekFields = WeekFields.of(locale)
        val weekDayLabels = (0..6).map { offset ->
            weekFields.firstDayOfWeek.plus(offset.toLong())
                .getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                .replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(locale) else char.toString()
                }
        }

        return when (scope) {
            StatsScope.MONTH -> {
                val total = monthTotal(month, countsByDay)
                val totalGrams = monthGramsTotal(month, gramsByDay)
                val averageInterval = averageSessionIntervalMillis(puffs, month, zone)
                StatsUiState(
                    scope = scope,
                    periodLabel = month.atDay(1).format(monthTitleFormatter)
                        .replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase(locale) else char.toString()
                        },
                    periodTotal = total,
                    sessionsStat = StatsSummaryStat(
                        valueLabel = total.toString(),
                        captionLabel = periodCountCaption(scope),
                    ),
                    gramsStat = StatsSummaryStat(
                        valueLabel = "${ConsumptionFormat.formatGramsTotal(totalGrams)} g",
                        captionLabel = periodGramsCaption(scope),
                    ),
                    averageStat = StatsSummaryStat(
                        valueLabel = averageInterval?.let {
                            SessionIntervalFormat.formatElapsedMillis(it)
                        } ?: "sem dados",
                        captionLabel = "média entre sessões do mesmo dia",
                    ),
                    exportLabel = exportLabelFor(scope),
                    canGoNext = month.isBefore(YearMonth.from(today)),
                    canGoPrevious = true,
                    zoomHint = "Toque no título para ver o ano",
                    weekDayLabels = weekDayLabels,
                    calendarDays = buildMonthCalendar(month, countsByDay, locale, gramsByDay),
                )
            }
            StatsScope.YEAR -> {
                val total = yearTotal(year, countsByMonth)
                val totalGrams = yearGramsTotal(year, gramsByMonth)
                val months = (1..12).map { monthNumber ->
                    val yearMonth = YearMonth.of(year, monthNumber)
                    MonthCell(
                        yearMonth = yearMonth,
                        count = countsByMonth[yearMonth] ?: 0,
                        grams = gramsByMonth[yearMonth] ?: 0.0,
                    )
                }
                StatsUiState(
                    scope = scope,
                    periodLabel = year.toString(),
                    periodTotal = total,
                    sessionsStat = StatsSummaryStat(
                        valueLabel = total.toString(),
                        captionLabel = periodCountCaption(scope),
                    ),
                    gramsStat = StatsSummaryStat(
                        valueLabel = "${ConsumptionFormat.formatGramsTotal(totalGrams)} g",
                        captionLabel = periodGramsCaption(scope),
                    ),
                    exportLabel = exportLabelFor(scope),
                    canGoNext = year < today.year,
                    canGoPrevious = true,
                    zoomHint = "Toque em um mês para ver os dias",
                    monthCells = months,
                )
            }
            StatsScope.YEARS -> {
                val total = puffs.size
                val allGrams = totalGrams(puffs)
                val years = yearsRange(puffs, zone, today.year)
                    .reversed()
                    .map { yearValue ->
                        YearCell(
                            year = yearValue,
                            count = countsByYear[yearValue] ?: 0,
                            grams = gramsByYear[yearValue] ?: 0.0,
                        )
                    }
                StatsUiState(
                    scope = scope,
                    periodLabel = "Todos os anos",
                    periodTotal = total,
                    sessionsStat = StatsSummaryStat(
                        valueLabel = total.toString(),
                        captionLabel = periodCountCaption(scope),
                    ),
                    gramsStat = StatsSummaryStat(
                        valueLabel = "${ConsumptionFormat.formatGramsTotal(allGrams)} g",
                        captionLabel = periodGramsCaption(scope),
                    ),
                    exportLabel = exportLabelFor(scope),
                    canGoNext = false,
                    canGoPrevious = false,
                    zoomHint = "Toque em um ano para ver os meses",
                    yearCells = years,
                )
            }
        }
    }
}

class StatsViewModelFactory(
    private val appContext: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            val repository = PuffRepository(AppDatabase.getInstance(appContext).puffDao())
            return StatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
