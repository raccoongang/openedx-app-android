package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.room.BlockDb
import org.openedx.core.data.model.room.CourseStructureEntity
import org.openedx.core.data.model.room.MediaDb
import org.openedx.core.data.model.room.discovery.ProgressDb
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.utils.TimeUtils

data class CourseStructureModel(
    @SerialName("root")
    val root: String,
    @SerialName("blocks")
    val blockData: Map<String, Block>,
    @SerialName("id")
    var id: String?,
    @SerialName("name")
    var name: String?,
    @SerialName("number")
    var number: String?,
    @SerialName("org")
    var org: String?,
    @SerialName("start")
    var start: String?,
    @SerialName("start_display")
    var startDisplay: String?,
    @SerialName("start_type")
    var startType: String?,
    @SerialName("end")
    var end: String?,
    @SerialName("courseware_access")
    var coursewareAccess: CoursewareAccess?,
    @SerialName("media")
    var media: Media?,
    @SerialName("course_access_details")
    val courseAccessDetails: CourseAccessDetails,
    @SerialName("certificate")
    val certificate: Certificate?,
    @SerialName("enrollment_details")
    val enrollmentDetails: EnrollmentDetails,
    @SerialName("is_self_paced")
    var isSelfPaced: Boolean?,
    @SerialName("course_progress")
    val progress: Progress?,
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
            start = TimeUtils.iso8601ToDate(start ?: ""),
            startDisplay = startDisplay ?: "",
            startType = startType ?: "",
            end = TimeUtils.iso8601ToDate(end ?: ""),
            coursewareAccess = coursewareAccess?.mapToDomain(),
            media = media?.mapToDomain(),
            certificate = certificate?.mapToDomain(),
            isSelfPaced = isSelfPaced ?: false,
            progress = progress?.mapToDomain(),
        )
    }

    fun mapToRoomEntity(): CourseStructureEntity {
        return CourseStructureEntity(
            root,
            blocks = blockData.map { BlockDb.createFrom(it.value) },
            id = id ?: "",
            name = name ?: "",
            number = number ?: "",
            org = org ?: "",
            start = start ?: "",
            startDisplay = startDisplay ?: "",
            startType = startType ?: "",
            end = end ?: "",
            coursewareAccess = coursewareAccess?.mapToRoomEntity(),
            media = MediaDb.createFrom(media),
            certificate = certificate?.mapToRoomEntity(),
            isSelfPaced = isSelfPaced ?: false,
            progress = progress?.mapToRoomEntity() ?: ProgressDb.DEFAULT_PROGRESS,
        )
    }
}
