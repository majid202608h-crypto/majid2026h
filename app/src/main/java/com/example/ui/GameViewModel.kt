package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class GameScreen {
    Login,
    Onboarding,
    MainMenu,
    WorldMap,
    Gameplay,
    StageResult,
    SpeedChallenge,
    Shop,
    Achievements,
    ProgressReport,
    LearnTables,
    LuckyWheel,
    Subscription,
    ReverseChallenge,
    DuelSetup,
    DuelGame,
    DuelResult
}

sealed class GameSoundEvent {
    data class PlayTone(val freq: Int, val duration: Int) : GameSoundEvent()
    object CorrectSound : GameSoundEvent()
    object WrongSound : GameSoundEvent()
    object StarSound : GameSoundEvent()
    object CoinSound : GameSoundEvent()
    data class SpeakText(val text: String) : GameSoundEvent()
    data class PlayLearnClip(val row: Int, val factor: Int) : GameSoundEvent()

    // New professional sounds
    object PlayCorrectRand : GameSoundEvent()
    object PlayWrongRand : GameSoundEvent()
    object PlayFinishRand : GameSoundEvent()
    object PlayBossStartRand : GameSoundEvent()
    object PlayBossWinRand : GameSoundEvent()
    object PlayBossLoseRand : GameSoundEvent()
    object PlayWheelStart : GameSoundEvent()
    object PlayWheelSpin : GameSoundEvent()
    object PlayWheelWin : GameSoundEvent()
    object PlayWheelFinish : GameSoundEvent()
    
    // Duel Sounds
    object PlayDuelCorrect : GameSoundEvent()
    object PlayDuelWrong : GameSoundEvent()
    object PlayDuelCombo : GameSoundEvent()
    object PlayDuelTick : GameSoundEvent()
    object PlayDuelStart : GameSoundEvent()
    object PlayDuelFinish : GameSoundEvent()
    object PlayDuelVictory : GameSoundEvent()
    object PlayDuelDraw : GameSoundEvent()
    object PlayDuelClick : GameSoundEvent()

}

enum class AnswerResult {
    Correct,
    Wrong
}

data class StageGameplayState(
    val stageId: Int = 0,
    val currentQuestionIndex: Int = 0,
    val correctAnswersCount: Int = 0,
    val wrongAnswersCount: Int = 0,
    val score: Int = 0,
    val lives: Int = 3,
    val streak: Int = 0,
    val timerProgress: Float = 1.0f,
    val timerSecondsLeft: Int = 20,
    val bossHp: Int = 5,
    val isLocked: Boolean = false,
    val options: List<Int> = emptyList(),
    val activeQuestion: Pair<Int, Int> = Pair(3, 4),
    val isBoss: Boolean = false,
    val selectResult: AnswerResult? = null,
    val selectedOptionIndex: Int? = null,
    val charMessage: String = "آماده‌ای؟ بریم! 🚀"
)

data class SpeedGameplayState(
    val secondsLeft: Int = 60,
    val score: Int = 0,
    val streak: Int = 0,
    val currentQuestion: Pair<Int, Int> = Pair(5, 6),
    val options: List<Int> = emptyList(),
    val isLocked: Boolean = false,
    val timerProgress: Float = 1.0f,
    val charMessage: String = "سریع‌تر! ⚡",
    val lastSelectedOption: Int? = null
)

data class ReverseGameplayState(
    val secondsLeft: Int = 90,
    val score: Int = 0,
    val streak: Int = 0,
    val timerProgress: Float = 1.0f,
    val charMessage: String = "جواب رو میبینی؟ سوالش رو پیدا کن! 🔍",
    val isLocked: Boolean = false,
    val lastSelectedOptionIndex: Int? = null,
    val answerToShow: Int = 12,
    val options: List<Pair<Int, Int>> = emptyList(),
    val correctOption: Pair<Int, Int> = Pair(3, 4),
    val correctCount: Int = 0,
    val wrongCount: Int = 0
)



data class DuelPlayerState(
    val name: String = "",
    val score: Int = 0,
    val streak: Int = 0,
    val a: Int = 1,
    val b: Int = 1,
    val answered: Boolean = false,
    val feedbackText: String = "",
    val feedbackIsCorrect: Boolean? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val maxCombo: Int = 0,
    val fastestTimeMs: Long = Long.MAX_VALUE,
    val totalTimeMs: Long = 0L,
    val questionStartTimeMs: Long = 0L
)

data class DuelGameState(
    val player1: DuelPlayerState = DuelPlayerState(),
    val player2: DuelPlayerState = DuelPlayerState(),
    val secondsLeft: Int = 60,
    val totalSeconds: Int = 60,
    val timerProgress: Float = 1.0f,
    val phase: DuelPhase = DuelPhase.Setup
)

enum class DuelPhase { Setup, Playing, Result }

