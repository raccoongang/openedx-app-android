package org.openedx.dates.data.repository

import org.openedx.core.domain.model.CourseDate
import org.openedx.core.domain.model.CourseDatesResponse

interface DatesRepository {
    suspend fun getUserDates(page: Int): CourseDatesResponse
    suspend fun getUserDatesFromCache(): List<CourseDate>
    suspend fun preloadFirstPageCachedDates(): List<CourseDate>
    suspend fun shiftAllDueDates()
}
