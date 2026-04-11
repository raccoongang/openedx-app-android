package org.openedx.app

import android.content.Context
import org.openedx.core.AppUpdateState
import org.openedx.core.openPlayMarket
import org.openedx.core.system.PlatformActions
import org.openedx.core.utils.EmailUtil

class PlatformActionsImpl(private val context: Context) : PlatformActions {
    override fun showFeedbackScreen(
        feedbackEmailAddress: String,
        subject: String,
        appVersion: String,
    ) {
        EmailUtil.showFeedbackScreen(context, feedbackEmailAddress, subject, appVersion = appVersion)
    }

    override fun openAppInMarket() {
        AppUpdateState.openPlayMarket(context)
    }
}
