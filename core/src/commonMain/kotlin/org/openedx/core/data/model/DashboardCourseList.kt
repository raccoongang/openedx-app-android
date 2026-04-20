package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.DashboardCourseList

@Serializable
data class DashboardCourseList(
    @SerialName("next")
    val next: String? = null,
    @SerialName("previous")
    val previous: String? = null,
    @SerialName("count")
    val count: Int,
    @SerialName("num_pages")
    val numPages: Int,
    @SerialName("current_page")
    val currentPage: Int,
    @SerialName("results")
    val results: List<EnrolledCourse>
) {

    fun mapToDomain(): DashboardCourseList {
        return DashboardCourseList(
            org.openedx.core.domain.model.Pagination(
                count,
                next ?: "",
                numPages,
                previous ?: ""
            ),
            results.map { it.mapToDomain() }
        )
    }
}
