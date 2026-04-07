package org.openedx.shared.config

import android.content.Context

private var appContext: Context? = null

fun initConfigLoader(context: Context) {
    appContext = context.applicationContext
}

actual fun loadConfigJson(): String {
    return try {
        appContext?.assets?.open("config/config.json")
            ?.bufferedReader()
            ?.use { it.readText() } ?: "{}"
    } catch (e: Exception) {
        "{}"
    }
}
