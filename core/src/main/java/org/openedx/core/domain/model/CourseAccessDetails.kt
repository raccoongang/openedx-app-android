package org.openedx.core.domain.model

import org.openedx.core.data.model.room.discovery.CourseAccessDetailsDb
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
            auditAccessExpires = auditAccessExpires?.let {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(it)
            },
            coursewareAccess = coursewareAccess?.mapToEntity()
        )
}
