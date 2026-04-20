package org.openedx.core.presentation.dialog.appreview

import kotlinx.coroutines.flow.StateFlow

interface AppReviewManager {
    var isDialogShowed: Boolean
    val visibleDialog: StateFlow<AppReviewStage?>
    fun tryToOpenRateDialog()
    fun dismiss()
    fun submitRating(rating: Int)
    fun submitFeedback(feedback: String)
    fun openMarketAndDismiss()
}

sealed class AppReviewStage {
    data object Rate : AppReviewStage()
    data class Feedback(val rating: Int) : AppReviewStage()
    data class ThankYou(val positive: Boolean) : AppReviewStage()
}
