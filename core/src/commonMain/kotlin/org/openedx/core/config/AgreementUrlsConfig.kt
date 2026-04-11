package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.Agreement
import org.openedx.core.domain.model.AgreementUrls

@Serializable
internal data class AgreementUrlsConfig(
    @SerialName("PRIVACY_POLICY_URL")
    private val privacyPolicyUrl: String = "",
    @SerialName("COOKIE_POLICY_URL")
    private val cookiePolicyUrl: String = "",
    @SerialName("DATA_SELL_CONSENT_URL")
    private val dataSellConsentUrl: String = "",
    @SerialName("TOS_URL")
    private val tosUrl: String = "",
    @SerialName("EULA_URL")
    private val eulaUrl: String = "",
    @SerialName("SUPPORTED_LANGUAGES")
    private val supportedLanguages: List<String> = emptyList(),
) {
    fun mapToDomain(): Agreement {
        val defaultAgreementUrls = AgreementUrls(
            privacyPolicyUrl = privacyPolicyUrl,
            cookiePolicyUrl = cookiePolicyUrl,
            dataSellConsentUrl = dataSellConsentUrl,
            tosUrl = tosUrl,
            eulaUrl = eulaUrl,
            supportedLanguages = supportedLanguages,
        )
        val agreementUrls = if (supportedLanguages.isNotEmpty()) {
            supportedLanguages.associateWith {
                AgreementUrls(
                    privacyPolicyUrl = privacyPolicyUrl.appendLocale(it),
                    cookiePolicyUrl = cookiePolicyUrl.appendLocale(it),
                    dataSellConsentUrl = dataSellConsentUrl.appendLocale(it),
                    tosUrl = tosUrl.appendLocale(it),
                    eulaUrl = eulaUrl.appendLocale(it),
                    supportedLanguages = supportedLanguages,
                )
            }
        } else {
            mapOf()
        }
        return Agreement(agreementUrls, defaultAgreementUrls)
    }

    private fun String.appendLocale(locale: String): String {
        if (this.isBlank()) return this
        val schemeEnd = this.indexOf("://")
        if (schemeEnd == -1) return this
        val pathStart = this.indexOf('/', schemeEnd + 3)
        return if (pathStart == -1) {
            "$this/$locale"
        } else {
            val scheme = this.substring(0, pathStart)
            val path = this.substring(pathStart)
            "$scheme/$locale$path"
        }
    }
}
