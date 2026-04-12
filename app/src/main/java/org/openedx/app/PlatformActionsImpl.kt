package org.openedx.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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

    override fun openLink(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
        }
    }

    override fun sendEmailIntent(to: String, subject: String, body: String) {
        EmailUtil.sendEmailIntent(context, to, subject, body)
    }
}
