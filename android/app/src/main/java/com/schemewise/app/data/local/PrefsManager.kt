package com.schemewise.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure local storage for JWT token and basic user info.
 * Uses Android EncryptedSharedPreferences so the token is never stored in plaintext.
 */
@Singleton
class PrefsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "schemewise_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Encrypted prefs corrupted (can happen after reinstall on some devices).
        // Wipe the file and start fresh — user will need to log in again.
        context.deleteSharedPreferences("schemewise_secure_prefs")
        EncryptedSharedPreferences.create(
            context,
            "schemewise_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var token: String?
        get()      = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: Int
        get()      = prefs.getInt(KEY_USER_ID, -1)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()

    var userName: String?
        get()      = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var isAdmin: Boolean
        get()      = prefs.getBoolean(KEY_IS_ADMIN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ADMIN, value).apply()

    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN     = "token"
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_ADMIN  = "is_admin"
        const  val KEY_LANGUAGE = "language"
    }

    var language: String
        get()      = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()
}
