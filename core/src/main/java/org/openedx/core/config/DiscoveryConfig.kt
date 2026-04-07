package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoveryConfig(
    @SerialName("TYPE")
    private val viewType: String = Config.ViewType.NATIVE.name,

    @SerialName("WEBVIEW")
    val webViewConfig: DiscoveryWebViewConfig = DiscoveryWebViewConfig(),
) {

    fun isViewTypeWebView(): Boolean {
        return Config.ViewType.WEBVIEW.name.equals(viewType, ignoreCase = true)
    }
}

@Serializable
data class DiscoveryWebViewConfig(
    @SerialName("BASE_URL")
    val baseUrl: String = "",

    @SerialName("COURSE_DETAIL_TEMPLATE")
    val courseUrlTemplate: String = "",

    @SerialName("PROGRAM_DETAIL_TEMPLATE")
    val programUrlTemplate: String = "",
)
