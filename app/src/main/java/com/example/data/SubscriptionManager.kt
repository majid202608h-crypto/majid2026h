package com.example.data

import android.content.Context
import android.os.SystemClock
import android.util.Log
import ir.cafebazaar.poolakey.Payment

object SubscriptionManager {
    private const val TAG = "SubscriptionManager"
    private const val PREFS_NAME = "game_license_pref"

    private const val KEY_FIRST_LAUNCH_TIME = "first_launch_time"
    private const val KEY_PERMANENT_LICENSE_ACTIVATED = "permanent_license"
    private const val KEY_SUBSCRIPTION_ID = "subscription_id"
    private const val KEY_PURCHASE_TOKEN = "purchase_token"
    private const val KEY_PURCHASE_TIME = "purchase_time"
    private const val KEY_EXPIRE_TIME = "expireTime"
    private const val KEY_PLAN_NAME = "plan_name"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    private const val KEY_LAST_KNOWN_TIME = "last_known_time"
    private const val KEY_LAST_ELAPSED_REALTIME = "last_elapsed_time"

    fun getSecureCurrentTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastKnownTime = prefs.getLong(KEY_LAST_KNOWN_TIME, 0L)
        val lastElapsed = prefs.getLong(KEY_LAST_ELAPSED_REALTIME, 0L)
        val systemTime = System.currentTimeMillis()
        val elapsedRealtime = SystemClock.elapsedRealtime()

        if (lastKnownTime == 0L) {
            prefs.edit()
                .putLong(KEY_LAST_KNOWN_TIME, systemTime)
                .putLong(KEY_LAST_ELAPSED_REALTIME, elapsedRealtime)
                .apply()
            return systemTime
        }

        val elapsedDelta = elapsedRealtime - lastElapsed
        val secureTime = if (elapsedDelta > 0 && lastElapsed > 0) {
            lastKnownTime + elapsedDelta
        } else {
            if (systemTime > lastKnownTime) systemTime else lastKnownTime
        }

