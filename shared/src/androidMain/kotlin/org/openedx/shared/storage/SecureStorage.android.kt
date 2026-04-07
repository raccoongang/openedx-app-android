package org.openedx.shared.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual class SecureStorage(context: Context) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "openedx_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    actual fun putString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    actual fun putBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()

    actual fun getLong(key: String, defaultValue: Long): Long =
        prefs.getLong(key, defaultValue)

    actual fun putLong(key: String, value: Long) =
        prefs.edit().putLong(key, value).apply()

    actual fun remove(key: String) =
        prefs.edit().remove(key).apply()

    actual fun clear() =
        prefs.edit().clear().apply()
}
