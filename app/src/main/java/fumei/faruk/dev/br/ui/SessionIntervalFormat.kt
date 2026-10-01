package fumei.faruk.dev.br.ui

object SessionIntervalFormat {
    fun formatElapsedMillis(elapsedMillis: Long): String {
        val totalMinutes = elapsedMillis.coerceAtLeast(0L) / 60_000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours == 0L -> "${minutes}m"
            minutes == 0L -> "${hours}h"
            else -> "${hours}h${minutes}m"
        }
    }
}
