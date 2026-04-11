package org.openedx.core.domain.interactor

import org.openedx.core.data.model.room.CourseCalendarEventEntity
import org.openedx.core.data.model.room.CourseCalendarStateEntity
import org.openedx.core.domain.model.CourseCalendarEvent
import org.openedx.core.domain.model.CourseCalendarState
import org.openedx.core.repository.CalendarRepository

class CalendarInteractorImpl(
    private val repository: CalendarRepository
) : CalendarInteractor {

    override suspend fun getEnrollmentsStatus() = repository.getEnrollmentsStatus()

    suspend fun getCourseDates(courseId: String) = repository.getCourseDates(courseId)

    suspend fun insertCourseCalendarEntityToCache(vararg courseCalendarEntity: CourseCalendarEventEntity) {
        repository.insertCourseCalendarEntityToCache(*courseCalendarEntity)
    }

    override suspend fun getCourseCalendarEventsByIdFromCache(courseId: String): List<CourseCalendarEvent> {
        return repository.getCourseCalendarEventsByIdFromCache(courseId)
    }

    override suspend fun getAllCourseCalendarEventsFromCache(): List<CourseCalendarEvent> {
        return repository.getAllCourseCalendarEventsFromCache()
    }

    override suspend fun deleteCourseCalendarEntitiesByIdFromCache(courseId: String) {
        repository.deleteCourseCalendarEntitiesByIdFromCache(courseId)
    }

    suspend fun insertCourseCalendarStateEntityToCache(vararg courseCalendarStateEntity: CourseCalendarStateEntity) {
        repository.insertCourseCalendarStateEntityToCache(*courseCalendarStateEntity)
    }

    override suspend fun getCourseCalendarStateByIdFromCache(courseId: String): CourseCalendarState? {
        return repository.getCourseCalendarStateByIdFromCache(courseId)
    }

    override suspend fun getAllCourseCalendarStateFromCache(): List<CourseCalendarState> {
        return repository.getAllCourseCalendarStateFromCache()
    }

    override suspend fun clearCalendarCachedData() {
        repository.clearCalendarCachedData()
    }

    override suspend fun resetChecksums() {
        repository.resetChecksums()
    }

    override suspend fun updateCourseCalendarStateByIdInCache(
        courseId: String,
        checksum: Int?,
        isCourseSyncEnabled: Boolean?
    ) {
        repository.updateCourseCalendarStateByIdInCache(courseId, checksum, isCourseSyncEnabled)
    }

    override suspend fun deleteCourseCalendarStateByIdFromCache(courseId: String) {
        repository.deleteCourseCalendarStateByIdFromCache(courseId)
    }
}
