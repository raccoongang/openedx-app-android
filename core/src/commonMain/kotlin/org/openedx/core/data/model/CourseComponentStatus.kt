package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseComponentStatus

@Serializable
data class CourseComponentStatus(
    @SerialName("last_visited_block_id")
    var lastVisitedBlockId: String? = null,
) {

    fun mapToDomain(): CourseComponentStatus {
        return CourseComponentStatus(
            lastVisitedBlockId = lastVisitedBlockId ?: ""
        )
    }
}
