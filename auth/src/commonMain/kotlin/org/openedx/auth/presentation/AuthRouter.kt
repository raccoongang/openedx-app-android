package org.openedx.auth.presentation


interface AuthRouter {

    fun navigateToMain(
        fm: Any?,
        courseId: String?,
        infoType: String?,
        openTab: String = ""
    )

    fun navigateToSignIn(fm: Any?, courseId: String?, infoType: String?)

    fun navigateToLogistration(fm: Any?, courseId: String?)

    fun navigateToSignUp(fm: Any?, courseId: String?, infoType: String?)

    fun navigateToRestorePassword(fm: Any?)

    fun navigateToWhatsNew(fm: Any?, courseId: String? = null, infoType: String? = null)

    fun navigateToWebDiscoverCourses(fm: Any?, querySearch: String)

    fun navigateToNativeDiscoverCourses(fm: Any?, querySearch: String)

    fun navigateToWebContent(fm: Any?, title: String, url: String)

    fun clearBackStack(fm: Any?)
}
