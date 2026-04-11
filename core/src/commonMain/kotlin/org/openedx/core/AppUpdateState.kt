package org.openedx.core

import androidx.compose.runtime.mutableStateOf
import org.openedx.core.system.notifier.app.AppUpgradeEvent

object AppUpdateState {
    var wasUpdateDialogDisplayed = false
    var wasUpgradeDialogClosed = mutableStateOf(false)
    var lastAppUpgradeEvent: AppUpgradeEvent? = null

    data class AppUpgradeParameters(
        val appUpgradeEvent: AppUpgradeEvent? = null,
        val wasUpgradeDialogClosed: Boolean = AppUpdateState.wasUpgradeDialogClosed.value,
        val appUpgradeRecommendedDialog: () -> Unit = {},
        val onAppUpgradeRecommendedBoxClick: () -> Unit = {},
        val onAppUpgradeRequired: () -> Unit = {},
    )
}
