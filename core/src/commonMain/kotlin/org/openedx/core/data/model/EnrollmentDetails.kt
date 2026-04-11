package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.utils.InstantUtils
import org.openedx.core.domain.model.EnrollmentDetails as DomainEnrollmentDetails

@Serializable
data class EnrollmentDetails(
    @SerialName("created")
    var created: String?,
    @SerialName("date")
    val date: String?,
    @SerialName("mode")
    val mode: String?,
    @SerialName("is_active")
    val isActive: Boolean = false,
    @SerialName("upgrade_deadline")
    val upgradeDeadline: String?,
) {
    fun mapToDomain() = DomainEnrollmentDetails(
        created = InstantUtils.iso8601ToInstant(date ?: ""),
        mode = mode,
        isActive = isActive,
        upgradeDeadline = InstantUtils.iso8601ToInstant(upgradeDeadline ?: ""),
    )
}
