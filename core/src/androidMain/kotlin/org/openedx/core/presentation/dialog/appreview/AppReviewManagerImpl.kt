package org.openedx.core.presentation.dialog.appreview

import androidx.appcompat.app.AppCompatActivity
import org.openedx.core.data.storage.InAppReviewPreferences
import org.openedx.core.presentation.global.AppData

class AppReviewManagerImpl(
    private val activity: AppCompatActivity,
    private val reviewPreferences: InAppReviewPreferences,
    private val appData: AppData
) : AppReviewManager {
    override var isDialogShowed = false

    override fun tryToOpenRateDialog() {
        if (!activity.isDestroyed) {
            isDialogShowed = true
            val currentVersionName = reviewPreferences.formatVersionName(appData.versionName)
            val minorVersionPassed =
                currentVersionName.minorVersion - 2 >= reviewPreferences.lastReviewVersion.minorVersion
            val majorVersionPassed =
                currentVersionName.majorVersion - 1 >= reviewPreferences.lastReviewVersion.majorVersion
            if (!reviewPreferences.wasPositiveRated && (minorVersionPassed || majorVersionPassed)) {
                // TODO: Show Compose dialog for app rating
            }
        }
    }
}
