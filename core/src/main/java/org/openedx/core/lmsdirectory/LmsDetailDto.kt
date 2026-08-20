package org.openedx.core.lmsdirectory

import com.google.gson.annotations.SerializedName

/** Wire format of one platform inside the directory document. */

data class LmsDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("short_description") val shortDescription: String? = null,
    @SerializedName("base_url") val baseUrl: String,
    @SerializedName("logo_url") val logoUrl: String? = null,
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
        @SerializedName("login_background_url") val loginBackgroundUrl: String? = null,
    )

    data class FeatureFlagsDto(
        @SerializedName("pre_login_discovery") val preLoginDiscovery: Boolean = false,
    )

    fun toDomain() = LmsDetail(
        id = id,
        title = title,
        shortDescription = shortDescription.orEmpty(),
        baseUrl = api?.hostUrl?.ifBlank { null } ?: baseUrl,
        logoUrl = logoUrl,
        accentColor = accentColor,
        oauthClientId = api?.oauthClientId?.ifBlank { null },
        feedbackEmail = api?.feedbackEmail?.ifBlank { null },
        loginBackgroundUrl = theme?.loginBackgroundUrl?.ifBlank { null },
        preLoginDiscovery = featureFlags?.preLoginDiscovery ?: false,
    )
}
