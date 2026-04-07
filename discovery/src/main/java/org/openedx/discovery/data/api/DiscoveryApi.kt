package org.openedx.discovery.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.openedx.core.data.model.EnrollBody
import org.openedx.discovery.data.model.CourseDetails
import org.openedx.discovery.data.model.CourseList

class DiscoveryApi(private val client: HttpClient) {

    suspend fun getCourseList(
        searchQuery: String? = null,
        page: Int,
        mobile: Boolean,
        mobileSearch: Boolean,
        username: String? = null,
        org: String? = null,
        permission: List<String> = listOf("enroll", "see_in_catalog", "see_about_page"),
    ): CourseList {
        return client.get("/api/courses/v1/courses/") {
            searchQuery?.let { parameter("search_term", it) }
            parameter("page", page)
            parameter("mobile", mobile)
            parameter("mobile_search", mobileSearch)
            username?.let { parameter("username", it) }
            org?.let { parameter("org", it) }
            permission.forEach { parameter("permissions", it) }
        }.body()
    }

    suspend fun getCourseDetail(
        courseId: String?,
        username: String? = null,
    ): CourseDetails {
        return client.get("/api/courses/v1/courses/$courseId") {
            username?.let { parameter("username", it) }
        }.body()
    }

    suspend fun enrollInACourse(enrollBody: EnrollBody): HttpResponse {
        return client.post("/api/enrollment/v1/enrollment") {
            contentType(ContentType.Application.Json)
            setBody(enrollBody)
        }
    }
}
