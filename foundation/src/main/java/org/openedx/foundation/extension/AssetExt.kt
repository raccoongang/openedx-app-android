package org.openedx.foundation.extension

import android.content.res.AssetManager

fun AssetManager.readAsText(fileName: String): String {
    return open(fileName).bufferedReader().use { it.readText() }
}
