package com.example.ui.luckywheel

import android.content.Context
import android.provider.Settings
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionManager(private val context: Context) {
    private val secretKey: SecretKeySpec
    private val ivSpec: IvParameterSpec

    init {
        // Create a unique key seed bound to ANDROID_ID and application package
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "LuckyWheelDefault"
        val rawKeySeed = (androidId + context.packageName).padEnd(32, 'X').take(32)
        secretKey = SecretKeySpec(rawKeySeed.toByteArray(StandardCharsets.UTF_8), "AES")
        
        val rawIvSeed = androidId.padStart(16, 'Y').take(16)
        ivSpec = IvParameterSpec(rawIvSeed.toByteArray(StandardCharsets.UTF_8))
    }

    fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT or Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(cipherText: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decodedBytes = Base64.decode(cipherText, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }
}
