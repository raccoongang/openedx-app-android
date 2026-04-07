package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseDatesBannerInfo

data class CourseDatesBannerInfo(
    @SerialName("dates_banner_info")
    val datesBannerInfo: DatesBannerInfo?,
    @SerialName("has_ended")
    val hasEnded: Boolean?,
) {
    fun mapToDomain(): CourseDatesBannerInfo {
        return CourseDatesBannerInfo(
            missedDeadlines = datesBannerInfo?.missedDeadlines ?: false,
            missedGatedContent = datesBannerInfo?.missedGatedContent ?: false,
            verifiedUpgradeLink = datesBannerInfo?.verifiedUpgradeLink ?: "",
            contentTypeGatingEnabled = datesBannerInfo?.contentTypeGatingEnabled ?: false,
            hasEnded = hasEnded ?: false,
        )
    }
}
