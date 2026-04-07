package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class UIConfig(
    @SerialName("COURSE_DROPDOWN_NAVIGATION_ENABLED")
    val isCourseDropdownNavigationEnabled: Boolean = false,
    @SerialName("COURSE_UNIT_PROGRESS_ENABLED")
    val isCourseUnitProgressEnabled: Boolean = false,
    @SerialName("COURSE_DOWNLOAD_QUEUE_SCREEN")
    val isCourseDownloadQueueEnabled: Boolean = false,
)
