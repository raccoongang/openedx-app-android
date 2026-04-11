package org.openedx.core.utils

expect object Sha1Util {
    fun SHA1(text: String): String
    fun convertToHex(data: ByteArray): String
}
