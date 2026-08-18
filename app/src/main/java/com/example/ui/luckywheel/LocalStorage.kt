package com.example.ui.luckywheel

import android.content.Context

class LocalStorage(context: Context) {
    private val prefs = context.getSharedPreferences("secure_lucky_wheel_prefs", Context.MODE_PRIVATE)
    private val encryptionManager = EncryptionManager(context)

    fun getLong(key: String, defaultValue: Long): Long {
        val encryptedValue = prefs.getString(key, null) ?: return defaultValue
        return try {
            encryptionManager.decrypt(encryptedValue).toLong()
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun putLong(key: String, value: Long) {
        val encryptedValue = encryptionManager.encrypt(value.toString())
        prefs.edit().putString(key, encryptedValue).apply()
    }

    fun getString(key: String, defaultValue: String?): String? {
        val encryptedValue = prefs.getString(key, null) ?: return defaultValue
        return try {
            encryptionManager.decrypt(encryptedValue)
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun putString(key: String, value: String?) {
        if (value == null) {
            prefs.edit().remove(key).apply()
        } else {
            val encryptedValue = encryptionManager.encrypt(value)
            prefs.edit().putString(key, encryptedValue).apply()
        }
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val encryptedValue = prefs.getString(key, null) ?: return defaultValue
        return try {
            encryptionManager.decrypt(encryptedValue).toInt()
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun putInt(key: String, value: Int) {
        val encryptedValue = encryptionManager.encrypt(value.toString())
        prefs.edit().putString(key, encryptedValue).apply()
    }
}
