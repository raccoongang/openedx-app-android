package org.openedx.core.presentation.dialog.appreview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.openedx.core.config.Config
import org.openedx.core.data.storage.InAppReviewPreferences
import org.openedx.core.presentation.global.AppData
import org.openedx.core.system.PlatformActions

class AppReviewManagerImpl(
    private val reviewPreferences: InAppReviewPreferences,
    private val appData: AppData,
    private val config: Config,
    private val platformActions: PlatformActions,
    private val analytics: AppReviewAnalytics,
) : AppReviewManager {
    override var isDialogShowed = false

    private val _visibleDialog = MutableStateFlow<AppReviewStage?>(null)
    override val visibleDialog: StateFlow<AppReviewStage?> = _visibleDialog.asStateFlow()

    private var pendingRating: Int = 0

    override fun tryToOpenRateDialog() {
        if (isDialogShowed) return
        val currentVersionName = reviewPreferences.formatVersionName(appData.versionName)
        val minorVersionPassed =
            currentVersionName.minorVersion - 2 >= reviewPreferences.lastReviewVersion.minorVersion
        val majorVersionPassed =
            currentVersionName.majorVersion - 1 >= reviewPreferences.lastReviewVersion.majorVersion
        if (!reviewPreferences.wasPositiveRated && (minorVersionPassed || majorVersionPassed)) {
            isDialogShowed = true
            _visibleDialog.value = AppReviewStage.Rate
            logRatingDialogShowed()
        }
    }

    override fun submitRating(rating: Int) {
        pendingRating = rating
        reviewPreferences.setVersion(appData.versionName)
        _visibleDialog.value = if (rating >= POSITIVE_THRESHOLD) {
            reviewPreferences.wasPositiveRated = true
            AppReviewStage.ThankYou(positive = true)
        } else {
            AppReviewStage.Feedback(rating = rating)
        }
    }

    override fun submitFeedback(feedback: String) {
        logDialogAction(AppReviewAnalyticsKey.SHARE_FEEDBACK.key)
        if (feedback.isNotBlank()) {
            val subject = "${appData.appName} $RATING_FEEDBACK_SUBJECT_SUFFIX"
            val body = "$FEEDBACK_RATING_PREFIX $pendingRating\n\n$feedback"
            platformActions.sendEmailIntent(
                to = config.getFeedbackEmailAddress(),
                subject = subject,
                body = body,
            )
        }
        _visibleDialog.value = AppReviewStage.ThankYou(positive = false)
    }

    private fun logRatingDialogShowed() {
        analytics.logEvent(
            event = AppReviewAnalyticsEvent.RATING_DIALOG.eventName,
            params = buildMap {
                put(AppReviewAnalyticsKey.NAME.key, AppReviewAnalyticsEvent.RATING_DIALOG.biValue)
                put(AppReviewAnalyticsKey.CATEGORY.key, AppReviewAnalyticsKey.APP_REVIEWS.key)
            }
        )
    }

    private fun logDialogAction(action: String, rating: Int = 0) {
        analytics.logEvent(
            event = AppReviewAnalyticsEvent.RATING_DIALOG_ACTION.eventName,
            params = buildMap {
                put(
                    AppReviewAnalyticsKey.NAME.key,
                    AppReviewAnalyticsEvent.RATING_DIALOG_ACTION.biValue
                )
                put(AppReviewAnalyticsKey.CATEGORY.key, AppReviewAnalyticsKey.APP_REVIEWS.key)
                put(AppReviewAnalyticsKey.ACTION.key, action)
                if (rating != 0) put(AppReviewAnalyticsKey.RATING.key, rating)
            }
        )
    }

    override fun openMarketAndDismiss() {
        platformActions.openAppInMarket()
        dismiss()
    }

    override fun dismiss() {
        _visibleDialog.value = null
    }

    companion object {
        private const val POSITIVE_THRESHOLD = 4
        private const val RATING_FEEDBACK_SUBJECT_SUFFIX = "Rating Feedback"
        private const val FEEDBACK_RATING_PREFIX = "Rating:"
    }
}
