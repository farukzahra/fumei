package fumei.faruk.dev.br.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class FumeiExtendedColors(
    val border: Color,
    val danger: Color,
    val primarySoft: Color,
    val timelineDot: Color,
    val timelineLine: Color,
)

val LocalFumeiExtendedColors = staticCompositionLocalOf {
    FumeiExtendedColors(
        border = AppColors.Border,
        danger = AppColors.Danger,
        primarySoft = AppColors.PrimarySoft,
        timelineDot = AppColors.TimelineDot,
        timelineLine = AppColors.TimelineLine,
    )
}

private val FumeiColorScheme = darkColorScheme(
    background = AppColors.Ash950,
    surface = AppColors.Ash850,
    surfaceVariant = AppColors.Ash750,
    primary = AppColors.Ember500,
    onPrimary = AppColors.Ash950,
    secondary = AppColors.Ember300,
    onBackground = AppColors.Paper100,
    onSurface = AppColors.Paper100,
    onSurfaceVariant = AppColors.Smoke400,
    outline = AppColors.Smoke400,
    error = AppColors.Ember300,
)

private val FumeiExtended = FumeiExtendedColors(
    border = AppColors.Border,
    danger = AppColors.Danger,
    primarySoft = AppColors.PrimarySoft,
    timelineDot = AppColors.TimelineDot,
    timelineLine = AppColors.TimelineLine,
)

@Composable
fun FumeiTheme(
  @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFumeiExtendedColors provides FumeiExtended) {
        MaterialTheme(
            colorScheme = FumeiColorScheme,
            typography = FumeiTypography,
            content = content,
        )
    }
}

object FumeiThemeExt {
    val extendedColors: FumeiExtendedColors
        @Composable
        get() = LocalFumeiExtendedColors.current
}
