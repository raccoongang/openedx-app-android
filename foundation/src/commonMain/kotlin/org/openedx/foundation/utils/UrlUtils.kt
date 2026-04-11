package org.openedx.foundation.utils

object UrlUtils {

    const val QUERY_PARAM_SEARCH = "q"

    fun buildUrlWithQueryParams(baseUrl: String, queryParams: Map<String, String>): String {
        if (queryParams.isEmpty()) return baseUrl
        val separator = if ('?' in baseUrl) '&' else '?'
        val params = queryParams.entries.joinToString("&") { (key, value) ->
            "${encodeUrl(key)}=${encodeUrl(value)}"
        }
        return "$baseUrl$separator$params"
    }

    private fun encodeUrl(value: String): String {
        return buildString {
            for (c in value) {
                when {
                    c.isLetterOrDigit() || c in "-_.~" -> append(c)
                    c == ' ' -> append('+')
                    else -> {
                        val bytes = c.toString().encodeToByteArray()
                        for (b in bytes) {
                            append('%')
                            append(b.toInt().and(0xFF).toString(16).uppercase().padStart(2, '0'))
                        }
                    }
                }
            }
        }
    }
}
