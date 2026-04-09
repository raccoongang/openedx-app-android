package org.openedx.core.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

object InstantUtils {

    fun iso8601ToInstant(text: String): Instant? {
        if (text.isBlank()) return null
        return try {
            Instant.parse(text)
        } catch (_: Exception) {
            null
        }
    }

    fun getCurrentTime(): Long = Clock.System.now().toEpochMilliseconds()
}

fun Instant.isToday(): Boolean {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val thisDate = this.toLocalDateTime(tz).date
    return thisDate == today
}

fun Instant.startOfDay(): Instant {
    val tz = TimeZone.currentSystemDefault()
    return this.toLocalDateTime(tz).date.atStartOfDayIn(tz)
}

fun Instant.addDays(days: Int): Instant {
    val tz = TimeZone.currentSystemDefault()
    val localDate = this.toLocalDateTime(tz).date
    return localDate.plus(days, DateTimeUnit.DAY).atStartOfDayIn(tz)
}

fun Instant.toLocalDate(): LocalDate {
    return this.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
