package fumei.faruk.dev.br.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PuffEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun puffDao(): PuffDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE puffs ADD COLUMN grams REAL NOT NULL DEFAULT ${ConsumptionSettings.DEFAULT_GRAMS_PER_SESSION}",
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fumei.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
        }

        @VisibleForTesting
        fun resetInstanceForTests() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }
    }
}
