package org.openedx.core.presentation.dialog.appreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.Res
import org.openedx.core.core_thank_you_dialog_negative_description
import org.openedx.core.core_thank_you_dialog_positive_description

private const val NEGATIVE_AUTO_DISMISS_MILLIS = 3_000L

@Composable
fun AppReviewHost(manager: AppReviewManager) {
    val stage by manager.visibleDialog.collectAsState()
    when (val current = stage) {
        null -> Unit

        AppReviewStage.Rate -> {
            val rating = remember { mutableIntStateOf(0) }
            RateDialog(
                rating = rating,
                onNotNowClick = { manager.dismiss() },
                onSubmitClick = { manager.submitRating(rating.intValue) },
            )
        }

        is AppReviewStage.Feedback -> {
            val feedback = remember { mutableStateOf("") }
            FeedbackDialog(
                feedback = feedback,
                onNotNowClick = { manager.dismiss() },
                onShareClick = { manager.submitFeedback(feedback.value) },
            )
        }

        is AppReviewStage.ThankYou -> {
            val description = if (current.positive) {
                stringResource(Res.string.core_thank_you_dialog_positive_description)
            } else {
                stringResource(Res.string.core_thank_you_dialog_negative_description)
            }
            if (!current.positive) {
                LaunchedEffect(current) {
                    delay(NEGATIVE_AUTO_DISMISS_MILLIS)
                    manager.dismiss()
                }
            }
            ThankYouDialog(
                description = description,
                showButtons = current.positive,
                onNotNowClick = { manager.dismiss() },
                onRateUsClick = { manager.openMarketAndDismiss() },
            )
        }
    }
}
