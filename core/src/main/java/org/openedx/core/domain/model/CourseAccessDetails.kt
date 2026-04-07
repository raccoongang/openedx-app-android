package org.openedx.core.domain.model

import com.google.gson.internal.bind.util.ISO8601Utils
import org.openedx.core.data.model.room.discovery.CourseAccessDetailsDb
import java.util.Date

data class CourseAccessDetails(
    val hasUnmetPrerequisites: Boolean,
    val isTooEarly: Boolean,
    val isStaff: Boolean,
    val auditAccessExpires: Date?,
    val coursewareAccess: CoursewareAccess?,
) {

    fun mapToRoomEntity(): CourseAccessDetailsDb =
        CourseAccessDetailsDb(
            hasUnmetPrerequisites = hasUnmetPrerequisites,
            isTooEarly = isTooEarly,
            isStaff = isStaff,
            auditAccessExpires = auditAccessExpires?.let { ISO8601Utils.format(it) },
            coursewareAccess = coursewareAccess?.mapToEntity()
        )
}
