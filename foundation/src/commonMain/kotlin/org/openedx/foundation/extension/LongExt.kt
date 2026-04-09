package org.openedx.foundation.extension

import kotlin.math.pow
import kotlin.math.roundToLong

private const val KIBI = 1024.0

fun Long.toFileSize(round: Int = 0, space: Boolean = false): String {
    val fractionDigits = round
    val removeZeroFraction = !space
    val sizeInKb = this / KIBI
    val sizeInMb = sizeInKb / KIBI
    val sizeInGb = sizeInMb / KIBI

    return when {
        sizeInGb >= 1 -> formatSize(sizeInGb, "GB", fractionDigits, removeZeroFraction)
        sizeInMb >= 1 -> formatSize(sizeInMb, "MB", fractionDigits, removeZeroFraction)
        sizeInKb >= 1 -> formatSize(sizeInKb, "KB", fractionDigits, removeZeroFraction)
        else -> "$this B"
    }
}

private fun formatSize(
    size: Double,
    unit: String,
    fractionDigits: Int,
    removeZeroFraction: Boolean,
): String {
    val factor = 10.0.pow(fractionDigits)
    val rounded = (size * factor).roundToLong() / factor
    val formatted = if (fractionDigits == 0) {
        rounded.toLong().toString()
    } else {
        rounded.toString().let { s ->
            val dot = s.indexOf('.')
            if (dot < 0) "$s.${"0".repeat(fractionDigits)}"
            else s.padEnd(dot + 1 + fractionDigits, '0').take(dot + 1 + fractionDigits)
        }
    }
    return if (removeZeroFraction && fractionDigits > 0 && formatted.endsWith(".0")) {
        "${formatted.dropLast(2)} $unit"
    } else {
        "$formatted $unit"
    }
}
