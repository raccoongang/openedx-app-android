package org.openedx.core.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.openedx.core.domain.model.AgreementUrls

@Suppress("TooManyFunctions")
class Config {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private var configProperties: JsonObject = try {
        val configString = loadConfigJson()
        json.parseToJsonElement(configString).jsonObject
    } catch (e: Exception) {
        e.printStackTrace()
        JsonObject(emptyMap())
    }

    fun getAppId(): String {
        return getString(APPLICATION_ID, "")
    }

    fun getApiHostURL(): String {
        return getString(API_HOST_URL)
    }

    fun getUriScheme(): String {
        return getString(URI_SCHEME)
    }

    fun getOAuthClientId(): String {
        return getString(OAUTH_CLIENT_ID)
    }

    fun getAccessTokenType(): String {
        return getString(TOKEN_TYPE)
    }

    fun getFaqUrl(): String {
        return getString(FAQ_URL)
    }

    fun getFeedbackEmailAddress(): String {
        return getString(FEEDBACK_EMAIL_ADDRESS)
    }

    fun getPlatformName(): String {
        return getString(PLATFORM_NAME)
    }

    fun getAgreement(locale: String): AgreementUrls {
        val agreement =
            getObjectOrNewInstance<AgreementUrlsConfig>(AGREEMENT_URLS).mapToDomain()
        return agreement.getAgreementForLocale(locale)
    }

    fun getFirebaseConfig(): FirebaseConfig {
        return getObjectOrNewInstance(FIREBASE)
    }

    fun getBrazeConfig(): BrazeConfig {
        return getObjectOrNewInstance(BRAZE)
    }

    fun getFacebookConfig(): FacebookConfig {
        return getObjectOrNewInstance(FACEBOOK)
    }

    fun getGoogleConfig(): GoogleConfig {
        return getObjectOrNewInstance(GOOGLE)
    }

    fun getMicrosoftConfig(): MicrosoftConfig {
        return getObjectOrNewInstance(MICROSOFT)
    }

    fun isSocialAuthEnabled() = getBoolean(SOCIAL_AUTH_ENABLED, false)

    fun getDiscoveryConfig(): DiscoveryConfig {
        return getObjectOrNewInstance(DISCOVERY)
    }

    fun getProgramConfig(): ProgramConfig {
        return getObjectOrNewInstance(PROGRAM)
    }

    fun getDashboardConfig(): DashboardConfig {
        return getObjectOrNewInstance(DASHBOARD)
    }

    fun getDownloadsConfig(): AppLevelDownloadsConfig {
        return getExperimentalFeaturesConfig().appLevelDownloadsConfig
    }

    fun getDatesConfig(): AppLevelDatesConfig {
        return getExperimentalFeaturesConfig().appLevelDatesConfig
    }

    fun getBranchConfig(): BranchConfig {
        return getObjectOrNewInstance(BRANCH)
    }

    fun isWhatsNewEnabled(): Boolean {
        return getBoolean(WHATS_NEW_ENABLED, false)
    }

    fun isPreLoginExperienceEnabled(): Boolean {
        return getBoolean(PRE_LOGIN_EXPERIENCE_ENABLED, true)
    }

    fun getCourseUIConfig(): UIConfig {
        return getObjectOrNewInstance(UI_COMPONENTS)
    }

    fun isRegistrationEnabled(): Boolean {
        return getBoolean(REGISTRATION_ENABLED, true)
    }

    fun isBrowserLoginEnabled(): Boolean {
        return getBoolean(BROWSER_LOGIN, false)
    }

    fun isBrowserRegistrationEnabled(): Boolean {
        return getBoolean(BROWSER_REGISTRATION, false)
    }

    private fun getExperimentalFeaturesConfig(): ExperimentalFeaturesConfig {
        return getObjectOrNewInstance(EXPERIMENTAL_FEATURES)
    }

    private fun getString(key: String, defaultValue: String = ""): String {
        val element = configProperties[key]
        return (element as? JsonPrimitive)?.content ?: defaultValue
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val element = configProperties[key]
        return (element as? JsonPrimitive)?.booleanOrNull ?: defaultValue
    }

    private inline fun <reified T> getObjectOrNewInstance(key: String): T {
        val element = configProperties[key]
        return if (element != null) {
            try {
                json.decodeFromJsonElement(kotlinx.serialization.serializer<T>(), element)
            } catch (e: Exception) {
                json.decodeFromJsonElement(
                    kotlinx.serialization.serializer<T>(),
                    JsonObject(emptyMap())
                )
            }
        } else {
            json.decodeFromJsonElement(
                kotlinx.serialization.serializer<T>(),
                JsonObject(emptyMap())
            )
        }
    }

    class ConfigParsingException(cause: Throwable) : Exception(cause)

    companion object {
        private const val APPLICATION_ID = "APPLICATION_ID"
        private const val API_HOST_URL = "API_HOST_URL"
        private const val URI_SCHEME = "URI_SCHEME"
        private const val OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID"
        private const val TOKEN_TYPE = "TOKEN_TYPE"
        private const val FAQ_URL = "FAQ_URL"
        private const val FEEDBACK_EMAIL_ADDRESS = "FEEDBACK_EMAIL_ADDRESS"
        private const val AGREEMENT_URLS = "AGREEMENT_URLS"
        private const val WHATS_NEW_ENABLED = "WHATS_NEW_ENABLED"
        private const val SOCIAL_AUTH_ENABLED = "SOCIAL_AUTH_ENABLED"
        private const val FIREBASE = "FIREBASE"
        private const val BRAZE = "BRAZE"
        private const val FACEBOOK = "FACEBOOK"
        private const val GOOGLE = "GOOGLE"
        private const val MICROSOFT = "MICROSOFT"
        private const val PRE_LOGIN_EXPERIENCE_ENABLED = "PRE_LOGIN_EXPERIENCE_ENABLED"
        private const val REGISTRATION_ENABLED = "REGISTRATION_ENABLED"
        private const val BROWSER_LOGIN = "BROWSER_LOGIN"
        private const val BROWSER_REGISTRATION = "BROWSER_REGISTRATION"
        private const val DISCOVERY = "DISCOVERY"
        private const val PROGRAM = "PROGRAM"
        private const val DASHBOARD = "DASHBOARD"
        private const val EXPERIMENTAL_FEATURES = "EXPERIMENTAL_FEATURES"
        private const val BRANCH = "BRANCH"
        private const val UI_COMPONENTS = "UI_COMPONENTS"
        private const val PLATFORM_NAME = "PLATFORM_NAME"
    }

    enum class ViewType {
        NATIVE,
        WEBVIEW
    }
}
