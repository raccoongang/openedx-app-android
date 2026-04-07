package org.openedx.profile.presentation

import org.openedx.core.presentation.settings.video.VideoQualityType
import org.openedx.profile.domain.model.Account

interface ProfileRouter {

    fun navigateToEditProfile(fm: Any?, account: Account)

    fun navigateToDeleteAccount(fm: Any?)

    fun navigateToSettings(fm: Any?)

    fun restartApp(fm: Any?, isLogistrationEnabled: Boolean)

    fun navigateToVideoSettings(fm: Any?)

    fun navigateToVideoQuality(fm: Any?, videoQualityType: VideoQualityType)

    fun navigateToWebContent(fm: Any?, title: String, url: String)

    fun navigateToManageAccount(fm: Any?)

    fun navigateToCoursesToSync(fm: Any?)
}
