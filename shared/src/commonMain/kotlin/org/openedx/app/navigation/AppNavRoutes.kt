package org.openedx.app.navigation

import kotlinx.serialization.Serializable

/**
 * Top-level navigation routes for the app.
 * These define all possible navigation destinations.
 */
object AppNavRoutes {

    // Auth flow
    @Serializable
    data class SignIn(
        val courseId: String? = null,
        val infoType: String? = null,
        val authCode: String = "",
    )

    @Serializable
    data class SignUp(
        val courseId: String? = null,
        val infoType: String? = null,
    )

    @Serializable
    data class Logistration(val courseId: String? = null)

    @Serializable
    object RestorePassword

    @Serializable
    data class WhatsNew(
        val courseId: String? = null,
        val infoType: String? = null,
    )

    // Main screen (after login)
    @Serializable
    data class Main(
        val courseId: String? = null,
        val infoType: String? = null,
        val openTab: String = "LEARN",
    )

    // Discovery
    @Serializable
    data class NativeDiscovery(val querySearch: String = "")

    @Serializable
    data class WebViewDiscovery(val querySearch: String = "")

    @Serializable
    data class CourseDetails(val courseId: String)

    @Serializable
    data class CourseSearch(val querySearch: String = "")

    @Serializable
    data class CourseInfo(val courseId: String, val infoType: String)

    @Serializable
    data class Program(val pathId: String = "", val isNestedFragment: Boolean = false)

    // Course
    @Serializable
    data class CourseContainer(
        val courseId: String,
        val courseTitle: String,
        val openTab: String = "COURSE",
        val resumeBlockId: String = "",
    )

    @Serializable
    data class NoAccessCourseContainer(val title: String)

    @Serializable
    data class CourseSection(
        val courseId: String,
        val subSectionId: String,
        val unitId: String = "",
        val componentId: String = "",
        val mode: String = "FULL",
    )

    @Serializable
    data class CourseContentAll(
        val courseId: String,
        val courseTitle: String,
        val initialTab: String = "ALL",
    )

    @Serializable
    data class CourseProgress(val courseId: String)

    @Serializable
    data class CourseDates(val courseId: String, val enrollmentMode: String = "")

    @Serializable
    data class DiscussionTopics(val courseId: String, val courseTitle: String)

    @Serializable
    data class CourseUnitContainer(
        val courseId: String,
        val unitId: String,
        val componentId: String = "",
        val mode: String = "FULL",
    )

    @Serializable
    data class VideoFullScreen(
        val videoUrl: String,
        val videoTime: Long,
        val blockId: String,
        val courseId: String,
        val isPlaying: Boolean,
    )

    @Serializable
    data class YoutubeVideoFullScreen(
        val videoUrl: String,
        val videoTime: Long,
        val blockId: String,
        val courseId: String,
        val isPlaying: Boolean,
    )

    @Serializable
    data class HandoutsWebView(val courseId: String, val type: String)

    @Serializable
    data class DownloadQueue(val descendants: List<String>)

    // Discussion
    @Serializable
    data class DiscussionThreads(
        val action: String,
        val courseId: String,
        val topicId: String,
        val title: String,
        val viewType: String,
    )

    @Serializable
    data class DiscussionComments(val threadJson: String)

    @Serializable
    data class DiscussionResponses(val commentJson: String, val isClosed: Boolean)

    @Serializable
    data class DiscussionAddThread(val topicId: String, val courseId: String)

    @Serializable
    data class DiscussionSearchThread(val courseId: String)

    // Profile
    @Serializable
    data class EditProfile(val accountJson: String)

    @Serializable
    object DeleteProfile

    @Serializable
    object Settings

    @Serializable
    object VideoSettings

    @Serializable
    data class VideoQuality(val videoQualityType: String)

    @Serializable
    data class WebContent(val title: String, val url: String)

    @Serializable
    object ManageAccount

    @Serializable
    object CalendarSettings

    @Serializable
    object CoursesToSync

    @Serializable
    data class AnothersProfile(val username: String)

    // Other
    @Serializable
    object UpgradeRequired

    @Serializable
    object AllEnrolledCourses

    @Serializable
    object UserProfile
}
