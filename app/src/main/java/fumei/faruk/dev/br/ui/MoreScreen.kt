package fumei.faruk.dev.br.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fumei.faruk.dev.br.ui.theme.AppColors
import fumei.faruk.dev.br.ui.theme.FumeiType

private enum class MoreRoute {
    Hub,
    Settings,
    About,
}

@Composable
fun MoreScreen(
    aboutState: AboutUiState,
    dailyGoal: Int,
    defaultGramsPerSession: Double,
    onDailyGoalIncrement: () -> Unit,
    onDailyGoalDecrement: () -> Unit,
    onDefaultGramsIncrement: () -> Unit,
    onDefaultGramsDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var route by rememberSaveable { mutableStateOf(MoreRoute.Hub) }

    when (route) {
        MoreRoute.Hub -> MoreHubScreen(
            modifier = modifier,
            onOpenSettings = { route = MoreRoute.Settings },
            onOpenAbout = { route = MoreRoute.About },
        )
        MoreRoute.Settings -> SettingsScreen(
            dailyGoal = dailyGoal,
            defaultGramsPerSession = defaultGramsPerSession,
            onDailyGoalIncrement = onDailyGoalIncrement,
            onDailyGoalDecrement = onDailyGoalDecrement,
            onDefaultGramsIncrement = onDefaultGramsIncrement,
            onDefaultGramsDecrement = onDefaultGramsDecrement,
            onBack = { route = MoreRoute.Hub },
            modifier = modifier,
        )
        MoreRoute.About -> AboutScreen(
            uiState = aboutState,
            onBack = { route = MoreRoute.Hub },
            modifier = modifier,
        )
    }
}

@Composable
private fun MoreHubScreen(
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_hub_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Mais",
                style = FumeiType.displayLabel,
                color = AppColors.Paper100,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configurações do consumo e informações do app.",
                style = FumeiType.body.copy(fontSize = 14.sp),
                color = AppColors.Smoke400,
            )
        }
        item {
            MoreHubEntry(
                title = "Configurações",
                subtitle = "Meta diária e gramas por sessão",
                icon = Icons.Outlined.Settings,
                testTag = "more_nav_settings",
                onClick = onOpenSettings,
            )
        }
        item {
            MoreHubEntry(
                title = "Sobre",
                subtitle = "Versão, novidades e Pix",
                icon = Icons.Outlined.Info,
                testTag = "more_nav_about",
                onClick = onOpenAbout,
            )
        }
    }
}

@Composable
private fun MoreHubEntry(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AboutSectionCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        title = title,
        trailing = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Ember500,
                modifier = Modifier.size(22.dp),
            )
        },
    ) {
        Text(
            text = subtitle,
            style = FumeiType.body.copy(fontSize = 14.sp),
            color = AppColors.Smoke400,
        )
    }
}

@Composable
fun SettingsScreen(
    dailyGoal: Int,
    defaultGramsPerSession: Double,
    onDailyGoalIncrement: () -> Unit,
    onDailyGoalDecrement: () -> Unit,
    onDefaultGramsIncrement: () -> Unit,
    onDefaultGramsDecrement: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = AppColors.Paper100,
                    )
                }
                Text(
                    text = "Configurações",
                    style = FumeiType.displayLabel,
                    color = AppColors.Paper100,
                )
            }
        }
        item {
            AboutSectionCard(
                modifier = Modifier.testTag("daily_goal_card"),
                title = "Meta diária",
            ) {
                Text(
                    text = "Quantas sessões por dia",
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
                        onClick = onDailyGoalDecrement,
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
                        onClick = onDailyGoalIncrement,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("daily_goal_increase"),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar meta")
                    }
                }
            }
        }
        item {
            AboutSectionCard(
                modifier = Modifier.testTag("default_grams_card"),
                title = "Gramas por sessão",
            ) {
                Text(
                    text = "Usado ao tocar em Fumei agora. Você pode ajustar cada registro depois.",
                    style = FumeiType.body.copy(fontSize = 14.sp),
                    color = AppColors.Smoke400,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = onDefaultGramsDecrement,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("default_grams_decrease"),
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Diminuir gramas")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = ConsumptionFormat.formatGramsWithUnit(defaultGramsPerSession),
                        style = FumeiType.displayCount.copy(fontSize = 28.sp),
                        color = AppColors.Ember500,
                        modifier = Modifier.testTag("default_grams_value"),
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    FilledTonalIconButton(
                        onClick = onDefaultGramsIncrement,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("default_grams_increase"),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar gramas")
                    }
                }
            }
        }
    }
}
