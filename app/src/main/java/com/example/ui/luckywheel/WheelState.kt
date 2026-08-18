package com.example.ui.luckywheel

data class WheelState(
    val coins: Int = 0,
    val remainingSubDays: Int = 0,
    val isSpinAllowed: Boolean = true,
    val countdownMs: Long = 0L,
    val lastRewardText: String? = null,
    val spinCount: Int = 0,
    val currentReward: RewardType? = null,
    val isSpinning: Boolean = false,
    val targetDegrees: Float = 0f,
    val selectedIndex: Int = -1
)
