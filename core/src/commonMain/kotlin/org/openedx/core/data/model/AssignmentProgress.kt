package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.AssignmentProgress

private const val DEFAULT_LABEL_LENGTH = 5

@Serializable
data class AssignmentProgress(
    @SerialName("assignment_type")
    val assignmentType: String? = null,
    @SerialName("num_points_earned")
    val numPointsEarned: Float? = null,
    @SerialName("num_points_possible")
    val numPointsPossible: Float? = null,
    @SerialName("short_label")
    val shortLabel: String? = null
) {
    fun mapToDomain(displayName: String) = AssignmentProgress(
        assignmentType = assignmentType,
        numPointsEarned = numPointsEarned ?: 0f,
        numPointsPossible = numPointsPossible ?: 0f,
        shortLabel = shortLabel ?: displayName.take(DEFAULT_LABEL_LENGTH)
    )
}
