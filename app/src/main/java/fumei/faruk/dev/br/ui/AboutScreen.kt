package fumei.faruk.dev.br.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fumei.faruk.dev.br.data.ReleaseHistoryEntry
import fumei.faruk.dev.br.ui.theme.AppColors
import fumei.faruk.dev.br.ui.theme.FumeiType

data class AboutUiState(
    val appName: String = "Fumei",
    val versionName: String = "",
    val versionCode: Int = 0,
    val entries: List<ReleaseHistoryEntry> = emptyList(),
    val pixKey: String = AboutSupport.PIX_KEY,
    val dailyGoal: Int = DailyProgress.DEFAULT_GOAL,
)

private object AboutSupport {
    const val PIX_KEY = "e6ac082e-a1f8-48b0-b38e-87be149abe52"
}

@Composable
fun AboutScreen(
    uiState: AboutUiState,
    onDailyGoalIncrement: () -> Unit,
    onDailyGoalDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var historyExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DailyGoalCard(
                dailyGoal = uiState.dailyGoal,
                onIncrement = onDailyGoalIncrement,
                onDecrement = onDailyGoalDecrement,
            )
        }
        item {
            SupportSection(pixKey = uiState.pixKey)
        }
        item {
            AboutSummaryCard(uiState = uiState)
        }
        item {
            ReleaseHistorySection(
                entries = uiState.entries,
                expanded = historyExpanded,
                onToggle = { historyExpanded = !historyExpanded },
            )
        }
    }
}

@Composable
private fun DailyGoalCard(
    dailyGoal: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AboutSectionCard(
        modifier = modifier.testTag("daily_goal_card"),
        title = "Meta diária",
    ) {
        Text(
            text = "Quantos vou fumar por dia",
            style = FumeiType.body,
            color = AppColors.Paper100,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("daily_goal_decrease"),
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Diminuir meta")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = dailyGoal.toString(),
                style = FumeiType.displayCount.copy(fontSize = 40.sp),
                color = AppColors.Ember500,
                modifier = Modifier.testTag("daily_goal_value"),
            )
            Spacer(modifier = Modifier.width(20.dp))
            FilledTonalIconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("daily_goal_increase"),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Aumentar meta")
            }
        }
    }
}

@Composable
private fun AboutSummaryCard(
    uiState: AboutUiState,
    modifier: Modifier = Modifier,
) {
    AboutSectionCard(
        modifier = modifier,
        title = "Sobre o app",
    ) {
        Text(
            text = uiState.appName,
            style = FumeiType.displayLabel,
            color = AppColors.Paper100,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Registre cada cigarro, veja a linha do tempo do dia e acompanhe estatísticas. Tudo offline no celular.",
            style = FumeiType.body.copy(fontSize = 14.sp),
            color = AppColors.Smoke400,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = AppColors.Ash750,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Versão ${uiState.versionName}",
                    style = FumeiType.eyebrow,
                    color = AppColors.Smoke400,
                    modifier = Modifier.testTag("about_version_name"),
                )
                Text(
                    text = "Build ${uiState.versionCode}",
                    style = FumeiType.body.copy(fontSize = 13.sp),
                    color = AppColors.Smoke400,
                )
            }
        }
    }
}

@Composable
private fun ReleaseHistorySection(
    entries: List<ReleaseHistoryEntry>,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    val latest = entries.first()
    val olderEntries = entries.drop(1)

    AboutSectionCard(
        modifier = modifier.testTag("about_history_section"),
        title = "Novidades",
    ) {
        AboutHistoryItem(entry = latest, showVersionTag = true)
        if (olderEntries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .testTag("about_history_toggle")
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (expanded) {
                        "Ocultar versões anteriores"
                    } else {
                        "Ver versões anteriores (${olderEntries.size})"
                    },
                    style = FumeiType.body.copy(fontSize = 14.sp),
                    color = AppColors.Ember500,
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.Ember500,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    olderEntries.forEach { entry ->
                        HorizontalDivider(color = AppColors.Ash750)
                        AboutHistoryItem(entry = entry, showVersionTag = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportSection(
    pixKey: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    AboutSectionCard(
        modifier = modifier.testTag("about_pix_card"),
        title = "Apoie o projeto",
    ) {
        Text(
            text = buildAnnotatedString {
                append("Se o ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Fumei")
                }
                append(" te ajudou, você pode mandar um Pix. Só se quiser.")
            },
            style = FumeiType.body.copy(fontSize = 14.sp),
            color = AppColors.Smoke400,
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    clipboard.setText(AnnotatedString(pixKey))
                    Toast.makeText(context, "Chave Pix copiada", Toast.LENGTH_SHORT).show()
                }
                .testTag("about_pix_copy"),
            shape = RoundedCornerShape(12.dp),
            color = AppColors.Ash750,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chave Pix",
                        style = FumeiType.eyebrow.copy(fontSize = 11.sp),
                        color = AppColors.Smoke400,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pixKey,
                        modifier = Modifier.testTag("about_pix_key"),
                        style = FumeiType.timestamp.copy(fontSize = 13.sp),
                        fontFamily = FontFamily.Monospace,
                        color = AppColors.Paper100,
                    )
                }
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(pixKey))
                        Toast.makeText(context, "Chave Pix copiada", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("about_pix_copy_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar chave Pix",
                        modifier = Modifier.size(20.dp),
                        tint = AppColors.Smoke400,
                    )
                }
            }
        }
        Text(
            text = "Toque para copiar a chave.",
            style = FumeiType.body.copy(fontSize = 13.sp),
            color = AppColors.Smoke400,
        )
    }
}

@Composable
private fun AboutHistoryItem(
    entry: ReleaseHistoryEntry,
    showVersionTag: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (showVersionTag) {
            Text(
                text = entry.version,
                style = FumeiType.eyebrow,
                color = AppColors.Ember500,
                modifier = Modifier.testTag("about_history_${entry.version}"),
            )
        }
        Text(
            text = entry.title,
            style = FumeiType.body.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.Paper100,
        )
        Text(
            text = entry.summary,
            style = FumeiType.body.copy(fontSize = 14.sp),
            color = AppColors.Smoke400,
        )
    }
}

@Composable
private fun AboutSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    onHeaderClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Ash850),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onHeaderClick != null) {
                            Modifier.clickable(onClick = onHeaderClick)
                        } else {
                            Modifier
                        },
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = FumeiType.eyebrow,
                    color = AppColors.Smoke400,
                )
                trailing?.invoke()
            }
            content()
        }
    }
}
