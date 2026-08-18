import re

with open('app/src/main/java/com/example/data/AppDao.kt', 'r') as f:
    content = f.read()

new_dao = """    @Query("SELECT * FROM duel_prefs WHERE id = 1 LIMIT 1")
    fun getDuelPrefsFlow(): Flow<DuelPrefsEntity?>

    @Query("SELECT * FROM duel_prefs WHERE id = 1 LIMIT 1")
    suspend fun getDuelPrefs(): DuelPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuelPrefs(duelPrefs: DuelPrefsEntity)

    @Query("DELETE FROM user_profile")"""

content = content.replace('    @Query("DELETE FROM user_profile")', new_dao)

with open('app/src/main/java/com/example/data/AppDao.kt', 'w') as f:
    f.write(content)
print("Patched AppDao.kt")
