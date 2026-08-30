package fumei.faruk.dev.br.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fumei.faruk.dev.br.ui.theme.AppColors
import fumei.faruk.dev.br.ui.theme.AppShapes
import fumei.faruk.dev.br.ui.theme.AppSpacing
import fumei.faruk.dev.br.ui.theme.FumeiThemeExt
import fumei.faruk.dev.br.ui.theme.FumeiType

enum class AppTab {
    Home,
    Stats,
    About,
}

@Composable
fun FumeiApp(
    homeState: TodayUiState,
    statsState: StatsUiState,
    aboutState: AboutUiState,
    onFumeiClick: () -> Unit,
    onEditPuff: (Long, Long) -> Unit,
    onDeletePuff: (Long) -> Unit,
    onStatsPrevious: () -> Unit,
    onStatsNext: () -> Unit,
    onStatsTitleClick: () -> Unit,
    onStatsMonthSelected: (java.time.YearMonth) -> Unit,
    onStatsYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            ) {
                if (selectedTab == AppTab.Home) {
                    RegisterActionButton(onClick = onFumeiClick)
                }
                AppBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.Home -> HomeScreen(
                uiState = homeState,
                onEditPuff = onEditPuff,
                onDeletePuff = onDeletePuff,
                modifier = Modifier.padding(innerPadding),
            )
            AppTab.Stats -> StatsScreen(
                uiState = statsState,
                onPreviousPeriod = onStatsPrevious,
                onNextPeriod = onStatsNext,
                onPeriodTitleClick = onStatsTitleClick,
                onMonthSelected = onStatsMonthSelected,
                onYearSelected = onStatsYearSelected,
                modifier = Modifier.padding(innerPadding),
            )
            AppTab.About -> AboutScreen(
                uiState = aboutState,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
fun RegisterActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.Ash950)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("fumei_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Ember500,
                contentColor = AppColors.Ash950,
            ),
        ) {
            Text("●", color = AppColors.Ash950)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Fumei agora", style = FumeiType.button)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Toque para registrar",
            style = FumeiType.eyebrow.copy(fontSize = 11.sp),
            color = AppColors.Smoke400,
        )
    }
}

@Composable
private fun AppBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = FumeiThemeExt.extendedColors
    Column(modifier = modifier.navigationBarsPadding()) {
        HorizontalDivider(color = extended.border, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.MD, vertical = AppSpacing.SM)
                .testTag("bottom_nav"),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BottomNavItem(
                label = "Hoje",
                icon = Icons.Outlined.Today,
                selected = selectedTab == AppTab.Home,
                onClick = { onTabSelected(AppTab.Home) },
                testTag = "nav_home",
            )
            BottomNavItem(
                label = "Estatísticas",
                icon = Icons.Outlined.BarChart,
                selected = selectedTab == AppTab.Stats,
                onClick = { onTabSelected(AppTab.Stats) },
                testTag = "nav_stats",
            )
            BottomNavItem(
                label = "Mais",
                icon = Icons.Outlined.MoreHoriz,
                selected = selectedTab == AppTab.About,
                onClick = { onTabSelected(AppTab.About) },
                testTag = "nav_about",
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.Small)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.MD, vertical = AppSpacing.SM)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}
