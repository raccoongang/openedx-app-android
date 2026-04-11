package org.openedx.core.domain.model

import org.jetbrains.compose.resources.StringResource
import org.openedx.core.Res
import org.openedx.core.core_dates_info_banner_body
import org.openedx.core.core_dates_reset_dates_banner_body
import org.openedx.core.core_dates_reset_dates_banner_button
import org.openedx.core.core_dates_reset_dates_banner_header
import org.openedx.core.core_dates_upgrade_to_graded_banner_body
import org.openedx.core.core_dates_upgrade_to_reset_banner_body

val CourseBannerType.headerRes: StringResource?
    get() = when (this) {
        CourseBannerType.RESET_DATES -> Res.string.core_dates_reset_dates_banner_header
        else -> null
    }

val CourseBannerType.bodyRes: StringResource?
    get() = when (this) {
        CourseBannerType.INFO_BANNER -> Res.string.core_dates_info_banner_body
        CourseBannerType.UPGRADE_TO_GRADED -> Res.string.core_dates_upgrade_to_graded_banner_body
        CourseBannerType.UPGRADE_TO_RESET -> Res.string.core_dates_upgrade_to_reset_banner_body
        CourseBannerType.RESET_DATES -> Res.string.core_dates_reset_dates_banner_body
        else -> null
    }

val CourseBannerType.buttonRes: StringResource?
    get() = when (this) {
        CourseBannerType.RESET_DATES -> Res.string.core_dates_reset_dates_banner_button
        else -> null
    }
