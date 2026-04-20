package org.openedx.shared.calendar

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import org.openedx.core.domain.model.CalendarData
import org.openedx.core.domain.model.CalendarType
import org.openedx.core.domain.model.CourseDateBlock
import org.openedx.core.domain.model.UserCalendar
import org.openedx.core.system.CalendarManager
import platform.EventKit.EKAuthorizationStatus
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKCalendar
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSourceType
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneWithName
import platform.UIKit.UIColor

/**
 * iOS CalendarManager backed by EventKit.
 *
 * EventKit identifies calendars + events by `NSString` (`calendarIdentifier` / `eventIdentifier`),
 * but the shared `CalendarManager` interface uses `Long` IDs (inherited from Android's
 * `CalendarContract`). We hash the EK string ID into a `Long` and persist a reverse map in
 * `NSUserDefaults` so a cached Long can round-trip back to the EK identifier.
 */
@OptIn(ExperimentalForeignApi::class)
class IosCalendarManager : CalendarManager {

    private val store = EKEventStore()
    private val prefs: NSUserDefaults = NSUserDefaults.standardUserDefaults
    private val calendarIdKey = "openedx.ios.calendar.idmap"
    private val eventIdKey = "openedx.ios.event.idmap"

    override fun hasPermissions(): Boolean {
        val status: EKAuthorizationStatus =
            EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        return status == EKAuthorizationStatusAuthorized
    }

    override fun requestPermissions(launcher: Any) {
        store.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { _, _ -> }
    }

    override fun isCalendarExist(calendarId: Long): Boolean {
        val stringId = resolveCalendarStringId(calendarId) ?: return false
        return store.calendarWithIdentifier(stringId) != null
    }

    override fun createOrUpdateCalendar(
        calendarId: Long,
        calendarTitle: String,
        calendarColor: Long,
        calendarType: CalendarType,
    ): Long {
        if (!hasPermissions()) return CalendarManager.CALENDAR_DOES_NOT_EXIST
        val existing = resolveCalendarStringId(calendarId)?.let { store.calendarWithIdentifier(it) }
        val calendar = existing ?: EKCalendar.calendarForEntityType(
            EKEntityType.EKEntityTypeEvent,
            eventStore = store,
        )
        calendar.setTitle(calendarTitle)
        calendar.setCGColor(uiColorFromArgb(calendarColor).CGColor)
        if (calendar.source == null) {
            val localSource = store.sources.firstOrNull { (it as? platform.EventKit.EKSource)?.sourceType == EKSourceType.EKSourceTypeLocal }
                ?: store.defaultCalendarForNewEvents?.source
            if (localSource != null) calendar.setSource(localSource as platform.EventKit.EKSource)
        }
        return try {
            store.saveCalendar(calendar, commit = true, error = null)
            val longId = storeCalendarMapping(calendar.calendarIdentifier)
            longId
        } catch (t: Throwable) {
            CalendarManager.CALENDAR_DOES_NOT_EXIST
        }
    }

    override fun getGoogleCalendars(): List<UserCalendar> = emptyList()

    override fun hasAlternativeCalendarApp(): Boolean = true

    override fun addEventsIntoCalendar(
        calendarId: Long,
        courseId: String,
        courseName: String,
        courseDateBlock: CourseDateBlock,
    ): Long {
        val stringCalendarId = resolveCalendarStringId(calendarId) ?: return CalendarManager.EVENT_DOES_NOT_EXIST
        val calendar = store.calendarWithIdentifier(stringCalendarId) ?: return CalendarManager.EVENT_DOES_NOT_EXIST
        val event = EKEvent.eventWithEventStore(store).apply {
            setTitle("$courseName — ${courseDateBlock.title}")
            setNotes(courseDateBlock.description.ifEmpty { courseDateBlock.link })
            setCalendar(calendar)
            val start = NSDate.dateWithTimeIntervalSince1970(courseDateBlock.date.toEpochMilliseconds() / 1000.0)
            setStartDate(start)
            setEndDate(start)
            setAllDay(true)
        }
        return try {
            store.saveEvent(event, span = EKSpan.EKSpanThisEvent, commit = true, error = null)
            val identifier = event.eventIdentifier ?: return CalendarManager.EVENT_DOES_NOT_EXIST
            storeEventMapping(identifier)
        } catch (t: Throwable) {
            CalendarManager.EVENT_DOES_NOT_EXIST
        }
    }

    override suspend fun deleteEvents(eventIds: List<Long>) {
        eventIds.forEach { deleteEvent(it) }
    }

    override fun deleteCalendar(calendarId: Long) {
        val stringId = resolveCalendarStringId(calendarId) ?: return
        val calendar = store.calendarWithIdentifier(stringId) ?: return
        try {
            store.removeCalendar(calendar, commit = true, error = null)
            removeCalendarMapping(calendarId)
        } catch (_: Throwable) {
        }
    }

    override fun getCalendarData(calendarId: Long): CalendarData? {
        val stringId = resolveCalendarStringId(calendarId) ?: return null
        val calendar = store.calendarWithIdentifier(stringId) ?: return null
        return CalendarData(title = calendar.title, color = 0)
    }

    override fun deleteEvent(eventId: Long) {
        val stringId = resolveEventStringId(eventId) ?: return
        val event = store.eventWithIdentifier(stringId) ?: return
        try {
            store.removeEvent(event, span = EKSpan.EKSpanThisEvent, commit = true, error = null)
            removeEventMapping(eventId)
        } catch (_: Throwable) {
        }
    }

    override val permissions: Array<String> = emptyArray()

    override val accountName: String = "iCloud"

    private fun storeCalendarMapping(stringId: String): Long {
        val longId = stringId.hashCode().toLong()
        val current = prefs.dictionaryForKey(calendarIdKey)?.toMutableMap() ?: mutableMapOf<Any?, Any?>()
        current[longId.toString()] = stringId
        prefs.setObject(current, forKey = calendarIdKey)
        return longId
    }

    private fun resolveCalendarStringId(longId: Long): String? {
        val current = prefs.dictionaryForKey(calendarIdKey) ?: return null
        return current[longId.toString()] as? String
    }

    private fun removeCalendarMapping(longId: Long) {
        val current = prefs.dictionaryForKey(calendarIdKey)?.toMutableMap() ?: return
        current.remove(longId.toString())
        prefs.setObject(current, forKey = calendarIdKey)
    }

    private fun storeEventMapping(stringId: String): Long {
        val longId = stringId.hashCode().toLong()
        val current = prefs.dictionaryForKey(eventIdKey)?.toMutableMap() ?: mutableMapOf<Any?, Any?>()
        current[longId.toString()] = stringId
        prefs.setObject(current, forKey = eventIdKey)
        return longId
    }

    private fun resolveEventStringId(longId: Long): String? {
        val current = prefs.dictionaryForKey(eventIdKey) ?: return null
        return current[longId.toString()] as? String
    }

    private fun removeEventMapping(longId: Long) {
        val current = prefs.dictionaryForKey(eventIdKey)?.toMutableMap() ?: return
        current.remove(longId.toString())
        prefs.setObject(current, forKey = eventIdKey)
    }

    private fun uiColorFromArgb(argb: Long): UIColor {
        val a = ((argb shr 24) and 0xFF) / 255.0
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        return UIColor.colorWithRed(r, green = g, blue = b, alpha = a)
    }
}
