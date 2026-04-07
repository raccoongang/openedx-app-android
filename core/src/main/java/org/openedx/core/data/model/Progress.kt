package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.room.discovery.ProgressDb
import org.openedx.core.domain.model.Progress

data class Progress(
    @SerialName("assignments_completed")
    val assignmentsCompleted: Int?,
    @SerialName("total_assignments_count")
    val totalAssignmentsCount: Int?,
) {
    fun mapToDomain() = Progress(
        completed = assignmentsCompleted ?: 0,
        total = totalAssignmentsCount ?: 0
    )

    fun mapToRoomEntity() = ProgressDb(
        assignmentsCompleted = assignmentsCompleted ?: 0,
        totalAssignmentsCount = totalAssignmentsCount ?: 0
    )
}
