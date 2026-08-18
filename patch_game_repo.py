import re

with open('app/src/main/java/com/example/data/GameRepository.kt', 'r') as f:
    content = f.read()

new_duel_methods = """
    val duelPrefs: Flow<DuelPrefsEntity?> = dao.getDuelPrefsFlow()

    suspend fun getOrCreateDuelPrefs(): DuelPrefsEntity = withContext(Dispatchers.IO) {
        var prefs = dao.getDuelPrefs()
        if (prefs == null) {
            duelPrefsMutex.withLock {
                prefs = dao.getDuelPrefs()
                if (prefs == null) {
                    prefs = DuelPrefsEntity()
                    dao.insertDuelPrefs(prefs!!)
                }
            }
        }
        prefs!!
    }

    suspend fun updateDuelPrefsAtomic(transform: (DuelPrefsEntity) -> DuelPrefsEntity) = withContext(Dispatchers.IO) {
        duelPrefsMutex.withLock {
            val current = dao.getDuelPrefs() ?: DuelPrefsEntity()
            val updated = transform(current)
            dao.insertDuelPrefs(updated)
        }
    }
"""

content = content.replace('    suspend fun getOrCreateProfile(): UserProfileEntity = withContext(Dispatchers.IO) {', new_duel_methods + '\n    suspend fun getOrCreateProfile(): UserProfileEntity = withContext(Dispatchers.IO) {')

content = content.replace('private val profileMutex = Mutex()', 'private val profileMutex = Mutex()\n        private val duelPrefsMutex = Mutex()')

with open('app/src/main/java/com/example/data/GameRepository.kt', 'w') as f:
    f.write(content)
print("Patched GameRepository.kt")
