package org.openedx.core.utils

import org.openedx.core.Res as coreRes
import org.openedx.core.core_assessment_soon
import org.openedx.core.core_date_format_MMM_dd_yyyy
import org.openedx.core.core_date_format_due_in_days
import org.openedx.core.core_date_type_past_due
import org.openedx.core.core_date_type_today
import org.openedx.core.core_label_access_expired
import org.openedx.core.core_label_ended
import org.openedx.core.core_label_ends
import org.openedx.core.core_label_expired_on
import org.openedx.core.core_label_expires
import org.openedx.core.core_label_starting
import org.openedx.core.core_next
import org.openedx.core.domain.model.StartType
import org.openedx.foundation.system.ResourceManager
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue

@Suppress("MagicNumber")
object TimeUtils {

    private const val SEVEN_DAYS_IN_MILLIS = 604800000L
    private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    private const val MILLIS_PER_SECOND = 1000L

    private val MONTH_NAMES = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun formatToString(
        resourceManager: ResourceManager,
        instant: Instant,
        useRelativeDates: Boolean,
    ): String {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz).date
        val dateLocal = instant.toLocalDateTime(tz).date

        if (!useRelativeDates) {
            return formatMediumDate(dateLocal)
        }

        val nowMillis = now.toEpochMilliseconds()
        val dateMillis = instant.toEpochMilliseconds()
        val daysDiff = ((nowMillis - dateMillis) / MILLIS_PER_DAY).toInt()

