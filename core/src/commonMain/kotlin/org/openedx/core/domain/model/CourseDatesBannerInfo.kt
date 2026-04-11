package org.openedx.core.domain.model

import org.openedx.core.domain.model.CourseBannerType.BLANK
import org.openedx.core.domain.model.CourseBannerType.INFO_BANNER
import org.openedx.core.domain.model.CourseBannerType.RESET_DATES
import org.openedx.core.domain.model.CourseBannerType.UPGRADE_TO_GRADED
import org.openedx.core.domain.model.CourseBannerType.UPGRADE_TO_RESET

data class CourseDatesBannerInfo(
    private val missedDeadlines: Boolean,
    private val missedGatedContent: Boolean,
    private val verifiedUpgradeLink: String,
    private val contentTypeGatingEnabled: Boolean,
    private val hasEnded: Boolean,
) {
    val bannerType by lazy { getCourseBannerType() }

    fun isBannerAvailableForUserType(isSelfPaced: Boolean): Boolean {
        if (hasEnded) return false

        val selfPacedAvailable = isSelfPaced && bannerType != BLANK
        val instructorPacedAvailable = !isSelfPaced && bannerType == UPGRADE_TO_GRADED

        return selfPacedAvailable || instructorPacedAvailable
    }

    fun isBannerAvailableForDashboard(): Boolean {
        return hasEnded.not() && bannerType == RESET_DATES
    }

    private fun getCourseBannerType(): CourseBannerType = when {
        canUpgradeToGraded() -> UPGRADE_TO_GRADED
        canUpgradeToReset() -> UPGRADE_TO_RESET
        canResetDates() -> RESET_DATES
        infoBanner() -> INFO_BANNER
        else -> BLANK
    }

    private fun infoBanner(): Boolean = !missedDeadlines

    private fun canUpgradeToGraded(): Boolean = contentTypeGatingEnabled && !missedDeadlines

    private fun canUpgradeToReset(): Boolean =
        !canUpgradeToGraded() && missedDeadlines && missedGatedContent

    private fun canResetDates(): Boolean =
        !canUpgradeToGraded() && missedDeadlines && !missedGatedContent
}

enum class CourseBannerType {
    BLANK,
    INFO_BANNER,
    UPGRADE_TO_GRADED,
    UPGRADE_TO_RESET,
    RESET_DATES,
}