        prefs.edit()
            .putLong(KEY_LAST_KNOWN_TIME, secureTime)
            .putLong(KEY_LAST_ELAPSED_REALTIME, elapsedRealtime)
            .apply()
        return secureTime
    }

    fun initFirstLaunchIfNeeded(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH_TIME, 0L)
        if (firstLaunch == 0L) {
            firstLaunch = getSecureCurrentTime(context)
            prefs.edit().putLong(KEY_FIRST_LAUNCH_TIME, firstLaunch).apply()
            Log.d(TAG, "First launch initialized at: $firstLaunch")
        }
        return firstLaunch
    }

    fun getFirstLaunchTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH_TIME, 0L)
        return if (firstLaunch == 0L) initFirstLaunchIfNeeded(context) else firstLaunch
    }

    fun isPermanentLicensed(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PERMANENT_LICENSE_ACTIVATED, false)
    }

    fun setPermanentLicensed(context: Context, activated: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PERMANENT_LICENSE_ACTIVATED, activated).apply()
    }

    fun recordPermanentActivationCode(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_PERMANENT_LICENSE_ACTIVATED, true)
            .remove(KEY_SUBSCRIPTION_ID)
            .remove(KEY_PURCHASE_TOKEN)
            .remove(KEY_PURCHASE_TIME)
            .remove(KEY_EXPIRE_TIME)
            .remove(KEY_PLAN_NAME)
            .remove(KEY_LAST_SYNC_TIME)
            .apply()
    }

    fun recordTimedActivationCode(context: Context, days: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentExp = prefs.getLong(KEY_EXPIRE_TIME, 0L)
        val secureNow = getSecureCurrentTime(context)
        val base = if (currentExp > secureNow) currentExp else secureNow
        val newExp = base + (days * 24L * 60L * 60L * 1000L)
        prefs.edit()
            .putString(KEY_SUBSCRIPTION_ID, "activation_code_$days")
            .putString(KEY_PURCHASE_TOKEN, "activated")
            .putLong(KEY_PURCHASE_TIME, secureNow)
            .putLong(KEY_EXPIRE_TIME, newExp)
            .putString(KEY_PLAN_NAME, "کد فعال‌سازی $days روزه")
            .putLong(KEY_LAST_SYNC_TIME, secureNow)
            .putBoolean(KEY_PERMANENT_LICENSE_ACTIVATED, false)
            .apply()
    }

    fun recordPurchase(context: Context, productId: String, purchaseToken: String, purchaseTime: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val days = when (productId) {
            "sub_1month" -> 30
            "sub_3month" -> 90
            "sub_6month" -> 180
            "sub_1year" -> 365
            else -> 30
        }
        val planName = when (productId) {
            "sub_1month" -> "اشتراک ۱ ماهه"
            "sub_3month" -> "اشتراک ۳ ماهه"
            "sub_6month" -> "اشتراک ۶ ماهه"
            "sub_1year" -> "اشتراک ۱ ساله"
            else -> "اشتراک ویژه"
        }
        val durationMs = days * 24L * 60L * 60L * 1000L
        val secureNow = getSecureCurrentTime(context)
        val expireTime = purchaseTime + durationMs
        prefs.edit()
            .putString(KEY_SUBSCRIPTION_ID, productId)
            .putString(KEY_PURCHASE_TOKEN, purchaseToken)
            .putLong(KEY_PURCHASE_TIME, purchaseTime)
            .putLong(KEY_EXPIRE_TIME, expireTime)
            .putString(KEY_PLAN_NAME, planName)
            .putLong(KEY_LAST_SYNC_TIME, secureNow)
            .apply()
        Log.d(TAG, "Recorded purchase: $productId, expires at: $expireTime")
    }

    fun getSubscriptionDetails(context: Context): Map<String, Any> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "subscriptionId" to (prefs.getString(KEY_SUBSCRIPTION_ID, "") ?: ""),
            "purchaseToken" to (prefs.getString(KEY_PURCHASE_TOKEN, "") ?: ""),
            "purchaseTime" to prefs.getLong(KEY_PURCHASE_TIME, 0L),
            "expireTime" to prefs.getLong(KEY_EXPIRE_TIME, 0L),
            "planName" to (prefs.getString(KEY_PLAN_NAME, "") ?: ""),
            "lastSyncTime" to prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
        )
    }

    fun clearSubscriptionDetails(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_SUBSCRIPTION_ID)
            .remove(KEY_PURCHASE_TOKEN)
            .remove(KEY_PURCHASE_TIME)
            .remove(KEY_EXPIRE_TIME)
            .remove(KEY_PLAN_NAME)
            .remove(KEY_LAST_SYNC_TIME)
            .apply()
    }

    fun syncSubscription(context: Context, payment: Payment, onComplete: (Boolean) -> Unit = {}) {
        payment.getSubscribedProducts {
            querySucceed { purchasedProducts ->
                val activeSub = purchasedProducts.find {
                    it.productId in listOf("sub_1month", "sub_3month", "sub_6month", "sub_1year")
                }
                if (activeSub != null) {
                    recordPurchase(
                        context = context,
                        productId = activeSub.productId,
                        purchaseToken = activeSub.purchaseToken ?: "",
                        purchaseTime = activeSub.purchaseTime
                    )
                    onComplete(true)
                } else {
                    clearSubscriptionDetails(context)
                    onComplete(false)
                }
            }
            queryFailed { errorStatus ->
                onComplete(false)
            }
        }
    }

    fun isLicenseExpired(context: Context): Boolean {
        if (isPermanentLicensed(context)) {
            return false
        }
        val secureNow = getSecureCurrentTime(context)
        val details = getSubscriptionDetails(context)
        val expireTime = details["expireTime"] as Long

        if (expireTime > 0L) {
            val expired = secureNow >= expireTime
            return expired
        }

        val firstLaunch = getFirstLaunchTime(context)
        val trialDurationMs = 3L * 24L * 60L * 60L * 1000L
        val elapsed = secureNow - firstLaunch
        val trialExpired = elapsed >= trialDurationMs
        return trialExpired
    }

    fun getRemainingTimeMs(context: Context): Long {
        if (isPermanentLicensed(context)) {
            return Long.MAX_VALUE
        }
        val secureNow = getSecureCurrentTime(context)
        val details = getSubscriptionDetails(context)
        val expireTime = details["expireTime"] as Long
        if (expireTime > 0L) {
            return (expireTime - secureNow).coerceAtLeast(0L)
        }
        val firstLaunch = getFirstLaunchTime(context)
        val trialDurationMs = 3L * 24L * 60L * 60L * 1000L
        val elapsed = secureNow - firstLaunch
        return (trialDurationMs - elapsed).coerceAtLeast(0L)
    }
}
