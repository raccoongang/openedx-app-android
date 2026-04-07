package org.openedx.whatsnew


interface WhatsNewRouter {
    fun navigateToMain(
        fm: Any?,
        courseId: String?,
        infoType: String?,
        openTab: String
    )
}
