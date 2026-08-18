package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameRepository(private val dao: AppDao) {

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfileFlow()
    val stageStars: Flow<Map<Int, Int>> = dao.getStageStarsFlow().map { list ->
        list.associate { it.stageId to it.stars }
    }
    val tableStats: Flow<List<TableStatEntity>> = dao.getTableStatsFlow()


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

    suspend fun getOrCreateProfile(): UserProfileEntity = withContext(Dispatchers.IO) {
        var profile = dao.getUserProfile()
        if (profile == null) {
            profileMutex.withLock {
                profile = dao.getUserProfile()
                if (profile == null) {
                    profile = UserProfileEntity()
                    dao.insertUserProfile(profile!!)
                }
            }
        }
        profile!!
    }

    suspend fun updateProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        dao.insertUserProfile(profile)
    }

    suspend fun updateProfileAtomic(transform: (UserProfileEntity) -> UserProfileEntity) = withContext(Dispatchers.IO) {
        profileMutex.withLock {
            val current = dao.getUserProfile() ?: UserProfileEntity()
            val updated = transform(current)
            dao.insertUserProfile(updated)
        }
    }

    /**
     * Atomically overwrite ALL local progress with a snapshot restored from Bazaar cloud storage.
     * Used only when the local profile is "fresh" (e.g. right after a re-install / device change).
     */
    suspend fun restoreFromBackup(
        profile: UserProfileEntity,
        stars: Map<Int, Int>,
        tables: List<TableStatEntity>
    ) = withContext(Dispatchers.IO) {
        profileMutex.withLock {
            dao.insertUserProfile(profile.copy(id = 1))
            dao.clearStageStars()
            stars.forEach { (stageId, s) -> dao.insertStageStars(StageStarsEntity(stageId, s)) }
            dao.clearTableStats()
            tables.forEach { dao.insertTableStat(it) }
        }
    }

    suspend fun saveStageResult(stageId: Int, stars: Int, score: Int, correct: Int, wrong: Int) = withContext(Dispatchers.IO) {
        dao.insertStageStars(StageStarsEntity(stageId, stars))
        updateProfileAtomic { profile ->
            val newCorrect = profile.totalCorrect + correct
            val newWrong = profile.totalWrong + wrong
            
            var newUnlocked = profile.unlockedStage
            if (stars > 0) {
                if (stageId >= profile.unlockedStage && stageId < GameConfig.STAGES.size - 1) {
                    newUnlocked = stageId + 1
                }
            }
            
            val isBoss = GameConfig.STAGES.getOrNull(stageId)?.boss ?: false
            val newBossDefeated = if (stars > 0 && isBoss) profile.bossesDefeated + 1 else profile.bossesDefeated
            val newPerfect = if (stars > 0 && wrong == 0) profile.perfectStages + 1 else profile.perfectStages
            val newCompleted = maxOf(profile.stagesCompleted, if (stars > 0) stageId + 1 else profile.stagesCompleted)

            profile.copy(
                unlockedStage = newUnlocked,
                totalCorrect = newCorrect,
                totalWrong = newWrong,
                bossesDefeated = newBossDefeated,
                perfectStages = newPerfect,
                stagesCompleted = newCompleted
            )
        }
    }

    suspend fun incrementTableStat(number: Int, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        val stat = dao.getTableStat(number) ?: TableStatEntity(number)
        val updated = if (isCorrect) {
            stat.copy(correctCount = stat.correctCount + 1)
        } else {
            stat.copy(wrongCount = stat.wrongCount + 1)
        }
        dao.insertTableStat(updated)
    }

    suspend fun recordCoinsEarned(amount: Int) = updateProfileAtomic { profile ->
        profile.copy(
            coins = profile.coins + amount,
            totalCoinsEarned = profile.totalCoinsEarned + amount
        )
    }

    suspend fun recordPurchase(itemPrice: Int) = updateProfileAtomic { profile ->
        profile.copy(
            coins = maxOf(0, profile.coins - itemPrice),
            purchases = profile.purchases + 1
        )
    }

    suspend fun buyAndSelectAvatar(avatarId: String) = updateProfileAtomic { profile ->
        val owned = parseStringList(profile.ownedAvatarsJson).toMutableSet()
        val price = GameConfig.AVATARS.find { it.id == avatarId }?.price ?: 0
        
        if (avatarId !in owned) {
            if (profile.coins >= price) {
                owned.add(avatarId)
                profile.copy(
                    coins = profile.coins - price,
                    ownedAvatarsJson = formatStringList(owned.toList()),
                    activeAvatar = avatarId,
                    purchases = profile.purchases + 1
                )
            } else {
                profile
            }
        } else {
            profile.copy(activeAvatar = avatarId)
        }
    }

    suspend fun buyAndSelectTheme(themeId: String) = updateProfileAtomic { profile ->
        val owned = parseStringList(profile.ownedThemesJson).toMutableSet()
        val price = GameConfig.THEMES.find { it.id == themeId }?.price ?: 0
        
        if (themeId !in owned) {
            if (profile.coins >= price) {
                owned.add(themeId)
                profile.copy(
                    coins = profile.coins - price,
                    ownedThemesJson = formatStringList(owned.toList()),
                    activeTheme = themeId,
                    purchases = profile.purchases + 1
                )
            } else {
                profile
            }
        } else {
            profile.copy(activeTheme = themeId)
        }
    }

    suspend fun buyAndSelectEffect(effectId: String) = updateProfileAtomic { profile ->
        val owned = parseStringList(profile.ownedEffectsJson).toMutableSet()
        val price = GameConfig.EFFECTS.find { it.id == effectId }?.price ?: 0
        
        if (effectId !in owned) {
            if (profile.coins >= price) {
                owned.add(effectId)
                profile.copy(
                    coins = profile.coins - price,
                    ownedEffectsJson = formatStringList(owned.toList()),
                    activeEffect = effectId,
                    purchases = profile.purchases + 1
                )
            } else {
                profile
            }
        } else {
            profile.copy(activeEffect = effectId)
        }
    }

    suspend fun updateMaxStreak(streak: Int) = updateProfileAtomic { profile ->
        if (streak > profile.maxStreak) {
            profile.copy(maxStreak = streak)
        } else {
            profile
        }
    }

    suspend fun updateBestSpeed(score: Int) = updateProfileAtomic { profile ->
        if (score > profile.bestSpeed) {
            profile.copy(bestSpeed = score)
        } else {
            profile
        }
    }

    suspend fun resetGameData() = withContext(Dispatchers.IO) {
        profileMutex.withLock {
            dao.clearProfile()
            dao.clearStageStars()
            dao.clearTableStats()
            dao.insertUserProfile(UserProfileEntity())
        }
    }

    companion object {
        private val profileMutex = Mutex()
        private val duelPrefsMutex = Mutex()
        fun parseStringList(json: String): List<String> {
            if (json.isEmpty() || json == "[]") return emptyList()
            return json.replace("[", "").replace("]", "").replace("\"", "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        fun formatStringList(list: List<String>): String {
            return "[" + list.joinToString(",") { "\"$it\"" } + "]"
        }
    }
}
