package org.openedx.core.domain.interactor

import org.openedx.core.domain.model.CourseCalendarEvent
import org.openedx.core.domain.model.CourseCalendarState
import org.openedx.core.domain.model.EnrollmentStatus

/**
 * iOS stub — Room-backed CalendarRepository/DAO aren't ported to iOS yet.
 *
 * TODO iOS: once Room KMP (or SQLDelight) is configured for the shared database,
 * replace with the real CalendarInteractorImpl. Used by SignInViewModel on successful
 * login to clear calendar cache when switching accounts — no-op here is safe.
 */
class StubCalendarInteractor : CalendarInteractor {
    override suspend fun getEnrollmentsStatus(): List<EnrollmentStatus> = emptyList()
    override suspend fun getCourseCalendarEventsByIdFromCache(courseId: String): List<CourseCalendarEvent> = emptyList()
    override suspend fun getAllCourseCalendarEventsFromCache(): List<CourseCalendarEvent> = emptyList()
    override suspend fun deleteCourseCalendarEntitiesByIdFromCache(courseId: String) = Unit
    override suspend fun getCourseCalendarStateByIdFromCache(courseId: String): CourseCalendarState? = null
    override suspend fun getAllCourseCalendarStateFromCache(): List<CourseCalendarState> = emptyList()
    override suspend fun clearCalendarCachedData() = Unit
    override suspend fun resetChecksums() = Unit
    override suspend fun updateCourseCalendarStateByIdInCache(
        courseId: String,
        checksum: Int?,
        isCourseSyncEnabled: Boolean?
    ) = Unit
    override suspend fun deleteCourseCalendarStateByIdFromCache(courseId: String) = Unit
}
