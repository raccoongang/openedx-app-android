package org.openedx.shared.calendar

import org.openedx.core.domain.model.CalendarData
import org.openedx.core.domain.model.CalendarType
import org.openedx.core.domain.model.CourseDateBlock
import org.openedx.core.domain.model.UserCalendar
import org.openedx.core.system.CalendarManager

/**
 * iOS stub for CalendarManager.
 * TODO: Implement using EventKit framework.
 */
class IosCalendarManager : CalendarManager {
    override fun hasPermissions(): Boolean = false

    override fun isCalendarExist(calendarId: Long): Boolean = false

    override fun createOrUpdateCalendar(
        calendarId: Long,
        calendarTitle: String,
        calendarColor: Long,
        calendarType: CalendarType,
    ): Long = CalendarManager.CALENDAR_DOES_NOT_EXIST

    override fun getGoogleCalendars(): List<UserCalendar> = emptyList()

    override fun hasAlternativeCalendarApp(): Boolean = false

    override fun addEventsIntoCalendar(
        calendarId: Long,
        courseId: String,
        courseName: String,
        courseDateBlock: CourseDateBlock,
    ): Long = CalendarManager.EVENT_DOES_NOT_EXIST

    override suspend fun deleteEvents(eventIds: List<Long>) {
        // No-op
    }

    override fun deleteCalendar(calendarId: Long) {
        // No-op
    }

    override fun getCalendarData(calendarId: Long): CalendarData? = null

    override fun deleteEvent(eventId: Long) {
        // No-op
    }

    override val permissions: Array<String> = emptyArray()

    override val accountName: String = ""

    override fun requestPermissions(launcher: Any) {
        // No-op
    }
}
