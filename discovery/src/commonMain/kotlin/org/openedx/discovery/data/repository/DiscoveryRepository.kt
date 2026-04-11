package org.openedx.discovery.data.repository

import io.ktor.client.statement.HttpResponse
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.domain.model.CourseList

interface DiscoveryRepository {
    suspend fun getCourseDetail(id: String): Course
    suspend fun getCourseDetailFromCache(id: String): Course?
    suspend fun enrollInACourse(courseId: String): HttpResponse
    suspend fun getCoursesList(username: String?, organization: String?, pageNumber: Int): CourseList
    suspend fun getCachedCoursesList(): List<Course>
    suspend fun getCoursesListByQuery(query: String, pageNumber: Int): CourseList
}
