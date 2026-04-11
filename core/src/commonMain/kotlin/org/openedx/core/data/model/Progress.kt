package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.Progress

@Serializable
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
}
