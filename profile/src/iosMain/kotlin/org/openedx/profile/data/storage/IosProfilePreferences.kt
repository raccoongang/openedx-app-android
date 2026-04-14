package org.openedx.profile.data.storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.openedx.profile.data.model.Account
import platform.Foundation.NSUserDefaults

/**
 * iOS ProfilePreferences backed by NSUserDefaults.
 *
 * TODO iOS: align with the encrypted storage iOS CorePreferences will eventually
 * use (Keychain). Today this is plaintext.
 */
class IosProfilePreferences : ProfilePreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private companion object {
        const val ACCOUNT_KEY = "profile_account"
    }

    override var profile: Account?
        get() {
            val payload = defaults.stringForKey(ACCOUNT_KEY) ?: return null
            if (payload.isEmpty()) return null
            return runCatching { json.decodeFromString<Account>(payload) }.getOrNull()
        }
        set(value) {
            if (value == null) {
                defaults.removeObjectForKey(ACCOUNT_KEY)
            } else {
                defaults.setObject(json.encodeToString(value), ACCOUNT_KEY)
            }
        }
}
