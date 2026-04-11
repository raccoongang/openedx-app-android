package org.openedx.core.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.openedx.core.data.model.DateType

data class CourseDateBlock(
    val title: String = "",
    val description: String = "",
    val link: String = "",
    val blockId: String = "",
    val learnerHasAccess: Boolean = false,
    val complete: Boolean = false,
    val date: Instant,
    val dateType: DateType = DateType.NONE,
    val assignmentType: String? = "",
) {
    fun isCompleted(): Boolean {
        val dateTypeInSet = dateType in setOf(
            DateType.COURSE_START_DATE,
            DateType.COURSE_END_DATE,
            DateType.CERTIFICATE_AVAILABLE_DATE,
            DateType.VERIFIED_UPGRADE_DEADLINE,
            DateType.VERIFICATION_DEADLINE_DATE
        )
        return complete || (dateTypeInSet && date < Clock.System.now())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CourseDateBlock) return false

        if (blockId != other.blockId) return false
        if (date != other.date) return false
        if (assignmentType != other.assignmentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockId.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + (assignmentType?.hashCode() ?: 0)
        return result
    }
}
