package fumei.faruk.dev.br.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ConsumptionFormatTest {
    @Test
    fun formatGrams_usesBrazilianDecimal() {
        assertEquals("0,3", ConsumptionFormat.formatGrams(0.3))
        assertEquals("1,25", ConsumptionFormat.formatGrams(1.25))
    }

    @Test
    fun todayTotalLabel_includesSuffix() {
        assertEquals("0,9 g fumadas hoje", ConsumptionFormat.todayTotalLabel(0.9))
    }

    @Test
    fun parseGramsInput_acceptsComma() {
        assertEquals(0.5, ConsumptionFormat.parseGramsInput("0,5")!!, 0.001)
    }
}
