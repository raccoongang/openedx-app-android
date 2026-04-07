package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ProgramConfig(
    @SerialName("TYPE")
    private val viewType: String = Config.ViewType.NATIVE.name,
    @SerialName("WEBVIEW")
    val webViewConfig: ProgramWebViewConfig = ProgramWebViewConfig(),
) {
    fun isViewTypeWebView(): Boolean {
        return Config.ViewType.WEBVIEW.name.equals(viewType, ignoreCase = true)
    }
}

data class ProgramWebViewConfig(
    @SerialName("BASE_URL")
    val programUrl: String = "",
    @SerialName("PROGRAM_DETAIL_TEMPLATE")
    val programDetailUrlTemplate: String = "",
)
