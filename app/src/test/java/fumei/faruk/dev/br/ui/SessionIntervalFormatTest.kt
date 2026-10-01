package fumei.faruk.dev.br.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionIntervalFormatTest {
    @Test
    fun formatElapsedMillis_showsHoursAndRemainingMinutes() {
        assertEquals("1h42m", SessionIntervalFormat.formatElapsedMillis(102 * 60_000L))
        assertEquals("2h5m", SessionIntervalFormat.formatElapsedMillis(125 * 60_000L))
    }

    @Test
    fun formatElapsedMillis_showsMinutesBelowOneHour() {
        assertEquals("59m", SessionIntervalFormat.formatElapsedMillis(59 * 60_000L))
    }

    @Test
    fun formatElapsedMillis_omitsZeroRemainingMinutes() {
        assertEquals("1h", SessionIntervalFormat.formatElapsedMillis(60 * 60_000L))
    }

    @Test
    fun formatElapsedMillis_discardsIncompleteMinutes() {
        assertEquals("59m", SessionIntervalFormat.formatElapsedMillis(59 * 60_000L + 59_999L))
    }
}
