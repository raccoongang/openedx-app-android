package org.openedx.core.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
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

    /**
     * Parses an ISO 8601 date string and formats it as "dd MMM yyyy hh:mm AM/PM".
     * Multiplatform replacement for [TimeUtils.iso8601ToDateWithTime].
     */
    fun iso8601ToDateWithTime(text: String): String {
        return try {
            val instant = Instant.parse(text)
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val outputFormat = LocalDateTime.Format {
                dayOfMonth(Padding.ZERO)
                char(' ')
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                char(' ')
                year()
                char(' ')
                amPmHour(Padding.ZERO)
                char(':')
                minute(Padding.ZERO)
                char(' ')
                amPmMarker("AM", "PM")
            }
            outputFormat.format(local)
        } catch (_: Exception) {
            ""
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

/**
 * Format an [Instant] to a human-readable string.
 * When [useRelativeDates] is false, returns medium-format date (e.g. "Jan 15, 2024").
 * When true, returns relative descriptions ("Yesterday", "Today", "Tomorrow", weekday names, etc.).
 */
fun formatToString(instant: Instant, useRelativeDates: Boolean): String {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val dateLocal = instant.toLocalDateTime(tz).date

    if (!useRelativeDates) {
        return formatMediumDate(dateLocal)
    }

    val daysDiff = today.daysUntil(dateLocal) // positive = future, negative = past
    return when {
        daysDiff == 0 -> "Today"
        daysDiff == -1 -> "Yesterday"
        daysDiff == 1 -> "Tomorrow"
        daysDiff in 2..6 -> formatWeekdayName(dateLocal.dayOfWeek)
        daysDiff in -6..-2 -> "$daysDiff".let {
            val absDays = (-daysDiff)
            "$absDays days ago"
        }
        else -> {
            if (dateLocal.year == today.year) {
                formatMonthDay(dateLocal)
            } else {
                formatMediumDate(dateLocal)
            }
        }
    }
}

private fun formatWeekdayName(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        DayOfWeek.SUNDAY -> "Sunday"
    }
}

private val MONTH_NAMES = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private fun formatMonthDay(date: LocalDate): String {
    return "${MONTH_NAMES[date.monthNumber - 1]} ${date.dayOfMonth}"
}

private fun formatMediumDate(date: LocalDate): String {
    return "${MONTH_NAMES[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}
