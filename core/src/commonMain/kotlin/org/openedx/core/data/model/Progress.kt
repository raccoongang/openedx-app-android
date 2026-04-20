package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.Progress

@Serializable
data class Progress(
    @SerialName("assignments_completed")
    val assignmentsCompleted: Int? = null,
    @SerialName("total_assignments_count")
    val totalAssignmentsCount: Int? = null,
) {
    fun mapToDomain() = Progress(
        completed = assignmentsCompleted ?: 0,
        total = totalAssignmentsCount ?: 0
    )
}
