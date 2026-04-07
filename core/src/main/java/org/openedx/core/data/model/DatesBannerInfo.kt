package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class DatesBannerInfo(
    @SerialName("missed_deadlines")
    val missedDeadlines: Boolean = false,
    @SerialName("missed_gated_content")
    val missedGatedContent: Boolean = false,
    @SerialName("verified_upgrade_link")
    val verifiedUpgradeLink: String? = "",
    @SerialName("content_type_gating_enabled")
    val contentTypeGatingEnabled: Boolean = false,
)
