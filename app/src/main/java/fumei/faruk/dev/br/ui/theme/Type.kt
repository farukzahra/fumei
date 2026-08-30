package fumei.faruk.dev.br.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val FrauncesFamily = FontFamily.Serif
private val ManropeFamily = FontFamily.SansSerif
private val MonoFamily = FontFamily.Monospace

object FumeiType {
    val displayCount = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 56.sp,
        letterSpacing = (-1).sp,
    )
    val displayLabel = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Light,
        fontSize = 18.sp,
    )
    val eyebrow = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
    )
    val body = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    )
    val button = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.3.sp,
    )
    val timestamp = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.2.sp,
    )
}

val FumeiTypography = Typography(
    displayLarge = FumeiType.displayCount,
    titleLarge = FumeiType.displayLabel,
    titleMedium = FumeiType.body.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = FumeiType.body,
    bodyMedium = FumeiType.body,
    bodySmall = FumeiType.body.copy(fontSize = 13.sp),
    labelLarge = FumeiType.eyebrow,
)
