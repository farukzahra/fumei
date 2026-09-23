package fumei.faruk.dev.br.data

object ConsumptionSettings {
    const val DEFAULT_GRAMS_PER_SESSION = 0.3
    const val MIN_GRAMS_PER_SESSION = 0.05
    const val MAX_GRAMS_PER_SESSION = 5.0
    const val GRAMS_STEP = 0.05

    fun normalizedGrams(value: Double): Double =
        value.coerceIn(MIN_GRAMS_PER_SESSION, MAX_GRAMS_PER_SESSION)
}
