package org.openedx.core.data.storage

import org.openedx.core.domain.model.CalendarType
import platform.Foundation.NSUserDefaults

/**
 * iOS CalendarPreferences backed by NSUserDefaults.
 *
 * TODO iOS: hook this up to EventKit/iCloud Calendar once the CMP migration adds
 * a real iOS CalendarManager. Today the underlying CalendarInteractor is a no-op
 * stub, so only `calendarUser` actually gets read (by SignInViewModel to detect
 * account switches).
 */
class IosCalendarPreferences : CalendarPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    private object Keys {
        const val CALENDAR_ID = "calendar_id"
        const val CALENDAR_USER = "calendar_user"
        const val CALENDAR_TYPE = "calendar_type"
        const val IS_CALENDAR_SYNC_ENABLED = "is_calendar_sync_enabled"
        const val IS_HIDE_INACTIVE_COURSES = "hide_inactive_courses"
    }

    override var calendarId: Long
        get() = defaults.integerForKey(Keys.CALENDAR_ID)
        set(value) = defaults.setInteger(value, Keys.CALENDAR_ID)

    override var calendarUser: String
        get() = defaults.stringForKey(Keys.CALENDAR_USER) ?: ""
        set(value) = defaults.setObject(value, Keys.CALENDAR_USER)

    override var calendarType: CalendarType
        get() {
            val stored = defaults.stringForKey(Keys.CALENDAR_TYPE) ?: CalendarType.LOCAL.name
            return runCatching { CalendarType.valueOf(stored) }.getOrDefault(CalendarType.LOCAL)
        }
        set(value) = defaults.setObject(value.name, Keys.CALENDAR_TYPE)

    override var isCalendarSyncEnabled: Boolean
        get() = if (defaults.objectForKey(Keys.IS_CALENDAR_SYNC_ENABLED) == null) true
        else defaults.boolForKey(Keys.IS_CALENDAR_SYNC_ENABLED)
        set(value) = defaults.setBool(value, Keys.IS_CALENDAR_SYNC_ENABLED)

    override var isHideInactiveCourses: Boolean
        get() = if (defaults.objectForKey(Keys.IS_HIDE_INACTIVE_COURSES) == null) true
        else defaults.boolForKey(Keys.IS_HIDE_INACTIVE_COURSES)
        set(value) = defaults.setBool(value, Keys.IS_HIDE_INACTIVE_COURSES)

    override suspend fun clearCalendarPreferences() {
        defaults.removeObjectForKey(Keys.CALENDAR_ID)
        defaults.removeObjectForKey(Keys.CALENDAR_TYPE)
        defaults.removeObjectForKey(Keys.IS_CALENDAR_SYNC_ENABLED)
        defaults.removeObjectForKey(Keys.IS_HIDE_INACTIVE_COURSES)
    }
}
