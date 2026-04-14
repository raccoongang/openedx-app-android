package org.openedx.core.config

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * iOS config loader.
 *
 * 1) Try config.json from main bundle (will work once Xcode build phase bundles it).
 * 2) Fallback to inlined dev config so the app is functional in CMP migration phase
 *    even before per-flavor config.json bundling is wired through Xcode.
 *
 * TODO iOS: bundle per-flavor config.json (dev/stage/prod) via Xcode build phase
 * mirroring Android's ConfigHelper -> assets/config/config.json pipeline.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun loadConfigJson(): String {
    val fromBundle = runCatching {
        val path = NSBundle.mainBundle.pathForResource("config", "json")
        if (path != null) {
            NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
        } else null
    }.getOrNull()
    if (!fromBundle.isNullOrBlank() && fromBundle != "{}") return fromBundle
    return INLINE_DEV_CONFIG
}

// Mirror of /core/assets/config/config.json (develop flavor) snapshotted for iOS bootstrap.
private const val INLINE_DEV_CONFIG = """
{
  "API_HOST_URL": "https://axim-mobile-dev.raccoongang.net",
  "APPLICATION_ID": "org.openedx.app.ios",
  "ENVIRONMENT_DISPLAY_NAME": "Localhost",
  "URI_SCHEME": "",
  "FEEDBACK_EMAIL_ADDRESS": "support@example.com",
  "FAQ_URL": "",
  "OAUTH_CLIENT_ID": "CJM1E6HmlG78STD3KBIhG4MuKfZRRmfPOe9pg8gj",
  "AGREEMENT_URLS": {
    "PRIVACY_POLICY_URL": "",
    "COOKIE_POLICY_URL": "",
    "DATA_SELL_CONSENT_URL": "",
    "TOS_URL": "",
    "EULA_URL": "",
    "SUPPORTED_LANGUAGES": []
  },
  "DISCOVERY": {
    "TYPE": "native",
    "WEBVIEW": {"BASE_URL": "", "COURSE_DETAIL_TEMPLATE": "", "PROGRAM_DETAIL_TEMPLATE": ""}
  },
  "PROGRAM": {"TYPE": "native", "WEBVIEW": {"BASE_URL": "", "PROGRAM_DETAIL_TEMPLATE": ""}},
  "DASHBOARD": {"TYPE": "gallery"},
  "FIREBASE": {"ENABLED": false, "CLOUD_MESSAGING_ENABLED": false},
  "BRAZE": {"ENABLED": false, "PUSH_NOTIFICATIONS_ENABLED": false},
  "GOOGLE": {"ENABLED": false, "CLIENT_ID": ""},
  "MICROSOFT": {"ENABLED": false, "CLIENT_ID": "", "PACKAGE_SIGNATURE": ""},
  "FACEBOOK": {"ENABLED": false, "FACEBOOK_APP_ID": "", "CLIENT_TOKEN": ""},
  "BRANCH": {"ENABLED": false},
  "EXPERIMENTAL_FEATURES": {
    "APP_LEVEL_DOWNLOADS": {"ENABLED": false},
    "APP_LEVEL_DATES": {"ENABLED": false}
  },
  "PLATFORM_NAME": "OpenEdX",
  "PLATFORM_FULL_NAME": "OpenEdX",
  "THEME_DIRECTORY": "openedx",
  "TOKEN_TYPE": "JWT",
  "WHATS_NEW_ENABLED": false,
  "SOCIAL_AUTH_ENABLED": false,
  "REGISTRATION_ENABLED": true,
  "PRE_LOGIN_EXPERIENCE_ENABLED": false,
  "BROWSER_LOGIN": false,
  "BROWSER_REGISTRATION": false,
  "UI_COMPONENTS": {
    "COURSE_DROPDOWN_NAVIGATION_ENABLED": false,
    "COURSE_UNIT_PROGRESS_ENABLED": false,
    "COURSE_DOWNLOAD_QUEUE_SCREEN": false
  }
}
"""
