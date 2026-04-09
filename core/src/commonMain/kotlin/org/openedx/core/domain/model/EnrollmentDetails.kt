package org.openedx.core.domain.model

import kotlinx.datetime.Instant

data class EnrollmentDetails(
    val created: Instant?,
    val mode: String?,
    val isActive: Boolean,
    val upgradeDeadline: Instant?,
)
