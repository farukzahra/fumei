package fumei.faruk.dev.br.ui

import fumei.faruk.dev.br.data.ConsumptionSettings
import java.util.Locale

object ConsumptionFormat {
    private val locale = Locale.forLanguageTag("pt-BR")

    fun formatGrams(value: Double): String {
        val normalized = ConsumptionSettings.normalizedGrams(value)
        return formatDecimal(normalized)
    }

    fun formatGramsTotal(value: Double): String = formatDecimal(value.coerceAtLeast(0.0))

    private fun formatDecimal(value: Double): String {
        return String.format(locale, "%.2f", value)
            .trimEnd('0')
            .trimEnd(',')
    }

    fun formatGramsWithUnit(value: Double): String = "${formatGrams(value)} g"

    fun todayTotalLabel(totalGrams: Double): String =
        "${formatGramsTotal(totalGrams)} g fumadas hoje"

    fun parseGramsInput(text: String): Double? {
        val cleaned = text.trim().replace("g", "", ignoreCase = true).trim().replace(',', '.')
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()?.let(ConsumptionSettings::normalizedGrams)
    }
}
