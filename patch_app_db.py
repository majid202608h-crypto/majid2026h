import re

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'entities = [UserProfileEntity::class, StageStarsEntity::class, TableStatEntity::class],',
    'entities = [UserProfileEntity::class, StageStarsEntity::class, TableStatEntity::class, DuelPrefsEntity::class],'
)
content = content.replace('version = 5,', 'version = 6,')

migration = """        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `duel_prefs` (`id` INTEGER NOT NULL, `player1Name` TEXT NOT NULL, `player2Name` TEXT NOT NULL, `player1Avatar` TEXT NOT NULL, `player2Avatar` TEXT NOT NULL, `matchDuration` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
"""
content = content.replace('val MIGRATION_4_5', migration + '        val MIGRATION_4_5')

content = content.replace('.addMigrations(MIGRATION_3_4, MIGRATION_4_5)', '.addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)')

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'w') as f:
    f.write(content)
print("Patched AppDatabase.kt")
