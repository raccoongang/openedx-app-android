package org.openedx.core.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.openedx.core.data.model.AnnouncementModel
import org.openedx.core.data.model.BlocksCompletionBody
import org.openedx.core.data.model.CourseComponentStatus
import org.openedx.core.data.model.CourseDates
import org.openedx.core.data.model.CourseDatesBannerInfo
import org.openedx.core.data.model.CourseDatesResponse
import org.openedx.core.data.model.CourseEnrollmentDetails
import org.openedx.core.data.model.CourseEnrollments
import org.openedx.core.data.model.CourseProgressResponse
import org.openedx.core.data.model.CourseStructureModel
import org.openedx.core.data.model.DownloadCoursePreview
import org.openedx.core.data.model.EnrollmentStatus
import org.openedx.core.data.model.HandoutsModel
import org.openedx.core.data.model.ResetCourseDates

class CourseApi(private val client: HttpClient) {

    suspend fun getEnrolledCourses(
        cacheControlHeaderParam: String? = null,
        username: String,
        org: String? = null,
        page: Int,
    ): CourseEnrollments {
        return client.get("/api/mobile/v3/users/$username/course_enrollments/") {
            cacheControlHeaderParam?.let { header("Cache-Control", it) }
            org?.let { parameter("org", it) }
            parameter("page", page)
        }.body()
    }

    suspend fun getCourseStructure(
        cacheControlHeaderParam: String,
        blocksApiVersion: String,
        username: String?,
        courseId: String,
    ): CourseStructureModel {
        return client.get(
            "/api/mobile/$blocksApiVersion/course_info/blocks/?" +
                "depth=all&" +
                "requested_fields=contains_gated_content,show_gated_sections,special_exam_info,graded,format," +
                "student_view_multi_device,due,completion&" +
                "student_view_data=video,discussion&" +
                "block_counts=video&" +
                "nav_depth=3"
        ) {
            header("Cache-Control", cacheControlHeaderParam)
            username?.let { parameter("username", it) }
            parameter("course_id", courseId)
        }.body()
    }

    suspend fun getCourseStatus(
        username: String,
        courseId: String,
    ): CourseComponentStatus {
        return client.get("/api/mobile/v1/users/$username/course_status_info/$courseId").body()
    }

    suspend fun markBlocksCompletion(blocksCompletionBody: BlocksCompletionBody) {
        client.post("/api/completion/v1/completion-batch") {
            contentType(ContentType.Application.Json)
            setBody(blocksCompletionBody)
        }
    }

    suspend fun getCourseDates(
        courseId: String,
        allowNotStartedCourses: Boolean = true,
        mobile: Boolean = true,
    ): CourseDates {
        return client.get("/api/course_home/v1/dates/$courseId") {
            parameter("allow_not_started_courses", allowNotStartedCourses)
            parameter("mobile", mobile)
        }.body()
    }

    suspend fun resetCourseDates(courseBody: Map<String, String>): ResetCourseDates {
        return client.post("/api/course_experience/v1/reset_course_deadlines") {
            contentType(ContentType.Application.Json)
            setBody(courseBody)
        }.body()
    }

    suspend fun getDatesBannerInfo(courseId: String): CourseDatesBannerInfo {
        return client.get("/api/course_experience/v1/course_deadlines_info/$courseId").body()
    }

    suspend fun getHandouts(courseId: String): HandoutsModel {
        return client.get("/api/mobile/v1/course_info/$courseId/handouts").body()
    }

    suspend fun getAnnouncements(courseId: String): List<AnnouncementModel> {
        return client.get("/api/mobile/v1/course_info/$courseId/updates").body()
    }

    suspend fun getUserCourses(
        username: String,
        page: Int = 1,
        pageSize: Int = 20,
        status: String? = null,
        fields: List<String> = emptyList(),
    ): CourseEnrollments {
        return client.get("/api/mobile/v4/users/$username/course_enrollments/") {
            parameter("page", page)
            parameter("page_size", pageSize)
            status?.let { parameter("status", it) }
            if (fields.isNotEmpty()) {
                parameter("requested_fields", fields.joinToString(","))
            }
        }.body()
    }

    suspend fun submitOfflineXBlockProgress(
        courseId: String,
        blockId: String,
        progressParts: List<Pair<String, ByteArray>>,
    ) {
        client.post("/courses/$courseId/xblock/$blockId/handler/xmodule_handler/problem_check") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        progressParts.forEach { (name, bytes) ->
                            append(name, bytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"$name\"")
                            })
                        }
                    }
                )
            )
        }
    }

    suspend fun getEnrollmentsStatus(username: String): List<EnrollmentStatus> {
        return client.get("/api/mobile/v1/users/$username/enrollments_status/").body()
    }

    suspend fun getEnrollmentDetails(courseId: String): CourseEnrollmentDetails {
        return client.get("/api/mobile/v1/course_info/$courseId/enrollment_details").body()
    }

    suspend fun getDownloadCoursesPreview(username: String): List<DownloadCoursePreview> {
        return client.get("/api/mobile/v1/download_courses/$username").body()
    }

    suspend fun getUserDates(username: String, page: Int): CourseDatesResponse {
        return client.get("/api/mobile/v1/course_dates/$username/") {
            parameter("page", page)
        }.body()
    }

    suspend fun getCourseProgress(courseId: String): CourseProgressResponse {
        return client.get("/api/course_home/progress/$courseId").body()
    }

    suspend fun shiftAllDueDates() {
        client.post("/api/course_experience/v1/reset_all_relative_course_deadlines/")
    }
}
