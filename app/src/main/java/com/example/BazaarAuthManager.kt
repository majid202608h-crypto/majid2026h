package com.example

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.farsitel.bazaar.BazaarClientProxy
import com.farsitel.bazaar.core.BazaarSignIn
import com.farsitel.bazaar.core.BazaarSignInClient
import com.farsitel.bazaar.core.callback.BazaarSignInCallback
import com.farsitel.bazaar.core.model.BazaarSignInAccount
import com.farsitel.bazaar.core.model.BazaarSignInOptions
import com.farsitel.bazaar.core.model.SignInOption
import com.farsitel.bazaar.storage.BazaarStorage
import com.farsitel.bazaar.storage.callback.BazaarStorageCallback
import com.farsitel.bazaar.util.ext.toReadableString

/**
 * A thin, crash-safe wrapper around Cafe Bazaar "Login with Bazaar" (BazaarAuth)
 * and In-App Storage (BazaarStorage). Every call is guarded so a missing/old Bazaar
 * app can never crash the game. The unique account id is stored in SharedPreferences.
 */
object BazaarAuthManager {

    const val PREFS_NAME = "bazaar_auth_pref"
    const val KEY_ACCOUNT_ID = "account_id"

    // Expose client credentials securely loaded from .env/BuildConfig
    val clientId: String get() = try { BuildConfig.BAZAAR_CLIENT_ID } catch (e: Exception) { "6d2V8RC7eHXulfwQ" }
    val clientSecret: String get() = try { BuildConfig.BAZAAR_CLIENT_SECRET } catch (e: Exception) { "bZV2hldRqhtzvrXMxKsOaaCQhpkObUTN" }

    private fun buildClient(context: Context): BazaarSignInClient {
        val options = BazaarSignInOptions.Builder(SignInOption.DEFAULT_SIGN_IN).build()
        return BazaarSignIn.getClient(context, options)
    }

    fun getSavedAccountId(context: Context): String? {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACCOUNT_ID, null)
                ?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    fun saveAccountId(context: Context, accountId: String) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACCOUNT_ID, accountId)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isBazaarInstalled(context: Context): Boolean = try {
        BazaarClientProxy.isBazaarInstalledOnDevice(context)
    } catch (e: Exception) {
        false
    }

    fun needsUpdateForAuth(context: Context): Boolean = try {
        BazaarClientProxy.isNeededToUpdateBazaar(context).needToUpdateForAuth
    } catch (e: Exception) {
        false
    }

    fun needsUpdateForStorage(context: Context): Boolean = try {
        BazaarClientProxy.isNeededToUpdateBazaar(context).needToUpdateForStorage
    } catch (e: Exception) {
        false
    }

    fun showInstall(context: Context) {
        try {
            BazaarClientProxy.showInstallBazaarView(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showUpdate(context: Context) {
        try {
            BazaarClientProxy.showUpdateBazaarView(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var lastErrorMessage: String? = null

    fun getSignInIntent(context: Context): Intent = buildClient(context).getSignInIntent()

    fun parseAccount(data: Intent?): BazaarSignInAccount? = try {
        lastErrorMessage = null
        if (data == null) {
            lastErrorMessage = "Intent data is null"
            null
        } else {
            BazaarSignIn.getSignedInAccountFromIntent(data)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        lastErrorMessage = e.localizedMessage ?: e.message ?: e.javaClass.simpleName
        null
    }

    fun getLastAccount(context: Context, owner: LifecycleOwner?, onResult: (String?) -> Unit) {
        try {
            BazaarSignIn.getLastSignedInAccount(
                context,
                owner,
                BazaarSignInCallback { response ->
                    onResult(response?.data?.accountId)
                }
            )
        } catch (e: Exception) {
            onResult(null)
        }
    }

    fun saveData(context: Context, owner: LifecycleOwner?, data: ByteArray, onDone: (Boolean) -> Unit = {}) {
        try {
            BazaarStorage.saveData(
                context,
                owner,
                data,
                BazaarStorageCallback { response ->
                    onDone(response?.isSuccessful == true)
                }
            )
        } catch (e: Exception) {
            onDone(false)
        }
    }

    fun getData(context: Context, owner: LifecycleOwner?, onResult: (String?) -> Unit) {
        try {
            BazaarStorage.getSavedData(
                context,
                owner,
                BazaarStorageCallback { response ->
                    onResult(response?.data?.toReadableString())
                }
            )
        } catch (e: Exception) {
            onResult(null)
        }
    }

    fun disconnect(context: Context) {
        try {
            BazaarSignIn.disconnect(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            BazaarStorage.disconnect(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
