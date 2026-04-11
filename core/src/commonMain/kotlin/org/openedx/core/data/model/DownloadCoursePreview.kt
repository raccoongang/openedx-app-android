package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.DownloadCoursePreview as DomainDownloadCoursePreview

@Serializable
data class DownloadCoursePreview(
    @SerialName("course_id")
    val id: String,
    @SerialName("course_name")
    val name: String?,
    @SerialName("course_image")
    val image: String?,
    @SerialName("total_size")
    val totalSize: Long?,
) {
    fun mapToDomain(): DomainDownloadCoursePreview {
        return DomainDownloadCoursePreview(
            id = id,
            name = name ?: "",
            image = image ?: "",
            totalSize = totalSize ?: 0,
        )
    }
}
