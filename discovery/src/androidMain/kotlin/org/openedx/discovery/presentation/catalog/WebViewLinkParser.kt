package org.openedx.discovery.presentation.catalog

import android.net.Uri
import org.openedx.foundation.extension.getQueryParams

/**
 * Android-specific parser for WebView links.
 */
object WebViewLinkParser {
    fun parse(uriStr: String?, uriScheme: String): ParsedWebViewLink? {
        if (uriStr.isNullOrEmpty()) return null

        val sanitizedUriStr = uriStr.replace("+", "%2B")
        val uri = Uri.parse(sanitizedUriStr)

        // Validate URI scheme and authority
        val isSchemeValid = uriScheme == uri.scheme
        val uriAuthority = WebViewLink.Authority.entries.find { it.key == uri.authority }

        return if (isSchemeValid && uriAuthority != null) {
            val params = uri.getQueryParams()
            ParsedWebViewLink(uriAuthority, params)
        } else {
            null
        }
    }
}

data class ParsedWebViewLink(
    val authority: WebViewLink.Authority,
    val params: Map<String, String>,
)
