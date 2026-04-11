package org.openedx.core.system

import org.openedx.core.domain.model.CalendarData
import org.openedx.core.domain.model.CourseDateBlock
import org.openedx.core.domain.model.CalendarType
import org.openedx.core.domain.model.UserCalendar

interface CalendarManager {
    fun hasPermissions(): Boolean
    fun isCalendarExist(calendarId: Long): Boolean
    fun createOrUpdateCalendar(calendarId: Long, calendarTitle: String, calendarColor: Long, calendarType: CalendarType): Long
    fun getGoogleCalendars(): List<UserCalendar>
    fun hasAlternativeCalendarApp(): Boolean
    fun addEventsIntoCalendar(calendarId: Long, courseId: String, courseName: String, courseDateBlock: CourseDateBlock): Long
    suspend fun deleteEvents(eventIds: List<Long>)
    fun deleteCalendar(calendarId: Long)
    fun getCalendarData(calendarId: Long): CalendarData?
    fun deleteEvent(eventId: Long)
    val permissions: Array<String>
    val accountName: String
    fun requestPermissions(launcher: Any)

    companion object {
        const val CALENDAR_DOES_NOT_EXIST = -1L
        const val EVENT_DOES_NOT_EXIST = -1L
    }
}
