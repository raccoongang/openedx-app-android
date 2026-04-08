package org.openedx.app

import androidx.fragment.app.Fragment
import org.openedx.auth.presentation.AuthRouter
import org.openedx.core.CalendarRouter
import org.openedx.core.FragmentViewType
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRouter
import org.openedx.core.presentation.settings.video.VideoQualityType
import org.openedx.course.presentation.CourseRouter
import org.openedx.course.presentation.handouts.HandoutsType
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.dashboard.presentation.DashboardRouter
import org.openedx.dates.presentation.DatesRouter
import org.openedx.discovery.presentation.DiscoveryRouter
import org.openedx.discussion.domain.model.DiscussionComment
import org.openedx.discussion.domain.model.Thread
import org.openedx.discussion.presentation.DiscussionRouter
import org.openedx.downloads.presentation.DownloadsRouter
import org.openedx.profile.domain.model.Account
import org.openedx.profile.presentation.ProfileRouter
import org.openedx.whatsnew.WhatsNewRouter

class NoOpRouter : AuthRouter, DiscoveryRouter, DashboardRouter, CourseRouter,
    DiscussionRouter, ProfileRouter, AppUpgradeRouter, WhatsNewRouter,
    CalendarRouter, DownloadsRouter, DatesRouter {
    override fun navigateToMain(fm: Any?, courseId: String?, infoType: String?, openTab: String) {}
    override fun navigateToSignIn(fm: Any?, courseId: String?, infoType: String?) {}
    override fun navigateToSignUp(fm: Any?, courseId: String?, infoType: String?) {}
    override fun navigateToLogistration(fm: Any?, courseId: String?) {}
    override fun navigateToRestorePassword(fm: Any?) {}
    override fun navigateToWhatsNew(fm: Any?, courseId: String?, infoType: String?) {}
    override fun navigateToWebDiscoverCourses(fm: Any?, querySearch: String) {}
    override fun navigateToNativeDiscoverCourses(fm: Any?, querySearch: String) {}
    override fun navigateToWebContent(fm: Any?, title: String, url: String) {}
    override fun clearBackStack(fm: Any?) {}
    override fun navigateToCourseDetail(fm: Any?, courseId: String) {}
    override fun navigateToCourseSearch(fm: Any?, querySearch: String) {}
    override fun navigateToUpgradeRequired(fm: Any?) {}
    override fun navigateToAllEnrolledCourses(fm: Any?) {}
    override fun getProgramFragment(): Fragment = Fragment()
    override fun navigateToCourseInfo(fm: Any?, courseId: String, infoType: String) {}
    override fun navigateToCourseOutline(fm: Any?, courseId: String, courseTitle: String) {}
    override fun navigateToCourseOutline(fm: Any?, courseId: String, courseTitle: String, openTab: String, resumeBlockId: String) {}
    override fun navigateToEnrolledProgramInfo(fm: Any?, pathId: String) {}
    override fun navigateToNoAccess(fm: Any?, title: String) {}
    override fun navigateToCourseSubsections(fm: Any?, courseId: String, subSectionId: String, unitId: String, componentId: String, mode: CourseViewMode) {}
    override fun navigateToCourseContainer(fm: Any?, courseId: String, unitId: String, componentId: String, mode: CourseViewMode) {}
    override fun replaceCourseContainer(fm: Any?, courseId: String, unitId: String, componentId: String, mode: CourseViewMode) {}
    override fun navigateToFullScreenVideo(fm: Any?, videoUrl: String, videoTime: Long, blockId: String, courseId: String, isPlaying: Boolean) {}
    override fun navigateToFullScreenYoutubeVideo(fm: Any?, videoUrl: String, videoTime: Long, blockId: String, courseId: String, isPlaying: Boolean) {}
    override fun navigateToHandoutsWebView(fm: Any?, courseId: String, type: HandoutsType) {}
    override fun navigateToDownloadQueue(fm: Any?, descendants: List<String>) {}
    override fun navigateToDiscussionThread(fm: Any?, action: String, courseId: String, topicId: String, title: String, viewType: FragmentViewType) {}
    override fun navigateToDiscussionComments(fm: Any?, thread: Thread) {}
    override fun navigateToDiscussionResponses(fm: Any?, comment: DiscussionComment, isClosed: Boolean) {}
    override fun navigateToAddThread(fm: Any?, topicId: String, courseId: String) {}
    override fun navigateToSearchThread(fm: Any?, courseId: String) {}
    override fun navigateToAnothersProfile(fm: Any?, username: String) {}
    override fun navigateToEditProfile(fm: Any?, account: Account) {}
    override fun navigateToDeleteAccount(fm: Any?) {}
    override fun navigateToSettings(fm: Any?) {}
    override fun restartApp(fm: Any?, isLogistrationEnabled: Boolean) {}
    override fun navigateToVideoSettings(fm: Any?) {}
    override fun navigateToVideoQuality(fm: Any?, videoQualityType: VideoQualityType) {}
    override fun navigateToDiscover(fm: Any?) {}
    override fun navigateToManageAccount(fm: Any?) {}
    override fun navigateToCalendarSettings(fm: Any?) {}
    override fun navigateToCoursesToSync(fm: Any?) {}
    override fun navigateToUserProfile(fm: Any?) {}
}
