package org.openedx.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AppConfig(
    val courseDatesCalendarSync: CourseDatesCalendarSync = CourseDatesCalendarSync(),
)

data class CourseDatesCalendarSync(
    @SerialName("is_enabled")
    val isEnabled: Boolean = false,
    @SerialName("is_self_paced_enabled")
    val isSelfPacedEnabled: Boolean = false,
    @SerialName("is_instructor_paced_enabled")
    val isInstructorPacedEnabled: Boolean = false,
    @SerialName("is_deep_link_enabled")
    val isDeepLinkEnabled: Boolean = false,
)
