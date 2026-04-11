package org.openedx.core.domain.interactor

import org.openedx.core.domain.model.CourseCalendarEvent
import org.openedx.core.domain.model.CourseCalendarState
import org.openedx.core.domain.model.EnrollmentStatus

interface CalendarInteractor {
    suspend fun getEnrollmentsStatus(): List<EnrollmentStatus>
    suspend fun getCourseCalendarEventsByIdFromCache(courseId: String): List<CourseCalendarEvent>
    suspend fun getAllCourseCalendarEventsFromCache(): List<CourseCalendarEvent>
    suspend fun deleteCourseCalendarEntitiesByIdFromCache(courseId: String)
    suspend fun getCourseCalendarStateByIdFromCache(courseId: String): CourseCalendarState?
    suspend fun getAllCourseCalendarStateFromCache(): List<CourseCalendarState>
    suspend fun clearCalendarCachedData()
    suspend fun resetChecksums()
    suspend fun updateCourseCalendarStateByIdInCache(
        courseId: String,
        checksum: Int? = null,
        isCourseSyncEnabled: Boolean? = null
    )
    suspend fun deleteCourseCalendarStateByIdFromCache(courseId: String)
}