        return when {
            daysDiff in -5..-1 -> formatWeekdayName(dateLocal.dayOfWeek)

            daysDiff == -6 -> {
                val next = resourceManager.getString(coreRes.string.core_next)
                "$next ${formatWeekdayName(dateLocal.dayOfWeek)}"
            }

            daysDiff in -1..1 -> {
                // Use relative day names: Yesterday / Today / Tomorrow
                val todayLocal = today
                val dateDay = dateLocal
                when {
                    dateDay == todayLocal -> resourceManager.getString(coreRes.string.core_date_type_today)
                    dateDay < todayLocal -> "Yesterday"
                    else -> "Tomorrow"
                }
            }

            daysDiff in 2..6 -> {
                "$daysDiff days ago"
            }

            dateLocal.year == today.year -> formatMonthDay(dateLocal)

            else -> formatMediumDate(dateLocal)
        }
    }

    fun formatToDueInString(resourceManager: ResourceManager, instant: Instant): String {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val nowDate = now.toLocalDateTime(tz).date
        val dueDate = instant.toLocalDateTime(tz).date

        val nowMillis = nowDate.atStartOfDayIn(tz).toEpochMilliseconds()
        val dueMillis = dueDate.atStartOfDayIn(tz).toEpochMilliseconds()
        val daysDifference = ((dueMillis - nowMillis) / MILLIS_PER_DAY).toInt()

        return when {
            daysDifference < 0 -> resourceManager.getString(coreRes.string.core_date_type_past_due)
            daysDifference == 0 -> resourceManager.getString(coreRes.string.core_date_type_today)
            else -> resourceManager.getString(coreRes.string.core_date_format_due_in_days, daysDifference)
        }
    }

    fun formatToMonthDay(instant: Instant): String {
        val tz = TimeZone.currentSystemDefault()
        val dateLocal = instant.toLocalDateTime(tz).date
        return formatMonthDay(dateLocal)
    }

    fun getCurrentTime(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    fun iso8601ToDate(text: String): Instant? {
        if (text.isBlank()) return null
        return try {
            Instant.parse(text)
        } catch (_: Exception) {
            null
        }
    }

    fun iso8601ToDateWithTime(resourceManager: ResourceManager, text: String): String {
        return try {
            val instant = Instant.parse(text)
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val format = LocalDateTime.Format {
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
            format.format(local)
        } catch (_: Exception) {
            ""
        }
    }

    private fun dateToCourseDate(resourceManager: ResourceManager, instant: Instant?): String {
        if (instant == null) return ""
        val tz = TimeZone.currentSystemDefault()
        val dateLocal = instant.toLocalDateTime(tz).date
        val format = resourceManager.getString(coreRes.string.core_date_format_MMM_dd_yyyy)
        return formatWithPattern(format, dateLocal)
    }

    /**
     * Formats a [LocalDate] using a simple pattern string like "MMM dd, yyyy".
     * Supports: MMM (abbreviated month), dd (day), yyyy (year).
     */
    private fun formatWithPattern(pattern: String, date: LocalDate): String {
        return pattern
            .replace("MMM", MONTH_NAMES[date.monthNumber - 1])
            .replace("dd", date.dayOfMonth.toString().padStart(2, '0'))
            .replace("yyyy", date.year.toString())
    }

    private fun isDatePassed(today: Instant, otherDate: Instant?): Boolean {
        return otherDate != null && today > otherDate
    }

    fun getCourseFormattedDate(
        resourceManager: ResourceManager,
        today: Instant,
        expiry: Instant?,
        start: Instant?,
        end: Instant?,
        startType: String,
        startDisplay: String,
    ): String {
        return when {
            isDatePassed(today, start) -> handleDatePassedToday(
                resourceManager, today, expiry, start, end, startType, startDisplay
            )
            else -> handleDateNotPassedToday(resourceManager, start, startType, startDisplay)
        }
    }

    fun getCourseAccessFormattedDate(resourceManager: ResourceManager, instant: Instant): String {
        return dateToCourseDate(resourceManager, instant)
    }

    private fun handleDatePassedToday(
        resourceManager: ResourceManager,
        today: Instant,
        expiry: Instant?,
        start: Instant?,
        end: Instant?,
        startType: String,
        startDisplay: String,
    ): String {
        return when {
            expiry != null -> handleExpiry(resourceManager, today, expiry)
            else -> handleNoExpiry(resourceManager, today, start, end, startType, startDisplay)
        }
    }

    private fun handleExpiry(
        resourceManager: ResourceManager,
        today: Instant,
        expiry: Instant,
    ): String {
        val todayMillis = today.toEpochMilliseconds()
        val expiryMillis = expiry.toEpochMilliseconds()
        val dayDifferenceInMillis = (todayMillis - expiryMillis).absoluteValue

        return when {
            isDatePassed(today, expiry) -> {
                if (dayDifferenceInMillis > SEVEN_DAYS_IN_MILLIS) {
                    resourceManager.getString(
                        coreRes.string.core_label_expired_on,
                        dateToCourseDate(resourceManager, expiry)
                    )
                } else {
                    val timeSpan = formatRelativeTimeSpan(expiryMillis, todayMillis)
                    resourceManager.getString(coreRes.string.core_label_access_expired, timeSpan)
                }
            }
            else -> {
                if (dayDifferenceInMillis > SEVEN_DAYS_IN_MILLIS) {
                    resourceManager.getString(
                        coreRes.string.core_label_expires,
                        dateToCourseDate(resourceManager, expiry)
                    )
                } else {
                    val timeSpan = formatRelativeTimeSpan(expiryMillis, todayMillis)
                    resourceManager.getString(coreRes.string.core_label_expires, timeSpan)
                }
            }
        }
    }

    private fun handleNoExpiry(
        resourceManager: ResourceManager,
        today: Instant,
        start: Instant?,
        end: Instant?,
        startType: String,
        startDisplay: String,
    ): String {
        return when {
            end == null -> handleNoEndDate(resourceManager, start, startType, startDisplay)
            isDatePassed(today, end) -> resourceManager.getString(
                coreRes.string.core_label_ended,
                dateToCourseDate(resourceManager, end)
            )
            else -> resourceManager.getString(
                coreRes.string.core_label_ends,
                dateToCourseDate(resourceManager, end)
            )
        }
    }

    private fun handleDateNotPassedToday(
        resourceManager: ResourceManager,
        start: Instant?,
        startType: String,
        startDisplay: String,
    ): String {
        return when {
            startType == StartType.TIMESTAMP.type && start != null -> resourceManager.getString(
                coreRes.string.core_label_starting,
                dateToCourseDate(resourceManager, start)
            )
            startType == StartType.STRING.type && start != null -> resourceManager.getString(
                coreRes.string.core_label_starting,
                startDisplay
            )
            else -> {
                val soon = resourceManager.getString(coreRes.string.core_assessment_soon)
                resourceManager.getString(coreRes.string.core_label_starting, soon)
            }
        }
    }

    private fun handleNoEndDate(
        resourceManager: ResourceManager,
        start: Instant?,
        startType: String,
        startDisplay: String,
    ): String {
        return when {
            startType == StartType.TIMESTAMP.type && start != null -> resourceManager.getString(
                coreRes.string.core_label_starting,
                dateToCourseDate(resourceManager, start)
            )
            startType == StartType.STRING.type && start != null -> resourceManager.getString(
                coreRes.string.core_label_starting,
                startDisplay
            )
            else -> {
                val soon = resourceManager.getString(coreRes.string.core_assessment_soon)
                resourceManager.getString(coreRes.string.core_label_starting, soon)
            }
        }
    }

    /**
     * Formats a relative time span between two epoch millisecond values.
     * Returns abbreviated relative strings like "2 hours ago", "in 3 days", etc.
     */
    private fun formatRelativeTimeSpan(timeMillis: Long, nowMillis: Long): String {
        val diffMillis = timeMillis - nowMillis
        val absDiffMillis = diffMillis.absoluteValue

        val seconds = absDiffMillis / MILLIS_PER_SECOND
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        val timeText = when {
            days > 0 -> "$days ${if (days == 1L) "day" else "days"}"
            hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"}"
            minutes > 0 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"}"
            else -> "$seconds ${if (seconds == 1L) "second" else "seconds"}"
        }

        return if (diffMillis < 0) {
            "$timeText ago"
        } else {
            "in $timeText"
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

    private fun formatMonthDay(date: LocalDate): String {
        return "${MONTH_NAMES[date.monthNumber - 1]} ${date.dayOfMonth.toString().padStart(2, '0')}"
    }

    private fun formatMediumDate(date: LocalDate): String {
        return "${MONTH_NAMES[date.monthNumber - 1]} ${date.dayOfMonth.toString().padStart(2, '0')}, ${date.year}"
    }

}
