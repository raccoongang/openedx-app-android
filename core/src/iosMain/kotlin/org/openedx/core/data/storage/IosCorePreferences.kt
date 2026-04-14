package org.openedx.core.data.storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.openedx.core.data.model.User
import org.openedx.core.domain.model.AppConfig
import org.openedx.core.domain.model.VideoQuality
import org.openedx.core.domain.model.VideoSettings
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of CorePreferences backed by NSUserDefaults.
 *
 * TODO iOS: tokens are stored in plaintext in NSUserDefaults. Migrate to Keychain
 * before any production use. The Android impl uses encrypted DataStore + Keystore.
 */
class IosCorePreferences : CorePreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

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

    private fun string(key: String, default: String = ""): String =
        defaults.stringForKey(key) ?: default

    override var accessToken: String
        get() = string(Keys.ACCESS_TOKEN)
        set(value) = defaults.setObject(value, Keys.ACCESS_TOKEN)

    override var refreshToken: String
        get() = string(Keys.REFRESH_TOKEN)
        set(value) = defaults.setObject(value, Keys.REFRESH_TOKEN)

    override var pushToken: String
        get() = string(Keys.PUSH_TOKEN)
        set(value) = defaults.setObject(value, Keys.PUSH_TOKEN)

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
        defaults.removeObjectForKey(Keys.ACCESS_TOKEN)
        defaults.removeObjectForKey(Keys.REFRESH_TOKEN)
        defaults.removeObjectForKey(Keys.PUSH_TOKEN)
        defaults.removeObjectForKey(Keys.EXPIRES_IN)
        defaults.removeObjectForKey(Keys.USER)
    }
}
