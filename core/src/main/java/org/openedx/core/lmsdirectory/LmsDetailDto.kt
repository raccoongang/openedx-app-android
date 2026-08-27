package org.openedx.core.lmsdirectory

import com.google.gson.annotations.SerializedName

/** Wire format of one platform inside the directory document. */

data class LmsDetailDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("url") val url: String,
    @SerializedName("logo") val logo: String? = null,
    @SerializedName("accent_color") val accentColor: String? = null,
    @SerializedName("api") val api: ApiDto? = null,
    @SerializedName("theme") val theme: ThemeDto? = null,
    @SerializedName("feature_flags") val featureFlags: FeatureFlagsDto? = null,
) {
    data class ApiDto(
        @SerializedName("host_url") val hostUrl: String? = null,
        @SerializedName("oauth_client_id") val oauthClientId: String? = null,
        @SerializedName("feedback_email") val feedbackEmail: String? = null,
    )

    data class ThemeDto(
        @SerializedName("login_background") val loginBackground: String? = null,
    )

    data class FeatureFlagsDto(
        @SerializedName("pre_login_discovery") val preLoginDiscovery: Boolean = false,
    )

    /**
     * The platform, identified by where it sits in the document.
     *
     * Position is the only thing guaranteed unique. Two entries may legitimately
     * share an address — the same LMS listed twice under different branding —
     * and identifying them by URL silently merges them.
     */
    fun toDomain(id: String) = LmsDetail(
        id = id,
        title = name,
        shortDescription = description.orEmpty(),
        baseUrl = api?.hostUrl?.ifBlank { null } ?: url,
        logoUrl = logo,
        accentColor = accentColor,
        oauthClientId = api?.oauthClientId?.ifBlank { null },
        feedbackEmail = api?.feedbackEmail?.ifBlank { null },
        loginBackgroundUrl = theme?.loginBackground?.ifBlank { null },
        preLoginDiscovery = featureFlags?.preLoginDiscovery ?: false,
    )
}
