package org.openedx.foundation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object UrlUtils {

    const val QUERY_PARAM_SEARCH = "q"

    fun openInBrowser(activity: Context, apiHostUrl: String, url: String) {
        openInBrowser(activity, "$apiHostUrl$url")
    }

    private fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            // No browser available
        }
    }

    fun buildUrlWithQueryParams(baseUrl: String, queryParams: Map<String, String>): String {
        if (queryParams.isEmpty()) return baseUrl
        val uri = Uri.parse(baseUrl).buildUpon()
        queryParams.forEach { (key, value) ->
            uri.appendQueryParameter(key, value)
        }
        return uri.build().toString()
    }
}
