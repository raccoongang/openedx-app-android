package org.openedx.foundation.extension

import android.net.Uri

fun Uri.getQueryParams(): Map<String, String> {
    val params = mutableMapOf<String, String>()
    queryParameterNames.forEach { key ->
        getQueryParameter(key)?.let { value ->
            params[key] = value
        }
    }
    return params
}
