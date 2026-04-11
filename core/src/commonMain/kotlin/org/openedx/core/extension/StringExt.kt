package org.openedx.core.extension

fun String?.equalsHost(host: String?): Boolean {
    return try {
        val urlHost = this?.let {
            val withoutProtocol = it.substringAfter("://")
            withoutProtocol.substringBefore("/").substringBefore("?").substringBefore(":")
        }
        host?.startsWith(urlHost ?: "", ignoreCase = true) == true
    } catch (_: Exception) {
        false
    }
}
