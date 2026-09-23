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

interface DefaultGramsStore {
    fun observeDefaultGramsPerSession(): Flow<Double>
    suspend fun setDefaultGramsPerSession(value: Double)
}

interface UserSettingsStore : DailyGoalStore, DefaultGramsStore

class UserPreferencesRepository(
    context: Context,
) : UserSettingsStore {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dailyGoalState = MutableStateFlow(readDailyGoal())
    private val defaultGramsState = MutableStateFlow(readDefaultGrams())

    override fun observeDailyGoal(): Flow<Int> = dailyGoalState.asStateFlow()

    override fun observeDefaultGramsPerSession(): Flow<Double> = defaultGramsState.asStateFlow()

    override suspend fun setDailyGoal(value: Int) {
        val normalized = DailyProgress.normalizedGoal(value)
        prefs.edit().putInt(KEY_DAILY_GOAL, normalized).apply()
        dailyGoalState.value = normalized
    }

    override suspend fun setDefaultGramsPerSession(value: Double) {
        val normalized = ConsumptionSettings.normalizedGrams(value)
        prefs.edit().putFloat(KEY_DEFAULT_GRAMS, normalized.toFloat()).apply()
        defaultGramsState.value = normalized
    }

    private fun readDailyGoal(): Int {
        return DailyProgress.normalizedGoal(
            prefs.getInt(KEY_DAILY_GOAL, DailyProgress.DEFAULT_GOAL),
        )
    }

    private fun readDefaultGrams(): Double {
        if (!prefs.contains(KEY_DEFAULT_GRAMS)) {
            return ConsumptionSettings.DEFAULT_GRAMS_PER_SESSION
        }
        return ConsumptionSettings.normalizedGrams(
            prefs.getFloat(KEY_DEFAULT_GRAMS, ConsumptionSettings.DEFAULT_GRAMS_PER_SESSION.toFloat()).toDouble(),
        )
    }

    companion object {
        private const val PREFS_NAME = "fumei_user_prefs"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_DEFAULT_GRAMS = "default_grams_per_session"
    }
}
