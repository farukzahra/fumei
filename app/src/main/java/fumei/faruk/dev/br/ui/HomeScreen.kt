package fumei.faruk.dev.br.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fumei.faruk.dev.br.ui.theme.AppColors
import fumei.faruk.dev.br.ui.theme.AppSpacing
import fumei.faruk.dev.br.ui.theme.FumeiTheme
import fumei.faruk.dev.br.ui.theme.FumeiType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: TodayUiState,
    onEditPuff: (Long, Long) -> Unit,
    onDeletePuff: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingEntry by remember { mutableStateOf<PuffListItem?>(null) }
    var deletingEntry by remember { mutableStateOf<PuffListItem?>(null) }

    editingEntry?.let { entry ->
        EditPuffDialog(
            timestampMillis = entry.timestampMillis,
            onDismiss = { editingEntry = null },
            onConfirm = { newTimestamp ->
                onEditPuff(entry.id, newTimestamp)
                editingEntry = null
            },
        )
    }

    deletingEntry?.let { entry ->
        DeletePuffDialog(
            label = entry.label,
            onDismiss = { deletingEntry = null },
            onConfirm = {
                onDeletePuff(entry.id)
                deletingEntry = null
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = AppSpacing.LG,
            bottom = AppSpacing.MD,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.LG),
    ) {
        item {
            DiaHeader(
                count = uiState.count,
                progressFraction = uiState.progressFraction,
                progressLabel = uiState.progressLabel,
                diaLabel = uiState.dateHeader.ifBlank { uiState.todayLabel },
            )
        }

        item {
            Text(
                text = "REGISTROS DE HOJE",
                style = FumeiType.eyebrow,
                color = AppColors.Smoke400,
            )
        }

        if (uiState.entries.isEmpty()) {
            item {
                Text(
                    text = "Nenhum registro ainda.\nToque em Fumei agora para começar.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_entries_message")
                        .padding(vertical = AppSpacing.XL),
                    style = FumeiType.body,
                    color = AppColors.Smoke400,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("entries_timeline"),
                ) {
                    uiState.entries.forEachIndexed { index, entry ->
                        RegistroRow(
                            entry = entry,
                            isLast = index == uiState.entries.lastIndex,
                            onEditar = { editingEntry = entry },
                            onApagar = { deletingEntry = entry },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaHeader(
    count: Int,
    progressFraction: Float,
    progressLabel: String,
    diaLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_summary"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(176.dp)) {
                val stroke = 10.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(
                    color = AppColors.Ash750,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke),
                    size = arcSize,
                    topLeft = topLeft,
                )
                drawArc(
                    color = AppColors.Ember500,
                    startAngle = -90f,
                    sweepAngle = 360f * progressFraction.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                    size = arcSize,
                    topLeft = topLeft,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = count.toString(),
                    style = FumeiType.displayCount,
                    color = AppColors.Paper100,
                    modifier = Modifier.testTag("daily_count"),
                )
                Text(
                    text = "HOJE",
                    style = FumeiType.eyebrow,
                    color = AppColors.Smoke400,
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        if (progressLabel.isNotBlank()) {
            Text(
                text = progressLabel,
                style = FumeiType.body,
                color = AppColors.Ember500,
                modifier = Modifier.testTag("daily_progress_label"),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (diaLabel.isNotBlank()) {
            Text(
                text = diaLabel,
                style = FumeiType.displayLabel,
                color = AppColors.Smoke400,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RegistroRow(
    entry: PuffListItem,
    isLast: Boolean,
    onEditar: () -> Unit,
    onApagar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_entry"),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Ember500),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(AppColors.Smoke400.copy(alpha = 0.35f)),
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.timeLabel,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEditar),
                style = FumeiType.timestamp,
                color = AppColors.Paper100,
            )
            IconButton(
                onClick = onEditar,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar registro",
                    tint = AppColors.Smoke400,
                )
            }
            IconButton(
                onClick = onApagar,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir registro",
                    tint = AppColors.Ember300,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FumeiTheme {
        HomeScreen(
            uiState = TodayUiState(
                count = 3,
                dateHeader = "DOMINGO · 30 AGO",
                progressLabel = "3 de 8",
                progressFraction = 0.375f,
                entries = listOf(
                    PuffListItem(1, "29/08/2026 14:49", "14:49", "Hoje", 1_756_489_740_000L),
                    PuffListItem(2, "29/08/2026 12:10", "12:10", "Hoje", 1_756_480_200_000L),
                    PuffListItem(3, "29/08/2026 11:02", "11:02", "Hoje", 1_756_476_120_000L),
                ),
            ),
            onEditPuff = { _, _ -> },
            onDeletePuff = {},
        )
    }
}
