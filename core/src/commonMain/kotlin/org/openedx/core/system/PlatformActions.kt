package org.openedx.core.system

interface PlatformActions {
    fun showFeedbackScreen(feedbackEmailAddress: String, subject: String, appVersion: String)
    fun openAppInMarket()
    fun openLink(url: String)
    fun sendEmailIntent(to: String, subject: String, body: String)
}
