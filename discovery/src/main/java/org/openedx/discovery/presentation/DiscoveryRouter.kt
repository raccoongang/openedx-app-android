package org.openedx.discovery.presentation


interface DiscoveryRouter {

    fun navigateToCourseOutline(
        fm: Any?,
        courseId: String,
        courseTitle: String,
    )

    fun navigateToLogistration(fm: Any?, courseId: String?)

    fun navigateToCourseDetail(fm: Any?, courseId: String)

    fun navigateToCourseSearch(fm: Any?, querySearch: String)

    fun navigateToUpgradeRequired(fm: Any?)

    fun navigateToCourseInfo(fm: Any?, courseId: String, infoType: String)

    fun navigateToSignUp(fm: Any?, courseId: String? = null, infoType: String? = null)

    fun navigateToSignIn(fm: Any?, courseId: String?, infoType: String?)

    fun navigateToSettings(fm: Any?)

    fun navigateToEnrolledProgramInfo(fm: Any?, pathId: String)
}
