package org.openedx.shared.stubs

import org.openedx.app.AppAnalytics
import org.openedx.core.data.storage.InAppReviewPreferences
import org.openedx.core.module.TranscriptProvider
import org.openedx.core.module.TranscriptResult
import org.openedx.course.data.storage.CoursePreferences
import org.openedx.core.system.PlatformActions
import org.openedx.whatsnew.WhatsNewManager
import org.openedx.whatsnew.data.storage.WhatsNewPreferences
import org.openedx.whatsnew.domain.model.WhatsNewItem
import platform.Foundation.NSUserDefaults

/**
 * iOS stubs for the remaining interfaces that don't have real iOS impls yet.
 * Grouped here so they're easy to find and replace incrementally.
 */

class IosAppAnalytics : AppAnalytics {
    override fun logoutEvent(force: Boolean) = Unit
    override fun setUserIdForSession(userId: Long) = Unit
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
    override fun logScreenEvent(screenName: String, params: Map<String, Any?>) = Unit
}

class IosPlatformActions : PlatformActions {
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    override fun openLink(url: String) {
        val nsUrl = platform.Foundation.NSURL.URLWithString(url) ?: return
        platform.UIKit.UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any>()) { _ -> }
    }
    override fun sendEmailIntent(to: String, subject: String, body: String) {
        openLink("mailto:$to?subject=$subject&body=$body")
    }
    override fun showFeedbackScreen(feedbackEmailAddress: String, subject: String, appVersion: String) {
        sendEmailIntent(feedbackEmailAddress, "Feedback - OpenEdX iOS $appVersion", "")
    }
    override fun openAppInMarket() = Unit
}

class IosWhatsNewManager : WhatsNewManager {
    override fun getNewestData(): WhatsNewItem? = null
    override fun shouldShowWhatsNew(): Boolean = false
}

class IosWhatsNewPreferences : WhatsNewPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults
    override var lastWhatsNewVersion: String
        get() = defaults.stringForKey("last_whats_new_version") ?: ""
        set(value) = defaults.setObject(value, "last_whats_new_version")
}

class IosCoursePreferences : CoursePreferences {
    private val defaults = NSUserDefaults.standardUserDefaults
    override fun setCalendarSyncEventsDialogShown(courseName: String) {
        defaults.setBool(true, "calendar_sync_dialog_$courseName")
    }
    override fun isCalendarSyncEventsDialogShown(courseName: String): Boolean {
        return defaults.boolForKey("calendar_sync_dialog_$courseName")
    }
}

class IosTranscriptProvider : TranscriptProvider {
    override suspend fun downloadTranscripts(url: String): TranscriptResult? = null
    override suspend fun cancelDownloading() = Unit
}

class IosInAppReviewPreferences : InAppReviewPreferences {
    override var lastReviewVersion: InAppReviewPreferences.VersionName =
        InAppReviewPreferences.VersionName.default
    override var wasPositiveRated: Boolean = false
}
