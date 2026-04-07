package org.openedx.foundation.extension

import java.util.Locale

private const val KIBI = 1024.0
private const val MIN_SIZE_REMINDER = 0.1
private const val MAX_SIZE_REMINDER = 0.9

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
    val formatted = String.format(Locale.US, "%.${fractionDigits}f", size)
    return if (removeZeroFraction && formatted.endsWith(".0")) {
        "${formatted.dropLast(2)} $unit"
    } else {
        "$formatted $unit"
    }
}
