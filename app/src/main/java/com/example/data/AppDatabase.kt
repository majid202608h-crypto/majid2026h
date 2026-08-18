package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserProfileEntity::class, StageStarsEntity::class, TableStatEntity::class, DuelPrefsEntity::class],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN newField INTEGER NOT NULL DEFAULT 0")
            }
        }

                val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `duel_prefs` (`id` INTEGER NOT NULL, `player1Name` TEXT NOT NULL, `player2Name` TEXT NOT NULL, `player1Avatar` TEXT NOT NULL, `player2Avatar` TEXT NOT NULL, `matchDuration` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1) Create new table user_profile_new without newField
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_profile_new` (
                        `id` INTEGER NOT NULL, 
                        `coins` INTEGER NOT NULL, 
                        `gems` INTEGER NOT NULL, 
                        `unlockedStage` INTEGER NOT NULL, 
                        `ownedAvatarsJson` TEXT NOT NULL, 
                        `ownedThemesJson` TEXT NOT NULL, 
                        `ownedEffectsJson` TEXT NOT NULL, 
                        `activeAvatar` TEXT NOT NULL, 
                        `activeTheme` TEXT NOT NULL, 
                        `activeEffect` TEXT NOT NULL, 
                        `unlockedAchsJson` TEXT NOT NULL, 
                        `totalCorrect` INTEGER NOT NULL, 
                        `totalWrong` INTEGER NOT NULL, 
                        `maxStreak` INTEGER NOT NULL, 
                        `stagesCompleted` INTEGER NOT NULL, 
                        `bossesDefeated` INTEGER NOT NULL, 
                        `purchases` INTEGER NOT NULL, 
                        `bestSpeed` INTEGER NOT NULL, 
                        `totalCoinsEarned` INTEGER NOT NULL, 
                        `perfectStages` INTEGER NOT NULL, 
                        `dailyStreak` INTEGER NOT NULL, 
                        `lastDailyClaimDate` TEXT, 
                        `level` INTEGER NOT NULL, 
                        `xp` INTEGER NOT NULL, 
                        `classCode` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `role` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                // 2) Copy all data from user_profile to user_profile_new
                db.execSQL(
                    """
                    INSERT INTO `user_profile_new` (
                        `id`, `coins`, `gems`, `unlockedStage`, `ownedAvatarsJson`, `ownedThemesJson`, 
                        `ownedEffectsJson`, `activeAvatar`, `activeTheme`, `activeEffect`, `unlockedAchsJson`, 
                        `totalCorrect`, `totalWrong`, `maxStreak`, `stagesCompleted`, `bossesDefeated`, 
                        `purchases`, `bestSpeed`, `totalCoinsEarned`, `perfectStages`, `dailyStreak`, 
                        `lastDailyClaimDate`, `level`, `xp`, `classCode`, `name`, `role`
                    ) SELECT 
                        `id`, `coins`, `gems`, `unlockedStage`, `ownedAvatarsJson`, `ownedThemesJson`, 
                        `ownedEffectsJson`, `activeAvatar`, `activeTheme`, `activeEffect`, `unlockedAchsJson`, 
                        `totalCorrect`, `totalWrong`, `maxStreak`, `stagesCompleted`, `bossesDefeated`, 
                        `purchases`, `bestSpeed`, `totalCoinsEarned`, `perfectStages`, `dailyStreak`, 
                        `lastDailyClaimDate`, `level`, `xp`, `classCode`, `name`, `role`
                    FROM `user_profile`
                    """.trimIndent()
                )

                // 3) Drop old table
                db.execSQL("DROP TABLE `user_profile`")

                // 4) Rename new table to user_profile
                db.execSQL("ALTER TABLE `user_profile_new` RENAME TO `user_profile`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "multiplication_hero_db"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigrationFrom(true, 1, 2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
