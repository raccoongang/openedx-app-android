package org.openedx.core.domain.model

import org.openedx.core.extension.safeDivBy

data class AssignmentProgress(
    val assignmentType: String?,
    val numPointsEarned: Float,
    val numPointsPossible: Float,
    val shortLabel: String
) {

    val value: Float = numPointsEarned.safeDivBy(numPointsPossible)

    fun toPointString(separator: String = ""): String {
        return "${numPointsEarned.toInt()}$separator/$separator${numPointsPossible.toInt()}"
    }

    val label = shortLabel
        .replace(" ", "")
        .replaceFirst(Regex("^(\\D+)(0*)(\\d+)$"), "$1$3")
}
