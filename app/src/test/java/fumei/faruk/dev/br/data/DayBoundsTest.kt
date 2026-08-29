package fumei.faruk.dev.br.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DayBoundsTest {
    @Test
    fun dayBounds_coversFullLocalDay() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val date = LocalDate.of(2026, 8, 28)
        val bounds = dayBounds(date, zone)

        assertEquals(
            date.atStartOfDay(zone).toInstant().toEpochMilli(),
            bounds.startMillis,
        )
        assertEquals(
            date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),
            bounds.endMillis,
        )
        assertEquals(86_400_000L, bounds.endMillis - bounds.startMillis)
    }
}
