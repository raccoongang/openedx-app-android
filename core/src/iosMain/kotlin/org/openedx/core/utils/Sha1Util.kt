package org.openedx.core.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA1
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual object Sha1Util {

    actual fun SHA1(text: String): String {
        return try {
            val bytes = text.encodeToByteArray()
            val digest = UByteArray(CC_SHA1_DIGEST_LENGTH)
            bytes.usePinned { pinned ->
                digest.usePinned { digestPinned ->
                    CC_SHA1(pinned.addressOf(0), bytes.size.convert(), digestPinned.addressOf(0))
                }
            }
            convertToHex(digest.toByteArray())
        } catch (_: Exception) {
            text
        }
    }

    @Suppress("MagicNumber")
    actual fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            var halfbyte = b.toInt() ushr 4 and 0x0F
            var twoHalfs = 0
            do {
                buf.append(
                    if (halfbyte in 0..9) {
                        ('0'.code + halfbyte).toChar()
                    } else {
                        ('a'.code + (halfbyte - 10)).toChar()
                    }
                )
                halfbyte = b.toInt() and 0x0F
            } while (twoHalfs++ < 1)
        }
        return buf.toString()
    }
}
