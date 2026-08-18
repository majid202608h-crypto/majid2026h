package com.example.ui.luckywheel

sealed class RewardType {
    data class Coins(val amount: Int) : RewardType()
    data class Subscription(val days: Int) : RewardType()

    fun getDisplayNameFarsi(): String {
        return when (this) {
            is Coins -> "${amount.toFa()} سکه 🪙"
            is Subscription -> "${days.toFa()} روز اشتراک ⭐"
        }
    }

    fun getSuccessMessageFarsi(): String {
        return when (this) {
            is Coins -> "شما ${amount.toFa()} سکه برنده شدید!"
            is Subscription -> "${days.toFa()} روز به اشتراک شما اضافه شد!"
        }
    }
}

private fun Int.toFa(): String {
    val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.toString().map { char ->
        if (char in '0'..'9') faDigits[char - '0'] else char
    }.joinToString("")
}
