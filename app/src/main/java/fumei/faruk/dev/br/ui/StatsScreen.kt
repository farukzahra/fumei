package fumei.faruk.dev.br.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import fumei.faruk.dev.br.stats.StatsScope
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onPeriodTitleClick: () -> Unit,
    onMonthSelected: (java.time.YearMonth) -> Unit,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.forLanguageTag("pt-BR")
    val monthLabelFormatter = DateTimeFormatter.ofPattern("MMMM", locale)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatsPeriodHeader(
            label = uiState.periodLabel,
            totalLabel = uiState.periodTotalLabel,
            zoomHint = uiState.zoomHint,
            canGoPrevious = uiState.canGoPrevious,
            canGoNext = uiState.canGoNext,
            onPrevious = onPreviousPeriod,
            onNext = onNextPeriod,
            onTitleClick = onPeriodTitleClick,
        )

        when (uiState.scope) {
            StatsScope.MONTH -> {
                WeekdayHeader(labels = uiState.weekDayLabels)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_month_grid"),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(uiState.calendarDays, key = { it.date }) { day ->
                        DayCountCell(day = day)
                    }
                }
            }
            StatsScope.YEAR -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_year_grid"),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.monthCells, key = { it.yearMonth }) { month ->
                        val label = month.yearMonth.atDay(1)
                            .format(monthLabelFormatter)
                            .replaceFirstChar { char ->
                                if (char.isLowerCase()) char.titlecase(locale) else char.toString()
                            }
                        MonthCountCell(
                            label = label,
                            count = month.count,
                            onClick = { onMonthSelected(month.yearMonth) },
                        )
                    }
                }
            }
            StatsScope.YEARS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_years_grid"),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.yearCells, key = { it.year }) { year ->
                        YearCountCell(
                            year = year.year,
                            count = year.count,
                            onClick = { onYearSelected(year.year) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsPeriodHeader(
    label: String,
    totalLabel: String,
    zoomHint: String,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = canGoPrevious,
                    modifier = Modifier.testTag("stats_prev_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Período anterior",
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onTitleClick)
                        .testTag("stats_period_title"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = totalLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    )
                }
                IconButton(
                    onClick = onNext,
                    enabled = canGoNext,
                    modifier = Modifier.testTag("stats_next_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Próximo período",
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = zoomHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeekdayHeader(
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DayCountCell(
    day: fumei.faruk.dev.br.stats.CalendarDayCell,
    modifier: Modifier = Modifier,
) {
    val background = when {
        !day.inCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        day.count > 0 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("stats_day_${day.date}"),
        shape = RoundedCornerShape(12.dp),
        color = background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (day.inCurrentMonth) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
            )
            if (day.count > 0) {
                Text(
                    text = day.count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun MonthCountCell(
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("stats_month_$label"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (count > 0) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PeriodCountBadge(count = count)
        }
    }
}

@Composable
private fun YearCountCell(
    year: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("stats_year_$year"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (count > 0) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PeriodCountBadge(count = count)
        }
    }
}

@Composable
private fun PeriodCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val countColor = if (count > 0) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = buildAnnotatedString {
            append("(")
            withStyle(
                SpanStyle(
                    color = countColor,
                    fontWeight = FontWeight.Bold,
                ),
            ) {
                append(count.toString())
            }
            append(")")
        },
        modifier = modifier.testTag("stats_count_badge_$count"),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.End,
    )
}
