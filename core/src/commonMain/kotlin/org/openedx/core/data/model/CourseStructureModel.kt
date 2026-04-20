package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.utils.InstantUtils

@Serializable
data class CourseStructureModel(
    @SerialName("root")
    val root: String,
    @SerialName("blocks")
    val blockData: Map<String, Block>,
    @SerialName("id")
    var id: String? = null,
    @SerialName("name")
    var name: String? = null,
    @SerialName("number")
    var number: String? = null,
    @SerialName("org")
    var org: String? = null,
    @SerialName("start")
    var start: String? = null,
    @SerialName("start_display")
    var startDisplay: String? = null,
    @SerialName("start_type")
    var startType: String? = null,
    @SerialName("end")
    var end: String? = null,
    @SerialName("courseware_access")
    var coursewareAccess: CoursewareAccess? = null,
    @SerialName("media")
    var media: Media? = null,
    @SerialName("course_access_details")
    val courseAccessDetails: CourseAccessDetails,
    @SerialName("certificate")
    val certificate: Certificate? = null,
    @SerialName("enrollment_details")
    val enrollmentDetails: EnrollmentDetails,
    @SerialName("is_self_paced")
    var isSelfPaced: Boolean? = null,
    @SerialName("course_progress")
    val progress: Progress? = null,
) {
    fun mapToDomain(): CourseStructure {
        return CourseStructure(
            root = root,
            blockData = blockData.map {
                it.value.mapToDomain(blockData)
            },
            id = id ?: "",
            name = name ?: "",
            number = number ?: "",
            org = org ?: "",
            start = InstantUtils.iso8601ToInstant(start ?: ""),
            startDisplay = startDisplay ?: "",
            startType = startType ?: "",
            end = InstantUtils.iso8601ToInstant(end ?: ""),
            coursewareAccess = coursewareAccess?.mapToDomain(),
            media = media?.mapToDomain(),
            certificate = certificate?.mapToDomain(),
            isSelfPaced = isSelfPaced ?: false,
            progress = progress?.mapToDomain(),
        )
    }
}
