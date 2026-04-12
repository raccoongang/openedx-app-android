package org.openedx.discovery.presentation.catalog

object WebViewLinkParser {
    fun parse(uriStr: String?, uriScheme: String): ParsedWebViewLink? {
        if (uriStr.isNullOrEmpty()) return null

        val sanitizedUriStr = uriStr.replace("+", "%2B")

        val schemeEnd = sanitizedUriStr.indexOf("://")
        if (schemeEnd < 0) return null

        val scheme = sanitizedUriStr.substring(0, schemeEnd)
        val afterScheme = sanitizedUriStr.substring(schemeEnd + 3)

        val queryStart = afterScheme.indexOf("?")
        val authority = if (queryStart >= 0) afterScheme.substring(0, queryStart) else afterScheme
        val queryString = if (queryStart >= 0) afterScheme.substring(queryStart + 1) else ""

        val isSchemeValid = uriScheme == scheme
        val uriAuthority = WebViewLink.Authority.entries.find { it.key == authority }

        return if (isSchemeValid && uriAuthority != null) {
            val params = parseQueryParams(queryString)
            ParsedWebViewLink(uriAuthority, params)
        } else {
            null
        }
    }

    private fun parseQueryParams(queryString: String): Map<String, String> {
        if (queryString.isEmpty()) return emptyMap()
        return queryString.split("&").mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }
}

data class ParsedWebViewLink(
    val authority: WebViewLink.Authority,
    val params: Map<String, String>,
)
