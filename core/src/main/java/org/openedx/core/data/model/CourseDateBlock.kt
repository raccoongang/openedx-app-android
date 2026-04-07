package org.openedx.core.data.model

import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize
import org.openedx.core.data.model.room.discovery.CourseDateBlockDb
import org.openedx.core.domain.model.CourseDateBlock
import org.openedx.core.utils.TimeUtils

@Parcelize
data class CourseDateBlock(
    @SerialName("complete")
    val complete: Boolean = false,
    @SerialName("date")
    val date: String = "", // ISO 8601 compliant format
    @SerialName("assignment_type")
    val assignmentType: String? = "",
    @SerialName("date_type")
    val dateType: DateType = DateType.NONE,
    @SerialName("description")
    val description: String = "",
    @SerialName("learner_has_access")
    val learnerHasAccess: Boolean = false,
    @SerialName("link")
    val link: String = "",
    @SerialName("link_text")
    val linkText: String = "",
    @SerialName("title")
    val title: String = "",
    // component blockId in-case of navigating inside the app for component available in mobile
    @SerialName("first_component_block_id")
    val blockId: String = "",
) : Parcelable {
    fun mapToDomain(): CourseDateBlock? {
        TimeUtils.iso8601ToDate(date)?.let {
            return CourseDateBlock(
                complete = complete,
                date = it,
                assignmentType = assignmentType,
                dateType = dateType,
                description = description,
                learnerHasAccess = learnerHasAccess,
                link = link,
                title = title,
                blockId = blockId
            )
        } ?: return null
    }

    fun mapToRoomEntity(): CourseDateBlockDb? {
        TimeUtils.iso8601ToDate(date)?.let {
            return CourseDateBlockDb(
                complete = complete,
                date = it,
                assignmentType = assignmentType,
                dateType = dateType,
                description = description,
                learnerHasAccess = learnerHasAccess,
                link = link,
                title = title,
                blockId = blockId
            )
        } ?: return null
    }
}
