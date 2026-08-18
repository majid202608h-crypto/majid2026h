package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 50,
    val gems: Int = 0,
    val unlockedStage: Int = 0,
    val ownedAvatarsJson: String = "[\"owl\"]",
    val ownedThemesJson: String = "[\"default\"]",
    val ownedEffectsJson: String = "[\"stars\"]",
    val activeAvatar: String = "owl",
    val activeTheme: String = "default",
    val activeEffect: String = "stars",
    val unlockedAchsJson: String = "[]",
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val maxStreak: Int = 0,
    val stagesCompleted: Int = 0,
    val bossesDefeated: Int = 0,
    val purchases: Int = 0,
    val bestSpeed: Int = 0,
    val totalCoinsEarned: Int = 50,
    val perfectStages: Int = 0,
    val dailyStreak: Int = 0,
    val lastDailyClaimDate: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val classCode: String = "",
    val name: String = "",
    val role: String = "student"
)
