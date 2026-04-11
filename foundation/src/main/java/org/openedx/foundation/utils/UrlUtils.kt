package org.openedx.foundation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun UrlUtils.openInBrowser(activity: Context, apiHostUrl: String, url: String) {
    openInBrowser(activity, "$apiHostUrl$url")
}

private fun UrlUtils.openInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        // No browser available
    }
}
