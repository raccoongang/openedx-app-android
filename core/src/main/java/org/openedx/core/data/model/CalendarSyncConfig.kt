package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseDatesCalendarSync

data class CalendarSyncConfig(
    @SerialName("android")
    val platformConfig: CalendarSyncPlatform = CalendarSyncPlatform(),
) {
    fun mapToDomain(): CourseDatesCalendarSync {
        return CourseDatesCalendarSync(
            isEnabled = platformConfig.enabled,
            isSelfPacedEnabled = platformConfig.selfPacedEnabled,
            isInstructorPacedEnabled = platformConfig.instructorPacedEnabled,
            isDeepLinkEnabled = platformConfig.deepLinksEnabled,
        )
    }
}

data class CalendarSyncPlatform(
    @SerialName("enabled")
    val enabled: Boolean = false,
    @SerialName("self_paced_enabled")
    val selfPacedEnabled: Boolean = false,
    @SerialName("instructor_paced_enabled")
    val instructorPacedEnabled: Boolean = false,
    @SerialName("deep_links_enabled")
    val deepLinksEnabled: Boolean = false,
)
