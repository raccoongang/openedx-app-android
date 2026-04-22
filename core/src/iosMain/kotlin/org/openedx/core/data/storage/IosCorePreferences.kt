package org.openedx.core.data.storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.openedx.core.data.model.User
import org.openedx.core.domain.model.AppConfig
import org.openedx.core.domain.model.VideoQuality
import org.openedx.core.domain.model.VideoSettings
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of CorePreferences.
 *
 * Auth tokens (access/refresh/push) live in the Keychain via [KeychainStore].
 * Non-sensitive values (user profile JSON, video settings, flags, token
 * expiry timestamp) stay in NSUserDefaults — matching the native iOS app's
 * AppStorage split (see openedx-app-ios/OpenEdX/Data/AppStorage.swift).
 *
 * On first access after upgrading, plaintext tokens previously stored in
 * NSUserDefaults are migrated into the Keychain so existing sessions survive.
 */
class IosCorePreferences : CorePreferences {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val keychain = KeychainStore()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private object Keys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val PUSH_TOKEN = "push_token"
        const val EXPIRES_IN = "expires_in"
        const val USER = "user"
        const val APP_CONFIG = "app_config"
        const val CAN_RESET_APP_DIRECTORY = "reset_app_directory"
        const val IS_RELATIVE_DATES_ENABLED = "is_relative_dates_enabled"
        const val VIDEO_WIFI_DOWNLOAD_ONLY = "video_settings_wifi_download_only"
        const val VIDEO_STREAMING_QUALITY = "video_settings_streaming_quality"
        const val VIDEO_DOWNLOAD_QUALITY = "video_settings_download_quality"
    }

    init {
        migratePlaintextTokenIfNeeded(Keys.ACCESS_TOKEN)
        migratePlaintextTokenIfNeeded(Keys.REFRESH_TOKEN)
        migratePlaintextTokenIfNeeded(Keys.PUSH_TOKEN)
    }

    private fun string(key: String, default: String = ""): String =
        defaults.stringForKey(key) ?: default

    private fun migratePlaintextTokenIfNeeded(key: String) {
        val legacy = defaults.stringForKey(key).orEmpty()
        if (legacy.isEmpty()) return
        if (keychain.get(key).isNullOrEmpty()) {
            keychain.set(key, legacy)
        }
        defaults.removeObjectForKey(key)
    }

    override var accessToken: String
        get() = keychain.get(Keys.ACCESS_TOKEN).orEmpty()
        set(value) {
            if (value.isEmpty()) keychain.remove(Keys.ACCESS_TOKEN) else keychain.set(Keys.ACCESS_TOKEN, value)
        }

    override var refreshToken: String
        get() = keychain.get(Keys.REFRESH_TOKEN).orEmpty()
        set(value) {
            if (value.isEmpty()) keychain.remove(Keys.REFRESH_TOKEN) else keychain.set(Keys.REFRESH_TOKEN, value)
        }

    override var pushToken: String
        get() = keychain.get(Keys.PUSH_TOKEN).orEmpty()
        set(value) {
            if (value.isEmpty()) keychain.remove(Keys.PUSH_TOKEN) else keychain.set(Keys.PUSH_TOKEN, value)
        }

    override var accessTokenExpiresAt: Long
        get() = defaults.integerForKey(Keys.EXPIRES_IN)
        set(value) = defaults.setInteger(value, Keys.EXPIRES_IN)

    override var user: User?
        get() {
            val payload = string(Keys.USER)
            return if (payload.isEmpty()) null else runCatching {
                json.decodeFromString<User>(payload)
            }.getOrNull()
        }
        set(value) {
            if (value == null) {
                defaults.removeObjectForKey(Keys.USER)
            } else {
                defaults.setObject(json.encodeToString(value), Keys.USER)
            }
        }

    override var videoSettings: VideoSettings
        get() {
            val wifiOnly = if (defaults.objectForKey(Keys.VIDEO_WIFI_DOWNLOAD_ONLY) == null) {
                true
            } else {
                defaults.boolForKey(Keys.VIDEO_WIFI_DOWNLOAD_ONLY)
            }
            val streaming = string(Keys.VIDEO_STREAMING_QUALITY, VideoQuality.AUTO.name)
            val download = string(Keys.VIDEO_DOWNLOAD_QUALITY, VideoQuality.AUTO.name)
            return VideoSettings(
                wifiDownloadOnly = wifiOnly,
                videoStreamingQuality = runCatching { VideoQuality.valueOf(streaming) }
                    .getOrDefault(VideoQuality.AUTO),
                videoDownloadQuality = runCatching { VideoQuality.valueOf(download) }
                    .getOrDefault(VideoQuality.AUTO),
            )
        }
        set(value) {
            defaults.setBool(value.wifiDownloadOnly, Keys.VIDEO_WIFI_DOWNLOAD_ONLY)
            defaults.setObject(value.videoStreamingQuality.name, Keys.VIDEO_STREAMING_QUALITY)
            defaults.setObject(value.videoDownloadQuality.name, Keys.VIDEO_DOWNLOAD_QUALITY)
        }

    override var appConfig: AppConfig
        get() {
            val payload = string(Keys.APP_CONFIG)
            return if (payload.isEmpty()) AppConfig() else runCatching {
                json.decodeFromString<AppConfig>(payload)
            }.getOrDefault(AppConfig())
        }
        set(value) = defaults.setObject(json.encodeToString(value), Keys.APP_CONFIG)

    override var canResetAppDirectory: Boolean
        get() = if (defaults.objectForKey(Keys.CAN_RESET_APP_DIRECTORY) == null) true
        else defaults.boolForKey(Keys.CAN_RESET_APP_DIRECTORY)
        set(value) = defaults.setBool(value, Keys.CAN_RESET_APP_DIRECTORY)

    override var isRelativeDatesEnabled: Boolean
        get() = if (defaults.objectForKey(Keys.IS_RELATIVE_DATES_ENABLED) == null) true
        else defaults.boolForKey(Keys.IS_RELATIVE_DATES_ENABLED)
        set(value) = defaults.setBool(value, Keys.IS_RELATIVE_DATES_ENABLED)

    override suspend fun clearCorePreferences() {
        keychain.remove(Keys.ACCESS_TOKEN)
        keychain.remove(Keys.REFRESH_TOKEN)
        keychain.remove(Keys.PUSH_TOKEN)
        defaults.removeObjectForKey(Keys.EXPIRES_IN)
        defaults.removeObjectForKey(Keys.USER)
    }
}
