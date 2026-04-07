package org.openedx.shared.storage

/**
 * Platform-specific secure key-value storage.
 * Android: DataStore with AES encryption
 * iOS: Keychain
 */
expect class SecureStorage {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getLong(key: String, defaultValue: Long = 0L): Long
    fun putLong(key: String, value: Long)
    fun remove(key: String)
    fun clear()
}
