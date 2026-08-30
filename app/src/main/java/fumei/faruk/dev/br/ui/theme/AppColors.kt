package fumei.faruk.dev.br.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // cinza / brasa — ver docs/DESIGN.md
    val Ash950 = Color(0xFF1B1918)
    val Ash850 = Color(0xFF24211F)
    val Ash750 = Color(0xFF322D29)
    val Ember500 = Color(0xFFE8734A)
    val Ember300 = Color(0xFFF2A387)
    val Paper100 = Color(0xFFF5F1EA)
    val Smoke400 = Color(0xFF8C8478)

    // aliases para compatibilidade com telas existentes
    val Background = Ash950
    val Surface = Ash850
    val Primary = Ember500
    val PrimarySoft = Ash750
    val Text = Paper100
    val TextSecondary = Smoke400
    val Border = Ash750
    val Danger = Ember300
    val TimelineDot = Ember500
    val TimelineLine = Smoke400.copy(alpha = 0.35f)

    val DarkBackground = Ash950
    val DarkSurface = Ash850
    val DarkPrimary = Ember500
    val DarkPrimarySoft = Ash750
    val DarkText = Paper100
    val DarkTextSecondary = Smoke400
    val DarkBorder = Ash750
    val DarkDanger = Ember300
    val DarkTimelineDot = Ember500
    val DarkTimelineLine = Smoke400.copy(alpha = 0.35f)
}
