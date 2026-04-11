package org.openedx.discovery.presentation.catalog

/**
 * Common constants for WebView link handling.
 */
object WebViewLink {
    enum class Authority(val key: String) {
        COURSE_INFO("course_info"),
        PROGRAM_INFO("program_info"),
        ENROLL("enroll"),
        ENROLLED_PROGRAM_INFO("enrolled_program_info"),
        ENROLLED_COURSE_INFO("enrolled_course_info"),
        COURSE("course"),
        EXTERNAL("external"),
    }

    object Param {
        const val PATH_ID = "path_id"
        const val COURSE_ID = "course_id"
        const val EMAIL_OPT = "email_opt_in"
        const val PROGRAMS = "programs"
    }
}
