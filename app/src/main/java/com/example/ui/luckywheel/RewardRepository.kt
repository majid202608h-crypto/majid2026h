package com.example.ui.luckywheel

import android.content.Context
import android.os.SystemClock
import com.example.data.GameRepository

class RewardRepository private constructor(
    context: Context,
    private val gameRepository: GameRepository
) {
    private val appContext: Context = context.applicationContext
    private val localStorage = LocalStorage(appContext)

    private val KEY_LAST_SPIN_WALL_TIME = "lucky_wheel_last_spin_wall_time"
    private val KEY_LAST_SPIN_ELAPSED_TIME = "lucky_wheel_last_spin_elapsed_time"
    private val KEY_LAST_REWARD_TEXT = "lucky_wheel_last_reward_text"
    private val KEY_SPIN_COUNT = "lucky_wheel_spin_count"

    private val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

    companion object {
        @Volatile
        private var INSTANCE: RewardRepository? = null

        fun getInstance(context: Context, gameRepository: GameRepository): RewardRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RewardRepository(context, gameRepository).also { INSTANCE = it }
            }
        }
    }

    fun getLastSpinWallTime(): Long {
        return localStorage.getLong(KEY_LAST_SPIN_WALL_TIME, 0L)
    }

    fun getLastSpinElapsedTime(): Long {
        return localStorage.getLong(KEY_LAST_SPIN_ELAPSED_TIME, 0L)
    }

    fun getLastRewardText(): String? {
        return localStorage.getString(KEY_LAST_REWARD_TEXT, null)
    }

    fun getSpinCount(): Int {
        return localStorage.getInt(KEY_SPIN_COUNT, 0)
    }

    fun recordSpin(rewardText: String) {
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        
        localStorage.putLong(KEY_LAST_SPIN_WALL_TIME, nowWall)
        localStorage.putLong(KEY_LAST_SPIN_ELAPSED_TIME, nowElapsed)
        localStorage.putString(KEY_LAST_REWARD_TEXT, rewardText)
        localStorage.putInt(KEY_SPIN_COUNT, getSpinCount() + 1)
    }

    suspend fun applyReward(reward: RewardType) {
        when (reward) {
            is RewardType.Coins -> {
                gameRepository.updateProfileAtomic { profile ->
                    profile.copy(
                        coins = profile.coins + reward.amount,
                        totalCoinsEarned = profile.totalCoinsEarned + reward.amount
                    )
                }
            }
            is RewardType.Subscription -> {
                val licensePrefs = appContext.getSharedPreferences("game_license_pref", Context.MODE_PRIVATE)
                val currentExpiry = licensePrefs.getLong("license_expiry_time", 0L)
                val baseTime = if (currentExpiry > System.currentTimeMillis()) currentExpiry else System.currentTimeMillis()
                val addedDuration = reward.days * 24L * 60L * 60L * 1000L
                val newExpiry = baseTime + addedDuration
                licensePrefs.edit().putLong("license_expiry_time", newExpiry).apply()
            }
        }
    }

    fun isSpinAllowed(): Boolean {
        val lastWall = getLastSpinWallTime()
        if (lastWall == 0L) return true

        val nowWall = System.currentTimeMillis()

        // Simple protection against turning system clock backwards
        if (nowWall < lastWall) {
            return false
        }

        // Additional protection using elapsedRealtime if boot didn't reset (nowElapsed >= lastElapsed)
        val lastElapsed = getLastSpinElapsedTime()
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed >= lastElapsed) {
            val elapsedDiff = nowElapsed - lastElapsed
            if (elapsedDiff < ONE_DAY_MILLIS) {
                return false
            }
        }

        val wallDiff = nowWall - lastWall
        return wallDiff >= ONE_DAY_MILLIS
    }

    fun getCountdownToNextSpin(): Long {
        if (isSpinAllowed()) return 0L
        val lastWall = getLastSpinWallTime()
        val nowWall = System.currentTimeMillis()

        val lastElapsed = getLastSpinElapsedTime()
        val nowElapsed = SystemClock.elapsedRealtime()

        var countdown = 0L

        // Base countdown on wall time
        val targetWall = lastWall + ONE_DAY_MILLIS
        if (targetWall > nowWall) {
            countdown = maxOf(countdown, targetWall - nowWall)
        }

        // Also check if elapsedRealtime can provide a larger (more secure) remaining time
        if (nowElapsed >= lastElapsed) {
            val targetElapsed = lastElapsed + ONE_DAY_MILLIS
            if (targetElapsed > nowElapsed) {
                countdown = maxOf(countdown, targetElapsed - nowElapsed)
            }
        }

        return countdown.coerceAtLeast(0L)
    }

    fun exportForCloud(): org.json.JSONObject {
        val obj = org.json.JSONObject()
        obj.put("lastSpinWall", getLastSpinWallTime())
        obj.put("spinCount", getSpinCount())
        return obj
    }

    fun reconcileFromCloud(cloudLastSpinWall: Long, cloudSpinCount: Int) {
        val localLastWall = getLastSpinWallTime()
        val localCount = getSpinCount()

        val finalLastWall = maxOf(localLastWall, cloudLastSpinWall)
        val finalCount = maxOf(localCount, cloudSpinCount)

        if (finalLastWall > localLastWall) {
            localStorage.putLong(KEY_LAST_SPIN_WALL_TIME, finalLastWall)
            // Reset elapsed base to current real time so clock forwards check doesn't instantly think elapsed has passed
            localStorage.putLong(KEY_LAST_SPIN_ELAPSED_TIME, SystemClock.elapsedRealtime())
        }
        if (finalCount > localCount) {
            localStorage.putInt(KEY_SPIN_COUNT, finalCount)
        }
    }
}
