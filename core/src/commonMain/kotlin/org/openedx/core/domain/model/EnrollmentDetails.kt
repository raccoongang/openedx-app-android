package org.openedx.core.domain.model

import java.util.Date

data class EnrollmentDetails(
    val created: Date?,
    val mode: String?,
    val isActive: Boolean,
    val upgradeDeadline: Date?,
)
