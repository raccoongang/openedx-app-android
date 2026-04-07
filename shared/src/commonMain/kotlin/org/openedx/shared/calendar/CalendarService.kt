package org.openedx.shared.calendar

/**
 * Common calendar service interface.
 * Android: CalendarContract
 * iOS: EventKit
 */
interface CalendarService {
    fun hasCalendarPermission(): Boolean
    suspend fun requestCalendarPermission(): Boolean
    fun createCalendar(title: String, color: Int): Long?
    fun deleteCalendar(calendarId: Long)
    fun addEvent(
        calendarId: Long,
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
    ): Long?
    fun deleteEvent(eventId: Long)
}
