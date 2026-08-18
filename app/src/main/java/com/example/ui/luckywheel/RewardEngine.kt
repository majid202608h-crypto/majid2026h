package com.example.ui.luckywheel

import kotlin.random.Random

class RewardEngine {
    
    fun getSectors(remainingSubDays: Int): List<RewardType> {
        return if (remainingSubDays < 10) {
            listOf(
                RewardType.Coins(10),
                RewardType.Coins(20),
                RewardType.Coins(20),
                RewardType.Coins(50),
                RewardType.Coins(50),
                RewardType.Coins(100),
                RewardType.Coins(100),
                RewardType.Coins(200)
            )
        } else {
            listOf(
                RewardType.Coins(10),
                RewardType.Coins(20),
                RewardType.Coins(20),
                RewardType.Coins(50),
                RewardType.Coins(100),
                RewardType.Subscription(1),
                RewardType.Subscription(3),
                RewardType.Subscription(7)
            )
        }
    }

    fun getWeights(remainingSubDays: Int): List<Float> {
        return if (remainingSubDays < 10) {
            listOf(30f, 12.5f, 12.5f, 10f, 10f, 6f, 6f, 13f)
        } else {
            listOf(30f, 12.5f, 12.5f, 20f, 12f, 7f, 4f, 2f)
        }
    }

    fun selectRandomRewardIndex(remainingSubDays: Int): Int {
        val weights = getWeights(remainingSubDays)
        val totalWeight = weights.sum()
        val randomValue = Random.nextFloat() * totalWeight
        
        var currentSum = 0f
        for (i in weights.indices) {
            currentSum += weights[i]
            if (randomValue <= currentSum) {
                return i
            }
        }
        return weights.size - 1
    }
}
