package fumei.faruk.dev.br.data

import android.content.Context
import fumei.faruk.dev.br.ui.DailyProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DailyGoalStore {
    fun observeDailyGoal(): Flow<Int>
    suspend fun setDailyGoal(value: Int)
}

class UserPreferencesRepository(
    context: Context,
) : DailyGoalStore {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dailyGoalState = MutableStateFlow(readDailyGoal())

    override fun observeDailyGoal(): Flow<Int> = dailyGoalState.asStateFlow()

    override suspend fun setDailyGoal(value: Int) {
        val normalized = DailyProgress.normalizedGoal(value)
        prefs.edit().putInt(KEY_DAILY_GOAL, normalized).apply()
        dailyGoalState.value = normalized
    }

    private fun readDailyGoal(): Int {
        return DailyProgress.normalizedGoal(
            prefs.getInt(KEY_DAILY_GOAL, DailyProgress.DEFAULT_GOAL),
        )
    }

    companion object {
        private const val PREFS_NAME = "fumei_user_prefs"
        private const val KEY_DAILY_GOAL = "daily_goal"
    }
}
