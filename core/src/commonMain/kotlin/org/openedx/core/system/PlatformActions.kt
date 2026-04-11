package org.openedx.core.system

interface PlatformActions {
    fun showFeedbackScreen(feedbackEmailAddress: String, subject: String, appVersion: String)
    fun openAppInMarket()
}
