package fumei.faruk.dev.br.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyProgressTest {
    @Test
    fun fraction_usesConfiguredGoal() {
        assertEquals(0.375f, DailyProgress.fraction(count = 3, dailyGoal = 8), 0.001f)
    }

    @Test
    fun fraction_capsAtOneWhenOverGoal() {
        assertEquals(1f, DailyProgress.fraction(count = 12, dailyGoal = 8), 0.001f)
    }

    @Test
    fun fraction_treatsInvalidGoalAsOne() {
        assertEquals(1f, DailyProgress.fraction(count = 2, dailyGoal = 0), 0.001f)
    }

    @Test
    fun label_showsCountAndGoal() {
        assertEquals("3 de 8", DailyProgress.label(count = 3, dailyGoal = 8))
    }
}
