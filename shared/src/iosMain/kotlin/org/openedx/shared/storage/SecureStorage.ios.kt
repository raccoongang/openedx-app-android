package org.openedx.shared.storage

import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS secure storage using NSUserDefaults.
 * For sensitive data (tokens), should use Keychain.
 * This is a simplified implementation for the initial KMP setup.
 */
actual class SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, defaultValue: String): String =
        defaults.stringForKey(key) ?: defaultValue

    actual fun putString(key: String, value: String) =
        defaults.setObject(value, key)

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else defaultValue

    actual fun putBoolean(key: String, value: Boolean) =
        defaults.setBool(value, key)

    actual fun getLong(key: String, defaultValue: Long): Long =
        if (defaults.objectForKey(key) != null) defaults.integerForKey(key) else defaultValue

    actual fun putLong(key: String, value: Long) =
        defaults.setInteger(value, key)

    actual fun remove(key: String) =
        defaults.removeObjectForKey(key)

    actual fun clear() {
        defaults.dictionaryRepresentation().keys.forEach { key ->
            (key as? String)?.let { defaults.removeObjectForKey(it) }
        }
    }
}
