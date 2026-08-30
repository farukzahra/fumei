package fumei.faruk.dev.br.ui

object DailyProgress {
    const val DEFAULT_GOAL = 8
    const val MIN_GOAL = 1
    const val MAX_GOAL = 99

    fun normalizedGoal(dailyGoal: Int): Int = dailyGoal.coerceIn(MIN_GOAL, MAX_GOAL)

    fun fraction(count: Int, dailyGoal: Int): Float {
        val goal = normalizedGoal(dailyGoal)
        return (count.toFloat() / goal).coerceIn(0f, 1f)
    }

    fun label(count: Int, dailyGoal: Int): String {
        val goal = normalizedGoal(dailyGoal)
        return "$count de $goal"
    }
}
