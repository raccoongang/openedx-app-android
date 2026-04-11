package org.openedx.dashboard.data.repository

import org.openedx.core.domain.model.CourseEnrollments
import org.openedx.core.domain.model.DashboardCourseList
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.dashboard.domain.CourseStatusFilter

interface DashboardRepository {
    suspend fun getEnrolledCourses(page: Int): DashboardCourseList
    suspend fun getEnrolledCoursesFromCache(): List<EnrolledCourse>
    suspend fun getMainUserCourses(pageSize: Int): CourseEnrollments
    suspend fun getAllUserCourses(page: Int, status: CourseStatusFilter?): DashboardCourseList
}
