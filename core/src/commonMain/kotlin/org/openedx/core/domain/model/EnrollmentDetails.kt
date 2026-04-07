package org.openedx.core.domain.model

import org.openedx.core.data.model.room.discovery.EnrollmentDetailsDB
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class EnrollmentDetails(
    val created: Date?,
    val mode: String?,
    val isActive: Boolean,
    val upgradeDeadline: Date?,
) {
    private fun formatIso8601(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(date)
    }

    fun mapToEntity() = EnrollmentDetailsDB(
        created = created?.let { formatIso8601(it) },
        mode = mode,
        isActive = isActive,
        upgradeDeadline = upgradeDeadline?.let { formatIso8601(it) }
    )
}
