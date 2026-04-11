package org.openedx.core.presentation.dialog.appreview

interface AppReviewManager {
    var isDialogShowed: Boolean
    fun tryToOpenRateDialog()
}