data class DuelResultState(
    val winner: Int? = null,
    val winnerName: String = "",
    val loserName: String = "",
    val score1: Int = 0,
    val score2: Int = 0,
    val congratsMessage: String = ""
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private var rewardRepository: com.example.ui.luckywheel.RewardRepository? = null

    fun attachWheelCloudSync(repo: com.example.ui.luckywheel.RewardRepository) {
        this.rewardRepository = repo
    }

    private var currentQuestions: List<Pair<Int, Int>> = emptyList()

    private val _currentScreen = MutableStateFlow(GameScreen.MainMenu)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Database states
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stageStars: StateFlow<Map<Int, Int>> = repository.stageStars
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val tableStats: StateFlow<List<TableStatEntity>> = repository.tableStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duelPrefs: StateFlow<com.example.data.DuelPrefsEntity?> = repository.duelPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateDuelPrefs(player1Name: String? = null, player2Name: String? = null, player1Avatar: String? = null, player2Avatar: String? = null, matchDuration: Int? = null) {
        viewModelScope.launch {
            repository.updateDuelPrefsAtomic { prefs ->
                prefs.copy(
                    player1Name = player1Name ?: prefs.player1Name,
                    player2Name = player2Name ?: prefs.player2Name,
                    player1Avatar = player1Avatar ?: prefs.player1Avatar,
                    player2Avatar = player2Avatar ?: prefs.player2Avatar,
                    matchDuration = matchDuration ?: prefs.matchDuration
                )
            }
        }
    }


    // Game states
    private val _stageState = MutableStateFlow(StageGameplayState())
    val stageState: StateFlow<StageGameplayState> = _stageState.asStateFlow()

    private val _speedState = MutableStateFlow(SpeedGameplayState())
    val speedState: StateFlow<SpeedGameplayState> = _speedState.asStateFlow()

    private val _reverseState = MutableStateFlow(ReverseGameplayState())
    val reverseState: StateFlow<ReverseGameplayState> = _reverseState.asStateFlow()
    private var reverseTimerJob: Job? = null

    private val _duelState = MutableStateFlow(DuelGameState())
    val duelState: StateFlow<DuelGameState> = _duelState.asStateFlow()

    private val _duelResult = MutableStateFlow(DuelResultState())
    val duelResult: StateFlow<DuelResultState> = _duelResult.asStateFlow()

    private var duelTimerJob: Job? = null


    // Overlay triggers
    private val _activeAchievementAward = MutableStateFlow<GameAchievement?>(null)
    val activeAchievementAward: StateFlow<GameAchievement?> = _activeAchievementAward.asStateFlow()

    private val achievementQueue = mutableListOf<GameAchievement>()

    private fun showNextAchievementFromQueue() {
        if (achievementQueue.isNotEmpty()) {
            val nextAch = achievementQueue.removeAt(0)
            _activeAchievementAward.value = nextAch
            viewModelScope.launch {
                _soundEvents.emit(GameSoundEvent.StarSound)
            }
        } else {
            _activeAchievementAward.value = null
        }
    }

    private val _activeDailyAward = MutableStateFlow<Int?>(null) // award coins
    val activeDailyAward: StateFlow<Int?> = _activeDailyAward.asStateFlow()

    private val _levelUpTrigger = MutableStateFlow<Int?>(null)
    val levelUpTrigger: StateFlow<Int?> = _levelUpTrigger.asStateFlow()

    fun dismissLevelUp() {
        _levelUpTrigger.value = null
    }

    private val _isDarkMode = MutableStateFlow(true) // Default is Dark Mode (true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    private val _soundEvents = MutableSharedFlow<GameSoundEvent>(extraBufferCapacity = 10)
    val soundEvents: SharedFlow<GameSoundEvent> = _soundEvents.asSharedFlow()

    private val _cloudBackupRequests = MutableSharedFlow<Unit>(replay = 0)
    val cloudBackupRequests: SharedFlow<Unit> = _cloudBackupRequests.asSharedFlow()

    // Daily reward banner label
    private val _dailyRewardBannerText = MutableStateFlow("جایزه روزانه! 🎁")
    val dailyRewardBannerText: StateFlow<String> = _dailyRewardBannerText.asStateFlow()

    private val _dailyRewardClickable = MutableStateFlow(true)
    val dailyRewardClickable: StateFlow<Boolean> = _dailyRewardClickable.asStateFlow()

    // Score / Streak calculations
    private var timerJob: Job? = null
    private var speedTimerJob: Job? = null

    init {
        viewModelScope.launch {
            // Seed profile
            val p = repository.getOrCreateProfile()
            if (p.name.isEmpty()) {
                _currentScreen.value = GameScreen.Login
            } else {
                _currentScreen.value = GameScreen.MainMenu
            }
            checkDailyStatus()
        }
    }

    fun finishAuth(name: String, role: String, classCode: String) {
        viewModelScope.launch {
            repository.updateProfileAtomic { profile ->
                profile.copy(
                    name = name,
                    role = role,
                    classCode = classCode
                )
            }
            _currentScreen.value = GameScreen.MainMenu
        }
    }

    // Bazaar Login

    /** Called after a successful "Login with Bazaar". Checks for cloud backup, restores it if applicable, and handles smart routing. */
    fun onBazaarAuthenticated(context: android.content.Context, cloudJson: String?) {
        viewModelScope.launch {
            if (!cloudJson.isNullOrBlank()) {
                applyCloudRestore(context, cloudJson, requireFresh = true)
            }
            val local = repository.getOrCreateProfile()
            if (local.name.isBlank() || local.name == "قهرمان ضرب") {
                _currentScreen.value = GameScreen.Onboarding
            } else {
                _currentScreen.value = GameScreen.MainMenu
            }
        }
    }

    /** Submit name from onboarding screen, save it, and request a cloud backup if logged in with Bazaar. */
    fun submitOnboardingName(name: String) {
        viewModelScope.launch {
            val cleanName = name.trim()
            val finalName = if (cleanName.isEmpty()) "قهرمان ضرب" else cleanName.take(20)
            repository.updateProfileAtomic { profile ->
                profile.copy(name = finalName)
            }
            _cloudBackupRequests.emit(Unit)
            _currentScreen.value = GameScreen.MainMenu
        }
    }

    // Cloud backup / restore (Bazaar Storage)

    /** Serialize the full local progress to a compact JSON string (well under the 10KB limit). */
    fun buildCloudBackupJson(context: android.content.Context): String {
        val p = userProfile.value ?: return ""
        return try {
            val root = org.json.JSONObject()
            root.put("v", 1)
            root.put("savedAt", System.currentTimeMillis())
            root.put("profile", profileToJson(p))
            val starsObj = org.json.JSONObject()
            stageStars.value.forEach { (stageId, s) -> starsObj.put(stageId.toString(), s) }
            root.put("stars", starsObj)
            val tablesObj = org.json.JSONObject()
            tableStats.value.forEach { t ->
                val tj = org.json.JSONObject()
                tj.put("c", t.correctCount)
                tj.put("w", t.wrongCount)
                tablesObj.put(t.number.toString(), tj)
            }
            root.put("tables", tablesObj)
rewardRepository?.let { repo ->
                root.put("wheel", repo.exportForCloud())
            }
            
            // Append License info to Cloud backup
            val licObj = org.json.JSONObject()
            licObj.put("firstLaunchTime", com.example.data.SubscriptionManager.getFirstLaunchTime(context))
            licObj.put("permanentLicensed", com.example.data.SubscriptionManager.isPermanentLicensed(context))
            licObj.put("expireTime", (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L)
            root.put("license", licObj)
            
            root.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun profileToJson(p: UserProfileEntity): org.json.JSONObject {
        val o = org.json.JSONObject()
        o.put("coins", p.coins)
        o.put("gems", p.gems)
        o.put("unlockedStage", p.unlockedStage)
        o.put("ownedAvatarsJson", p.ownedAvatarsJson)
        o.put("ownedThemesJson", p.ownedThemesJson)
        o.put("ownedEffectsJson", p.ownedEffectsJson)
        o.put("activeAvatar", p.activeAvatar)
        o.put("activeTheme", p.activeTheme)
        o.put("activeEffect", p.activeEffect)
        o.put("unlockedAchsJson", p.unlockedAchsJson)
        o.put("totalCorrect", p.totalCorrect)
        o.put("totalWrong", p.totalWrong)
        o.put("maxStreak", p.maxStreak)
        o.put("stagesCompleted", p.stagesCompleted)
        o.put("bossesDefeated", p.bossesDefeated)
        o.put("purchases", p.purchases)
        o.put("bestSpeed", p.bestSpeed)
        o.put("totalCoinsEarned", p.totalCoinsEarned)
        o.put("perfectStages", p.perfectStages)
        o.put("dailyStreak", p.dailyStreak)
        o.put("lastDailyClaimDate", p.lastDailyClaimDate ?: org.json.JSONObject.NULL)
        o.put("level", p.level)
        o.put("xp", p.xp)
        o.put("classCode", p.classCode)
        o.put("name", p.name)
        o.put("role", p.role)
        return o
    }

    /** Auto-restore: applies cloud data ONLY if local progress is still fresh (used right after login). */
    fun maybeRestoreFromCloud(context: android.content.Context, json: String) {
        viewModelScope.launch { applyCloudRestore(context, json, requireFresh = true) }
    }

    /** Manual restore: applies cloud data even if local progress exists (used by the Sync button). */
    fun forceRestoreFromCloud(context: android.content.Context, json: String) {
        viewModelScope.launch { applyCloudRestore(context, json, requireFresh = false) }
    }

    private suspend fun applyCloudRestore(context: android.content.Context, json: String, requireFresh: Boolean) {
        if (json.isBlank()) return
        try {
            val root = org.json.JSONObject(json)
root.optJSONObject("wheel")?.let { wheelObj ->
                val cloudLastSpinWall = wheelObj.optLong("lastSpinWall", 0L)
                val cloudSpinCount = wheelObj.optInt("spinCount", 0)
                rewardRepository?.reconcileFromCloud(cloudLastSpinWall, cloudSpinCount)
            }
            
            root.optJSONObject("license")?.let { licObj ->
                val cloudFirstLaunch = licObj.optLong("firstLaunchTime", 0L)
                val cloudPermanent = licObj.optBoolean("permanentLicensed", false)
                val cloudExpiry = licObj.optLong("expireTime", 0L)
                
                // Reconcile
                if (cloudPermanent) {
                    com.example.data.SubscriptionManager.setPermanentLicensed(context, true)
                }
                
                val currentFirstLaunch = com.example.data.SubscriptionManager.getFirstLaunchTime(context)
                // If cloud has an older (smaller) first launch time, we should use it so user doesn't get infinite trials
                if (cloudFirstLaunch > 0L && (currentFirstLaunch == 0L || cloudFirstLaunch < currentFirstLaunch)) {
                    val prefs = context.getSharedPreferences("game_license_pref", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putLong("first_launch_time", cloudFirstLaunch).apply()
                }
                
                val currentExp = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
                if (cloudExpiry > currentExp) {
                    val prefs = context.getSharedPreferences("game_license_pref", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putLong("expireTime", cloudExpiry).apply()
                }
            }

            val pj = root.optJSONObject("profile") ?: return
            val local = repository.getOrCreateProfile()

            if (requireFresh) {
                val localIsFresh = local.totalCorrect == 0 && local.totalWrong == 0 &&
                    local.unlockedStage == 0 && local.stagesCompleted == 0 && local.xp == 0
                if (!localIsFresh) return
            }

            val restoredProfile = local.copy(
                coins = pj.optInt("coins", local.coins),
                gems = pj.optInt("gems", local.gems),
                unlockedStage = pj.optInt("unlockedStage", local.unlockedStage),
                ownedAvatarsJson = pj.optString("ownedAvatarsJson", local.ownedAvatarsJson),
                ownedThemesJson = pj.optString("ownedThemesJson", local.ownedThemesJson),
                ownedEffectsJson = pj.optString("ownedEffectsJson", local.ownedEffectsJson),
                activeAvatar = pj.optString("activeAvatar", local.activeAvatar),
                activeTheme = pj.optString("activeTheme", local.activeTheme),
                activeEffect = pj.optString("activeEffect", local.activeEffect),
                unlockedAchsJson = pj.optString("unlockedAchsJson", local.unlockedAchsJson),
                totalCorrect = pj.optInt("totalCorrect", local.totalCorrect),
                totalWrong = pj.optInt("totalWrong", local.totalWrong),
                maxStreak = pj.optInt("maxStreak", local.maxStreak),
                stagesCompleted = pj.optInt("stagesCompleted", local.stagesCompleted),
                bossesDefeated = pj.optInt("bossesDefeated", local.bossesDefeated),
                purchases = pj.optInt("purchases", local.purchases),
                bestSpeed = pj.optInt("bestSpeed", local.bestSpeed),
                totalCoinsEarned = pj.optInt("totalCoinsEarned", local.totalCoinsEarned),
                perfectStages = pj.optInt("perfectStages", local.perfectStages),
                dailyStreak = pj.optInt("dailyStreak", local.dailyStreak),
                lastDailyClaimDate = if (pj.has("lastDailyClaimDate") && !pj.isNull("lastDailyClaimDate"))
                    pj.optString("lastDailyClaimDate") else local.lastDailyClaimDate,
                level = pj.optInt("level", local.level),
                xp = pj.optInt("xp", local.xp),
                classCode = pj.optString("classCode", local.classCode),
                name = pj.optString("name", local.name),
                role = pj.optString("role", local.role)
            )

            val starsMap = mutableMapOf<Int, Int>()
            root.optJSONObject("stars")?.let { starsObj ->
                val keys = starsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val stageId = key.toIntOrNull() ?: continue
                    starsMap[stageId] = starsObj.optInt(key)
                }
            }

            val tablesList = mutableListOf<TableStatEntity>()
            root.optJSONObject("tables")?.let { tablesObj ->
                val keys = tablesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val number = key.toIntOrNull() ?: continue
                    val tj = tablesObj.optJSONObject(key) ?: continue
                    tablesList.add(TableStatEntity(number, tj.optInt("c"), tj.optInt("w")))
                }
            }

            repository.restoreFromBackup(restoredProfile, starsMap, tablesList)
            _currentScreen.value = GameScreen.MainMenu
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun navigateTo(screen: GameScreen) {
        // Stop any active timers
        timerJob?.cancel()
        speedTimerJob?.cancel()
        reverseTimerJob?.cancel()
        duelTimerJob?.cancel()
        _currentScreen.value = screen
        if (screen == GameScreen.Shop || screen == GameScreen.Achievements || screen == GameScreen.ProgressReport) {
            checkAchievementsList()
        }
    }

    private suspend fun checkDailyStatus() {
        val profile = repository.getOrCreateProfile()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (profile.lastDailyClaimDate == todayStr) {
            _dailyRewardBannerText.value = "فردا ملحق شو! 🌟"
            _dailyRewardClickable.value = false
        } else {
            _dailyRewardBannerText.value = "جایزه روزانهت آماده‌ست! 😍"
            _dailyRewardClickable.value = true
        }
    }

    fun claimDailyReward() {
        if (!_dailyRewardClickable.value) return
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 86400000))
            var rewardCoins = 0
            
            repository.updateProfileAtomic { profile ->
                val isConsecutive = profile.lastDailyClaimDate == yesterdayStr
                val newStreak = if (isConsecutive) profile.dailyStreak + 1 else 1
                rewardCoins = 50 + (newStreak * 10)

                profile.copy(
                    coins = profile.coins + rewardCoins,
                    totalCoinsEarned = profile.totalCoinsEarned + rewardCoins,
                    lastDailyClaimDate = todayStr,
                    dailyStreak = newStreak
                )
            }
            _activeDailyAward.value = rewardCoins
            _dailyRewardBannerText.value = "جایزه با موفقیت جمع شد! 🎉"
            _dailyRewardClickable.value = false
            
            _soundEvents.emit(GameSoundEvent.CoinSound)
            _soundEvents.emit(GameSoundEvent.StarSound)
            checkAchievementsList()
        }
    }

    fun dismissDailyOverlay() {
        _activeDailyAward.value = null
    }

    fun dismissAchievementOverlay() {
        showNextAchievementFromQueue()
    }

    // ─── STAGE GAME PLAY ───
    fun getMsgWithUser(type: String): String {
        val name = userProfile.value?.name.orEmpty().trim()
        val raw = GameConfig.getMsg(type)
        if (name.isEmpty()) return raw
        return when (type) {
            "correct" -> {
                val prefixes = listOf("آفرین $name! ", "عالی بود $name! ", "باریکلا $name! ", "هزارآفرین $name! ", "پرچم بالاست $name! ")
                prefixes.random() + raw
            }
            "wrong" -> {
                val prefixes = listOf("اشکالی نداره $name عزیز! ", "ناامید نشو $name! ", "تلاش مجدد کن $name! ")
                prefixes.random() + raw
            }
            "speed" -> {
                "سریع‌تر $name! $raw"
            }
            "boss" -> {
                "مبارزه با غول مرحله $name! $raw"
            }
            else -> raw
        }
    }

    fun buyStageLivesWithGems(): Boolean {
        val p = userProfile.value ?: return false
        if (p.gems >= 5) {
            viewModelScope.launch {
                var success = false
                repository.updateProfileAtomic { profile ->
                    if (profile.gems >= 5) {
                        success = true
                        profile.copy(gems = profile.gems - 5)
                    } else {
                        profile
                    }
                }
                if (success) {
                    _stageState.update {
                        it.copy(
                            lives = minOf(3, it.lives + 2),
                            charMessage = "با پرداخت ۵ الماس ۲ جان تازه گرفتی! عالیه! 💪"
                        )
                    }
                    _soundEvents.emit(GameSoundEvent.CoinSound)
                }
            }
            return true
        }
        return false
    }

    fun startStage(stageId: Int) {
        timerJob?.cancel()
        val stage = GameConfig.STAGES.firstOrNull { it.id == stageId } ?: return
        
        val questions = buildStageQuestions(stageId)
        currentQuestions = questions
        
        _stageState.value = StageGameplayState(
            stageId = stageId,
            currentQuestionIndex = 0,
            correctAnswersCount = 0,
            wrongAnswersCount = 0,
            score = 0,
            lives = 3,
            streak = 0,
            bossHp = stage.q,
            isLocked = false,
            isBoss = stage.boss,
            charMessage = if (stage.boss) getMsgWithUser("boss") else "آماده‌ای؟ شروع شد! 🚀"
        )
        _currentScreen.value = GameScreen.Gameplay
        loadQuestion(0)
    }

    private fun buildStageQuestions(stageId: Int): List<Pair<Int, Int>> {
        val table = (stageId / 10) + 1
        val sub = (stageId % 10) + 1

        // The group/table number must always be the first factor (the left one).
        // The second factor b is a unique number from 1..12.
        // We select exactly 10 unique values of b to form 10 unique questions.
        val bValues = when {
            sub <= 3 -> {
                // Easy stages: second factor b is strictly from 1..10
                (1..10).toList().shuffled().take(10)
            }
            sub <= 7 -> {
                // Medium stages: second factor b is selected from 1..11
                (1..11).toList().shuffled().take(10)
            }
            else -> {
                // Hard/Boss stages: second factor b is selected from 1..12
                (1..12).toList().shuffled().take(10)
            }
        }

        // Map the unique b values to Pair(table, b)
        // Since bValues has exactly 10 unique elements, we will have exactly 10 unique questions!
        return bValues.map { b -> Pair(table, b) }.shuffled()
    }

    private fun buildAnswerOptions(a: Int, b: Int, count: Int = 4): List<Int> {
        val correct = a * b
        val options = linkedSetOf(correct)

        val candidates = mutableListOf(
            a * (b - 1), a * (b + 1), (a - 1) * b, (a + 1) * b,
            (a + 1) * (b + 1), (a - 1) * (b - 1),
            correct + a, correct - a, correct + b, correct - b, correct + 1, correct - 1
        )

        val validCandidates = candidates.filter { it > 0 && it != correct }.shuffled()
        for (candidate in validCandidates) {
            if (options.size >= count) break
            options.add(candidate)
        }

        var step = 1
        while (options.size < count) {
            val high = correct + step
            if (high > 0) options.add(high)
            if (options.size >= count) break
            val low = correct - step
            if (low > 0) options.add(low)
            step++
        }

        return options.toList().shuffled()
    }

    private fun loadQuestion(index: Int) {
        val stage = GameConfig.STAGES.first { it.id == _stageState.value.stageId }
        if (index >= currentQuestions.size) {
            finishStageGameplay()
            return
        }

        val question = currentQuestions[index]
        val optionsList = buildAnswerOptions(question.first, question.second)

        _stageState.update {
            it.copy(
                currentQuestionIndex = index,
                options = optionsList,
                activeQuestion = question,
                isLocked = false,
                selectResult = null,
                selectedOptionIndex = null,
                timerSecondsLeft = stage.time,
                timerProgress = 1.0f
            )
        }

        viewModelScope.launch {
            if (index == 0 && stage.boss) {
                _soundEvents.emit(GameSoundEvent.PlayBossStartRand)
                delay(1200)
            }
            _soundEvents.emit(GameSoundEvent.PlayLearnClip(question.first, question.second))
        }

        // Run timer counting down
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val totalTime = stage.time
            val steps = totalTime * 20 // 20 steps per second (50ms interval)
            var currentStep = steps
            while (currentStep > 0) {
                delay(50)
                currentStep--
                val secondsLeft = currentStep / 20
                val progress = currentStep.toFloat() / steps.toFloat()
                _stageState.update {
                    it.copy(
                        timerSecondsLeft = secondsLeft,
                        timerProgress = progress
                    )
                }
            }
            onTimeUp()
        }
    }

    fun submitStageAnswer(option: Int, optionIndex: Int) {
        if (_stageState.value.isLocked) return
        timerJob?.cancel()

        val state = _stageState.value
        val stage = GameConfig.STAGES.first { it.id == state.stageId }
        val q = state.activeQuestion
        val correctRes = q.first * q.second
        val isCorrect = option == correctRes

        viewModelScope.launch {
            repository.incrementTableStat(q.first, isCorrect)
        }

        if (isCorrect) {
            val newStreak = state.streak + 1
            val multiplier = when {
                newStreak >= 5 -> 4
                newStreak >= 3 -> 3
                newStreak >= 2 -> 2
                else -> 1
            }
            val scoreGained = 10 * multiplier
            val xpGained = 10 * multiplier
            val newScore = state.score + scoreGained
            val newBossHp = if (state.isBoss) maxOf(0, state.bossHp - 1) else state.bossHp
            val comboMsg = if (multiplier > 1) "ضریب کمبو x${multiplier.toFa()} امتیاز! 🔥 " else ""
            _stageState.update {
                it.copy(
                    isLocked = true,
                    selectResult = AnswerResult.Correct,
                    selectedOptionIndex = optionIndex,
                    score = newScore,
                    streak = newStreak,
                    bossHp = newBossHp,
                    correctAnswersCount = state.correctAnswersCount + 1,
                    charMessage = "$comboMsg${getMsgWithUser("correct")}"
                )
            }
            viewModelScope.launch {
                repository.updateMaxStreak(newStreak)
                _soundEvents.emit(GameSoundEvent.CorrectSound)

                // Level & XP System
                var leveledUp = false
                var finalLevel = 1
                repository.updateProfileAtomic { p ->
                    var newXp = p.xp + xpGained
                    var newLevel = p.level
                    val xpNeeded = newLevel * 100
                    if (newXp >= xpNeeded) {
                        newXp -= xpNeeded
                        newLevel += 1
                        leveledUp = true
                    }
                    finalLevel = newLevel
                    p.copy(
                        xp = newXp,
                        level = newLevel
                    )
                }
                if (leveledUp) {
                    _levelUpTrigger.value = finalLevel
                    _soundEvents.emit(GameSoundEvent.StarSound)
                }
            }
        } else {
            val newLives = maxOf(0, state.lives - 1)
            _stageState.update {
                it.copy(
                    isLocked = true,
                    selectResult = AnswerResult.Wrong,
                    selectedOptionIndex = optionIndex,
                    streak = 0,
                    lives = newLives,
                    wrongAnswersCount = state.wrongAnswersCount + 1,
                    charMessage = getMsgWithUser("wrong")
                )
            }
            viewModelScope.launch {
                _soundEvents.emit(GameSoundEvent.WrongSound)
            }

            if (newLives <= 0) {
                viewModelScope.launch {
                    delay(900)
                    finishStageGameplay()
                }
                return
            }
        }

        viewModelScope.launch {
            delay(800)
            loadQuestion(state.currentQuestionIndex + 1)
        }
    }

    private fun onTimeUp() {
        if (_stageState.value.isLocked) return
        timerJob?.cancel()

        val state = _stageState.value
        val newLives = maxOf(0, state.lives - 1)
        _stageState.update {
            it.copy(
                isLocked = true,
                lives = newLives,
                streak = 0,
                wrongAnswersCount = state.wrongAnswersCount + 1,
                charMessage = "وقت تمام شد! ⏰"
            )
        }

        viewModelScope.launch {
            _soundEvents.emit(GameSoundEvent.WrongSound)
            if (newLives <= 0) {
                delay(900)
                finishStageGameplay()
            } else {
                delay(800)
                loadQuestion(state.currentQuestionIndex + 1)
            }
        }
    }

    private fun finishStageGameplay() {
        timerJob?.cancel()
        val sState = _stageState.value
        val stage = GameConfig.STAGES.first { it.id == sState.stageId }
        val pct = sState.correctAnswersCount.toFloat() / stage.q.toFloat()
        
        val stars = if (sState.lives > 0) {
            when {
                pct == 1.0f -> 3
                pct >= 0.7f -> 2
                pct >= 0.4f -> 1
                else -> 0
            }
        } else {
            0
        }

        viewModelScope.launch {
            if (stars > 0) {
                // Save stage stars and unlock variables
                repository.saveStageResult(sState.stageId, stars, sState.score, sState.correctAnswersCount, sState.wrongAnswersCount)
                // Add coin rewards
                val finalCoinReward = stage.coinReward + (stars - 1) * 20
                repository.recordCoinsEarned(finalCoinReward)
                if (stage.gemReward > 0) {
                    repository.updateProfileAtomic { p ->
                        p.copy(gems = p.gems + stage.gemReward)
                    }
                }
                if (stage.boss) {
                    _soundEvents.emit(GameSoundEvent.PlayBossWinRand)
                } else {
                    _soundEvents.emit(GameSoundEvent.PlayFinishRand)
                }
            } else {
                // Register stats without unlocks
                repository.updateProfileAtomic { p ->
                    p.copy(
                        totalCorrect = p.totalCorrect + sState.correctAnswersCount,
                        totalWrong = p.totalWrong + sState.wrongAnswersCount
                    )
                }
                if (stage.boss) {
                    _soundEvents.emit(GameSoundEvent.PlayBossLoseRand)
                } else {
                    _soundEvents.emit(GameSoundEvent.PlayWrongRand)
                }
            }
            _currentScreen.value = GameScreen.StageResult
            checkAchievementsList()
        }
    }

    // ─── SPEED CHALLENGE ───
    fun startSpeedChallenge() {
        timerJob?.cancel()
        speedTimerJob?.cancel()

        _speedState.value = SpeedGameplayState(
            secondsLeft = 60,
            score = 0,
            streak = 0,
            isLocked = false,
            charMessage = "بزن بریم! ۶۰ ثانیه رکورد بزن! ⚡"
        )
        _currentScreen.value = GameScreen.SpeedChallenge
        loadSpeedQuestion()

        speedTimerJob = viewModelScope.launch {
            val totalSteps = 600 // 10 steps per second (100ms interval for 60 seconds)
            var currentStep = totalSteps
            while (currentStep > 0) {
                delay(100)
                currentStep--
                val secondsLeft = currentStep / 10
                val progress = currentStep.toFloat() / totalSteps.toFloat()
                _speedState.update {
                    it.copy(
                        secondsLeft = secondsLeft,
                        timerProgress = progress
                    )
                }
            }
            finishSpeedChallenge()
        }
    }

    private fun loadSpeedQuestion() {
        val a = (1..9).random()
        val b = (1..9).random()
        val optionsList = buildAnswerOptions(a, b)
        _speedState.update {
            it.copy(
                currentQuestion = Pair(a, b),
                options = optionsList,
                isLocked = false,
                lastSelectedOption = null
            )
        }
        viewModelScope.launch {
            _soundEvents.emit(GameSoundEvent.PlayLearnClip(a, b))
        }
    }

    fun submitSpeedAnswer(option: Int) {
        if (_speedState.value.isLocked) return
        val state = _speedState.value
        val correctVal = state.currentQuestion.first * state.currentQuestion.second
        val isCorrect = option == correctVal

        _speedState.update {
            it.copy(
                lastSelectedOption = option,
                isLocked = true
            )
        }

        if (isCorrect) {
            val newScore = state.score + 10
            val newStreak = state.streak + 1
            _speedState.update {
                it.copy(
                    score = newScore,
                    streak = newStreak,
                    charMessage = getMsgWithUser("speed")
                )
            }
            viewModelScope.launch {
                repository.updateMaxStreak(newStreak)
                _soundEvents.emit(GameSoundEvent.CorrectSound)
            }
        } else {
            _speedState.update {
                it.copy(
                    streak = 0,
                    charMessage = "ای بابا! دوباره تلاش کن."
                )
            }
            viewModelScope.launch { _soundEvents.emit(GameSoundEvent.WrongSound) }
        }

        viewModelScope.launch {
            delay(800)
            loadSpeedQuestion()
        }
    }

    private fun finishSpeedChallenge() {
        speedTimerJob?.cancel()
        val finalScore = _speedState.value.score
        viewModelScope.launch {
            repository.updateBestSpeed(finalScore)
            val coinGain = finalScore / 10
            if (coinGain > 0) {
                repository.recordCoinsEarned(coinGain)
            }
            _currentScreen.value = GameScreen.MainMenu
            _soundEvents.emit(GameSoundEvent.StarSound)
            checkAchievementsList()
        }
    }

    // ─── REVERSE CHALLENGE ───
    fun startReverseChallenge() {
        timerJob?.cancel()
        reverseTimerJob?.cancel()
        _reverseState.value = ReverseGameplayState()
        _currentScreen.value = GameScreen.ReverseChallenge
        loadReverseQuestion()

        reverseTimerJob = viewModelScope.launch {
            val totalSteps = 900  // 90 ثانیه × 10 قدم/ثانیه
            var currentStep = totalSteps
            while (currentStep > 0) {
                delay(100)
                currentStep--
                _reverseState.update {
                    it.copy(
                        secondsLeft = currentStep / 10,
                        timerProgress = currentStep.toFloat() / totalSteps.toFloat()
                    )
                }
            }
            finishReverseChallenge()
        }
    }

    private fun loadReverseQuestion() {
        val a = (1..9).random()
        val b = (1..9).random()
        val correctAnswer = a * b
        val correctPair = Pair(a, b)
        val wrongPairs = buildWrongPairs(a, b, correctAnswer, count = 3)
        val allOptions = (listOf(correctPair) + wrongPairs).shuffled()

        _reverseState.update {
            it.copy(
                answerToShow = correctAnswer,
                options = allOptions,
                correctOption = correctPair,
                isLocked = false,
                lastSelectedOptionIndex = null
            )
        }
        viewModelScope.launch {
            _soundEvents.emit(GameSoundEvent.SpeakText("$correctAnswer"))
        }
    }

    private fun buildWrongPairs(
        correctA: Int,
        correctB: Int,
        correctProduct: Int,
        count: Int
    ): List<Pair<Int, Int>> {
        val usedProducts = mutableSetOf(correctProduct)
        val candidates = mutableListOf<Pair<Int, Int>>()

        for (da in -3..3) {
            for (db in -3..3) {
                val na = (correctA + da).coerceIn(1, 9)
                val nb = (correctB + db).coerceIn(1, 9)
                if (na == correctA && nb == correctB) continue
                val prod = na * nb
                if (prod !in usedProducts) {
                    candidates.add(Pair(na, nb))
                    usedProducts.add(prod)
                }
            }
        }

        var attempts = 0
        while (candidates.size < count * 2 && attempts < 100) {
            val ra = (1..9).random()
            val rb = (1..9).random()
            val prod = ra * rb
            if (prod !in usedProducts) {
                candidates.add(Pair(ra, rb))
                usedProducts.add(prod)
            }
            attempts++
        }
        return candidates.shuffled().take(count)
    }

    fun submitReverseAnswer(selectedPair: Pair<Int, Int>, selectedIndex: Int) {
        if (_reverseState.value.isLocked) return
        val state = _reverseState.value
        val isCorrect = (selectedPair == state.correctOption)

        _reverseState.update { it.copy(isLocked = true, lastSelectedOptionIndex = selectedIndex) }

        if (isCorrect) {
            val newStreak = state.streak + 1
            val multiplier = when {
                newStreak >= 5 -> 3
                newStreak >= 3 -> 2
                else -> 1
            }
            val gained = 15 * multiplier
            _reverseState.update {
                it.copy(
                    score = it.score + gained,
                    streak = newStreak,
                    correctCount = it.correctCount + 1,
                    charMessage = if (multiplier > 1)
                        "کمبو x${multiplier.toFa()}! عالی! 🔥"
                    else
                        getMsgWithUser("correct")
                )
            }
            viewModelScope.launch {
                _soundEvents.emit(GameSoundEvent.CorrectSound)
            }
        } else {
            _reverseState.update {
                it.copy(
                    streak = 0,
                    wrongCount = it.wrongCount + 1,
                    charMessage = "اشتباه! ${state.correctOption.first.toFa()} × ${state.correctOption.second.toFa()} = ${state.answerToShow.toFa()} 💡"
                )
            }
            viewModelScope.launch { _soundEvents.emit(GameSoundEvent.WrongSound) }
        }

        viewModelScope.launch {
            delay(900)
            loadReverseQuestion()
        }
    }

    private fun finishReverseChallenge() {
        reverseTimerJob?.cancel()
        val finalScore = _reverseState.value.score
        viewModelScope.launch {
            val coinGain = finalScore / 15
            if (coinGain > 0) repository.recordCoinsEarned(coinGain)
            _currentScreen.value = GameScreen.MainMenu
            _soundEvents.emit(GameSoundEvent.StarSound)
            checkAchievementsList()
        }
    }

    // ─── SHOP OPERATIONS ───
    fun buyOrSelectAvatar(avatarId: String) {
        viewModelScope.launch {
            repository.buyAndSelectAvatar(avatarId)
            _soundEvents.emit(GameSoundEvent.CoinSound)
            checkAchievementsList()
        }
    }

    fun buyOrSelectTheme(themeId: String) {
        viewModelScope.launch {
            repository.buyAndSelectTheme(themeId)
            _soundEvents.emit(GameSoundEvent.CoinSound)
            checkAchievementsList()
        }
    }

    fun buyOrSelectEffect(effectId: String) {
        viewModelScope.launch {
            repository.buyAndSelectEffect(effectId)
            _soundEvents.emit(GameSoundEvent.CoinSound)
            checkAchievementsList()
        }
    }

    fun playLearnSpeech(number: Int, factor: Int) {
        viewModelScope.launch {
            _soundEvents.emit(GameSoundEvent.PlayLearnClip(number, factor))
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetGameData()
            checkDailyStatus()
            navigateTo(GameScreen.MainMenu)
        }
    }

    fun triggerSound(sound: GameSoundEvent) {
        viewModelScope.launch {
            _soundEvents.emit(sound)
        }
    }

    // ─── ACHIEVEMENT MONITORING ───
    private fun checkAchievementsList() {
        viewModelScope.launch {
            val newlyUnlockedAchs = mutableListOf<GameAchievement>()
            repository.updateProfileAtomic { profile ->
                val unlockedSet = GameRepository.parseStringList(profile.unlockedAchsJson).toMutableSet()
                var currentProfile = profile
                var changed = false
                
                GameConfig.ACHIEVEMENTS.forEach { ach ->
                    if (ach.id !in unlockedSet) {
                        val completed = when (ach.id) {
                            "first" -> currentProfile.totalCorrect >= 1
                            "streak5" -> currentProfile.maxStreak >= 5
                            "streak10" -> currentProfile.maxStreak >= 10
                            "stage1" -> currentProfile.stagesCompleted >= 1
                            "stage5" -> currentProfile.stagesCompleted >= 5
                            "allStages" -> currentProfile.stagesCompleted >= GameConfig.STAGES.size
                            "boss1" -> currentProfile.bossesDefeated >= 1
                            "shopItem" -> currentProfile.purchases >= 1
                            "speed100" -> currentProfile.bestSpeed >= 100
                            "coins200" -> currentProfile.coins >= 200
                            "perfect" -> currentProfile.perfectStages >= 1
                            "daily3" -> currentProfile.dailyStreak >= 3
                            else -> false
                        }

                        if (completed) {
                            unlockedSet.add(ach.id)
                            newlyUnlockedAchs.add(ach)
                            currentProfile = currentProfile.copy(
                                unlockedAchsJson = GameRepository.formatStringList(unlockedSet.toList()),
                                coins = currentProfile.coins + ach.reward,
                                totalCoinsEarned = currentProfile.totalCoinsEarned + ach.reward
                            )
                            changed = true
                        }
                    }
                }
                if (changed) currentProfile else profile
            }
            
            // Show notification overlay sequentially for each unlocked achievement
            if (newlyUnlockedAchs.isNotEmpty()) {
                achievementQueue.addAll(newlyUnlockedAchs)
                if (_activeAchievementAward.value == null) {
                    showNextAchievementFromQueue()
                }
            }
        }
    }

    
    fun exitDuelMode() {
        duelTimerJob?.cancel()
        _duelState.value = DuelGameState()
        _currentScreen.value = GameScreen.MainMenu
    }
    
    fun goToDuelSetup() {
        duelTimerJob?.cancel()
        _duelState.value = DuelGameState()
        _currentScreen.value = GameScreen.DuelSetup
    }

    fun startDuel(name1: String, name2: String, totalSeconds: Int) {
        duelTimerJob?.cancel()
        val n1 = name1.trim().ifEmpty { "بازیکن ۱" }
        val n2 = name2.trim().ifEmpty { "بازیکن ۲" }

        _duelState.value = DuelGameState(
            player1 = DuelPlayerState(name = n1),
            player2 = DuelPlayerState(name = n2),
            secondsLeft = totalSeconds,
            totalSeconds = totalSeconds,
            timerProgress = 1f,
            phase = DuelPhase.Playing
        )
        _currentScreen.value = GameScreen.DuelGame
        loadDuelQuestion(1)
        loadDuelQuestion(2)

        duelTimerJob = viewModelScope.launch {
            delay(4000)
            val steps = totalSeconds * 10
            var current = steps
            while (current > 0) {
                delay(100)
                current--
                _duelState.update {
                    it.copy(
                        secondsLeft = current / 10,
                        timerProgress = current.toFloat() / steps.toFloat()
                    )
                }
            }
            finishDuel()
        }
    }

    private fun loadDuelQuestion(player: Int) {
        val a = (1..12).random()
        val b = (1..12).random()
        val now = System.currentTimeMillis()
        _duelState.update { state ->
            if (player == 1)
                state.copy(player1 = state.player1.copy(
                    a = a, b = b, answered = false,
                    feedbackText = "", feedbackIsCorrect = null,
                    questionStartTimeMs = now
                ))
            else
                state.copy(player2 = state.player2.copy(
                    a = a, b = b, answered = false,
                    feedbackText = "", feedbackIsCorrect = null,
                    questionStartTimeMs = now
                ))
        }
    }

    fun submitDuelAnswer(player: Int, answer: Int) {
        val state = _duelState.value
        if (state.phase != DuelPhase.Playing) return
        val pl = if (player == 1) state.player1 else state.player2
        if (pl.answered) return

        val correct = pl.a * pl.b
        val isCorrect = answer == correct

        val newStreak = if (isCorrect) pl.streak + 1 else 0
        val mult = when {
            newStreak >= 5 -> 3
            newStreak >= 3 -> 2
            else -> 1
        }
        val newScore = if (isCorrect)
            pl.score + (10 * mult)
        else
            maxOf(0, pl.score - 3)

        val feedback = when {
            isCorrect && mult > 1 -> "درست! کمبو ×${mult.toFa()} — +${(10 * mult).toFa()}"
            isCorrect             -> "درست! +${10.toFa()}"
            else                  -> "غلط — جواب: ${correct.toFa()}"
        }

        val timeTakenMs = System.currentTimeMillis() - pl.questionStartTimeMs
        val newCorrectCount = if (isCorrect) pl.correctCount + 1 else pl.correctCount
        val newWrongCount = if (!isCorrect) pl.wrongCount + 1 else pl.wrongCount
        val newMaxCombo = maxOf(pl.maxCombo, newStreak)
        val newFastest = if (isCorrect) minOf(pl.fastestTimeMs, timeTakenMs) else pl.fastestTimeMs
        val newTotalTime = pl.totalTimeMs + timeTakenMs

        val updated = pl.copy(
            score = newScore,
            streak = newStreak,
            answered = true,
            feedbackText = feedback,
            feedbackIsCorrect = isCorrect,
            correctCount = newCorrectCount,
            wrongCount = newWrongCount,
            maxCombo = newMaxCombo,
            fastestTimeMs = newFastest,
            totalTimeMs = newTotalTime
        )

        _duelState.update { s ->
            if (player == 1) s.copy(player1 = updated)
            else             s.copy(player2 = updated)
        }

        viewModelScope.launch {
            if (isCorrect) {
                if (mult > 1) {
                    _soundEvents.emit(GameSoundEvent.PlayDuelCombo)
                } else {
                    _soundEvents.emit(GameSoundEvent.PlayDuelCorrect)
                }
            } else {
                _soundEvents.emit(GameSoundEvent.PlayDuelWrong)
            }
            delay(650)
            loadDuelQuestion(player)
        }
    }

    private fun finishDuel() {
        duelTimerJob?.cancel()
        val state = _duelState.value
        val s1 = state.player1.score
        val s2 = state.player2.score
        val n1 = state.player1.name
        val n2 = state.player2.name

        val congratsPool = listOf(
            "$n1 عزیز، امروز واقعاً درخشیدی! ریاضیت خیلی قویه. 🌟",
            "چه بازی فوقالعاده‌‌ای $n1! جدول ضرب رو مثل آب خوردن حل کردی. 🎉",
            "$n1، تو یه قهرمان واقعی جدول ضربی! این پیروزی حقته. 🏅",
            "برق از جدول ضرب گرفتی $n1! سرعت و دقتت مثال‌زدنیه. ⚡"
        )

        val result = when {
            s1 > s2 -> DuelResultState(
                winner = 1, winnerName = n1, loserName = n2,
                score1 = s1, score2 = s2,
                congratsMessage = congratsPool.random()
            )
            s2 > s1 -> {
                val msgs = congratsPool.map { it.replace(n1, n2) }
                DuelResultState(
                    winner = 2, winnerName = n2, loserName = n1,
                    score1 = s1, score2 = s2,
                    congratsMessage = msgs.random()
                )
            }
            else -> DuelResultState(
                winner = null, winnerName = n1, loserName = n2,
                score1 = s1, score2 = s2,
                congratsMessage = "$n1 و $n2، هر دو فوق‌العاده بودید! این مساوی ارزش یه جشن داره. 🤝"
            )
        }

        _duelResult.value = result
        _duelState.update { it.copy(phase = DuelPhase.Result) }

        viewModelScope.launch {
            if ((result.score1 + result.score2) / 10 > 0)
                repository.recordCoinsEarned((result.score1 + result.score2) / 20)
            _soundEvents.emit(GameSoundEvent.PlayDuelFinish)
            _currentScreen.value = GameScreen.DuelResult
        }
    }

    fun rematchDuel() {
        val state = _duelState.value
        startDuel(state.player1.name, state.player2.name, state.totalSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        speedTimerJob?.cancel()
        reverseTimerJob?.cancel()
        duelTimerJob?.cancel()
        duelTimerJob?.cancel()
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun Int.toFa(): String {
    val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.toString().map { char ->
        if (char in '0'..'9') faDigits[char - '0'] else char
    }.joinToString("")
}
