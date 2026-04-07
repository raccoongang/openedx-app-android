package org.openedx.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import org.openedx.app.deeplink.HomeTab
import org.openedx.auth.presentation.AuthRouter
import org.openedx.auth.presentation.logistration.LogistrationFragment
import org.openedx.auth.presentation.restore.RestorePasswordFragment
import org.openedx.auth.presentation.signin.SignInFragment
import org.openedx.auth.presentation.signup.SignUpFragment
import org.openedx.core.CalendarRouter
import org.openedx.core.FragmentViewType
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRouter
import org.openedx.core.presentation.global.appupgrade.UpgradeRequiredFragment
import org.openedx.core.presentation.global.webview.WebContentFragment
import org.openedx.core.presentation.settings.video.VideoQualityFragment
import org.openedx.core.presentation.settings.video.VideoQualityType
import org.openedx.course.presentation.CourseRouter
import org.openedx.course.presentation.container.CourseContainerFragment
import org.openedx.course.presentation.container.NoAccessCourseContainerFragment
import org.openedx.course.presentation.handouts.HandoutsType
import org.openedx.course.presentation.handouts.HandoutsWebViewFragment
import org.openedx.course.presentation.section.CourseSectionFragment
import org.openedx.course.presentation.unit.container.CourseUnitContainerFragment
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.course.presentation.unit.video.VideoFullScreenFragment
import org.openedx.course.presentation.unit.video.YoutubeVideoFullScreenFragment
import org.openedx.course.settings.download.DownloadQueueFragment
import org.openedx.courses.presentation.AllEnrolledCoursesFragment
import org.openedx.dashboard.presentation.DashboardRouter
import org.openedx.dates.presentation.DatesRouter
import org.openedx.discovery.presentation.DiscoveryRouter
import org.openedx.discovery.presentation.NativeDiscoveryFragment
import org.openedx.discovery.presentation.WebViewDiscoveryFragment
import org.openedx.discovery.presentation.detail.CourseDetailsFragment
import org.openedx.discovery.presentation.info.CourseInfoFragment
import org.openedx.discovery.presentation.program.ProgramFragment
import org.openedx.discovery.presentation.search.CourseSearchFragment
import org.openedx.discussion.domain.model.DiscussionComment
import org.openedx.discussion.domain.model.Thread
import org.openedx.discussion.presentation.DiscussionRouter
import org.openedx.discussion.presentation.comments.DiscussionCommentsFragment
import org.openedx.discussion.presentation.responses.DiscussionResponsesFragment
import org.openedx.discussion.presentation.search.DiscussionSearchThreadFragment
import org.openedx.discussion.presentation.threads.DiscussionAddThreadFragment
import org.openedx.discussion.presentation.threads.DiscussionThreadsFragment
import org.openedx.downloads.presentation.DownloadsRouter
import org.openedx.profile.domain.model.Account
import org.openedx.profile.presentation.ProfileRouter
import org.openedx.profile.presentation.anothersaccount.AnothersProfileFragment
import org.openedx.profile.presentation.calendar.CalendarFragment
import org.openedx.profile.presentation.calendar.CoursesToSyncFragment
import org.openedx.profile.presentation.delete.DeleteProfileFragment
import org.openedx.profile.presentation.edit.EditProfileFragment
import org.openedx.profile.presentation.manageaccount.ManageAccountFragment
import org.openedx.profile.presentation.profile.ProfileFragment
import org.openedx.profile.presentation.settings.SettingsFragment
import org.openedx.profile.presentation.video.VideoSettingsFragment
import org.openedx.whatsnew.WhatsNewRouter
import org.openedx.whatsnew.presentation.whatsnew.WhatsNewFragment

class AppRouter :
    AuthRouter,
    DiscoveryRouter,
    DashboardRouter,
    CourseRouter,
    DiscussionRouter,
    ProfileRouter,
    AppUpgradeRouter,
    WhatsNewRouter,
    CalendarRouter,
    DownloadsRouter,
    DatesRouter {

    // region AuthRouter
    override fun navigateToMain(
        fm: Any?,
        courseId: String?,
        infoType: String?,
        openTab: String
    ) {
        try {
            (fm as? FragmentManager)?.popBackStack()
            (fm as? FragmentManager)?.beginTransaction()
                ?.replace(R.id.container, MainFragment.newInstance(courseId, infoType, openTab))
                ?.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun navigateToSignIn(fm: Any?, courseId: String?, infoType: String?) {
        replaceFragmentWithBackStack(fm, SignInFragment.newInstance(courseId, infoType))
    }

    override fun navigateToSignUp(fm: Any?, courseId: String?, infoType: String?) {
        replaceFragmentWithBackStack(fm, SignUpFragment.newInstance(courseId, infoType))
    }

    override fun navigateToLogistration(fm: Any?, courseId: String?) {
        replaceFragmentWithBackStack(fm, LogistrationFragment.newInstance(courseId))
    }

    override fun navigateToDownloadQueue(fm: Any?, descendants: List<String>) {
        replaceFragmentWithBackStack(fm, DownloadQueueFragment.newInstance(descendants))
    }

    override fun navigateToRestorePassword(fm: Any?) {
        replaceFragmentWithBackStack(fm, RestorePasswordFragment())
    }

    override fun navigateToNativeDiscoverCourses(fm: Any?, querySearch: String) {
        replaceFragmentWithBackStack(fm, NativeDiscoveryFragment.newInstance(querySearch))
    }

    override fun navigateToWebDiscoverCourses(fm: Any?, querySearch: String) {
        replaceFragmentWithBackStack(fm, WebViewDiscoveryFragment.newInstance(querySearch))
    }

    override fun navigateToWhatsNew(fm: Any?, courseId: String?, infoType: String?) {
        try {
            (fm as? FragmentManager)?.popBackStack()
            (fm as? FragmentManager)?.beginTransaction()
                ?.replace(R.id.container, WhatsNewFragment.newInstance(courseId, infoType))
                ?.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clearBackStack(fm: Any?) {
        (fm as? FragmentManager)?.apply {
            try {
                for (fragment in fragments) {
                    beginTransaction().remove(fragment).commit()
                }
                popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // endregion

    // region DiscoveryRouter
    override fun navigateToCourseDetail(fm: Any?, courseId: String) {
        replaceFragmentWithBackStack(fm, CourseDetailsFragment.newInstance(courseId))
    }

    override fun navigateToCourseSearch(fm: Any?, querySearch: String) {
        replaceFragmentWithBackStack(fm, CourseSearchFragment.newInstance(querySearch))
    }

    override fun navigateToUpgradeRequired(fm: Any?) {
        replaceFragmentWithBackStack(fm, UpgradeRequiredFragment())
    }

    override fun navigateToAllEnrolledCourses(fm: Any?) {
        replaceFragmentWithBackStack(fm, AllEnrolledCoursesFragment())
    }

    override fun getProgramFragment(): Fragment {
        return ProgramFragment.newInstance(isNestedFragment = true)
    }

    override fun navigateToCourseInfo(
        fm: Any?,
        courseId: String,
        infoType: String,
    ) {
        replaceFragmentWithBackStack(fm, CourseInfoFragment.newInstance(courseId, infoType))
    }

    override fun navigateToCourseOutline(
        fm: Any?,
        courseId: String,
        courseTitle: String,
    ) {
        replaceFragmentWithBackStack(
            fm,
            CourseContainerFragment.newInstance(courseId, courseTitle)
        )
    }
    // endregion

    // region DashboardRouter

    override fun navigateToCourseOutline(
        fm: Any?,
        courseId: String,
        courseTitle: String,
        openTab: String,
        resumeBlockId: String,
    ) {
        replaceFragmentWithBackStack(
            fm,
            CourseContainerFragment.newInstance(
                courseId,
                courseTitle,
                openTab,
                resumeBlockId
            )
        )
    }

    override fun navigateToEnrolledProgramInfo(fm: Any?, pathId: String) {
        replaceFragmentWithBackStack(
            fm,
            ProgramFragment.newInstance(pathId = pathId, isNestedFragment = false)
        )
    }

    override fun navigateToNoAccess(
        fm: Any?,
        title: String,
    ) {
        replaceFragment(fm, NoAccessCourseContainerFragment.newInstance(title))
    }
    // endregion

    // region CourseRouter

    override fun navigateToCourseSubsections(
        fm: Any?,
        courseId: String,
        subSectionId: String,
        unitId: String,
        componentId: String,
        mode: CourseViewMode,
    ) {
        replaceFragmentWithBackStack(
            fm,
            CourseSectionFragment.newInstance(
                courseId = courseId,
                subSectionId = subSectionId,
                unitId = unitId,
                componentId = componentId,
                mode = mode
            )
        )
    }

    override fun navigateToCourseContainer(
        fm: Any?,
        courseId: String,
        unitId: String,
        componentId: String,
        mode: CourseViewMode,
    ) {
        replaceFragmentWithBackStack(
            fm,
            CourseUnitContainerFragment.newInstance(
                courseId = courseId,
                unitId = unitId,
                componentId = componentId,
                mode = mode
            )
        )
    }

    override fun replaceCourseContainer(
        fm: Any?,
        courseId: String,
        unitId: String,
        componentId: String,
        mode: CourseViewMode,
    ) {
        replaceFragment(
            fm,
            CourseUnitContainerFragment.newInstance(
                courseId = courseId,
                unitId = unitId,
                componentId = componentId,
                mode = mode
            ),
            FragmentTransaction.TRANSIT_FRAGMENT_FADE
        )
    }

    override fun navigateToFullScreenVideo(
        fm: Any?,
        videoUrl: String,
        videoTime: Long,
        blockId: String,
        courseId: String,
        isPlaying: Boolean,
    ) {
        replaceFragmentWithBackStack(
            fm,
            VideoFullScreenFragment.newInstance(videoUrl, videoTime, blockId, courseId, isPlaying)
        )
    }

    override fun navigateToFullScreenYoutubeVideo(
        fm: Any?,
        videoUrl: String,
        videoTime: Long,
        blockId: String,
        courseId: String,
        isPlaying: Boolean,
    ) {
        replaceFragmentWithBackStack(
            fm,
            YoutubeVideoFullScreenFragment.newInstance(
                videoUrl,
                videoTime,
                blockId,
                courseId,
                isPlaying
            )
        )
    }

    override fun navigateToHandoutsWebView(
        fm: Any?,
        courseId: String,
        type: HandoutsType,
    ) {
        replaceFragmentWithBackStack(
            fm,
            HandoutsWebViewFragment.newInstance(type.name, courseId)
        )
    }
    // endregion

    // region DiscussionRouter
    override fun navigateToDiscussionThread(
        fm: Any?,
        action: String,
        courseId: String,
        topicId: String,
        title: String,
        viewType: FragmentViewType,
    ) {
        replaceFragmentWithBackStack(
            fm,
            DiscussionThreadsFragment.newInstance(action, courseId, topicId, title, viewType.name)
        )
    }

    override fun navigateToDiscussionComments(fm: Any?, thread: Thread) {
        replaceFragmentWithBackStack(
            fm,
            DiscussionCommentsFragment.newInstance(thread)
        )
    }

    override fun navigateToDiscussionResponses(
        fm: Any?,
        comment: DiscussionComment,
        isClosed: Boolean,
    ) {
        replaceFragmentWithBackStack(
            fm,
            DiscussionResponsesFragment.newInstance(comment, isClosed)
        )
    }

    override fun navigateToAddThread(
        fm: Any?,
        topicId: String,
        courseId: String,
    ) {
        replaceFragmentWithBackStack(
            fm,
            DiscussionAddThreadFragment.newInstance(topicId, courseId)
        )
    }

    override fun navigateToSearchThread(fm: Any?, courseId: String) {
        replaceFragmentWithBackStack(
            fm,
            DiscussionSearchThreadFragment.newInstance(courseId)
        )
    }

    override fun navigateToAnothersProfile(
        fm: Any?,
        username: String,
    ) {
        replaceFragmentWithBackStack(
            fm,
            AnothersProfileFragment.newInstance(username)
        )
    }
    // endregion

    // region ProfileRouter
    override fun navigateToEditProfile(fm: Any?, account: Account) {
        replaceFragmentWithBackStack(fm, EditProfileFragment.newInstance(account))
    }

    override fun navigateToDeleteAccount(fm: Any?) {
        replaceFragmentWithBackStack(fm, DeleteProfileFragment())
    }

    override fun navigateToSettings(fm: Any?) {
        replaceFragmentWithBackStack(
            fm,
            SettingsFragment()
        )
    }

    override fun restartApp(fm: Any?, isLogistrationEnabled: Boolean) {
        (fm as? FragmentManager)?.apply {
            clearBackStack(this)
            if (isLogistrationEnabled) {
                replaceFragment(fm, LogistrationFragment())
            } else {
                replaceFragment(fm, SignInFragment.newInstance(null, null))
            }
        }
    }

    override fun navigateToVideoSettings(fm: Any?) {
        replaceFragmentWithBackStack(fm, VideoSettingsFragment())
    }

    override fun navigateToVideoQuality(fm: Any?, videoQualityType: VideoQualityType) {
        replaceFragmentWithBackStack(fm, VideoQualityFragment.newInstance(videoQualityType.name))
    }

    override fun navigateToDiscover(fm: Any?) {
        (fm as? FragmentManager)?.beginTransaction()
            ?.replace(R.id.container, MainFragment.newInstance("", "", HomeTab.DISCOVER.name))
            ?.commit()
    }

    override fun navigateToWebContent(fm: Any?, title: String, url: String) {
        replaceFragmentWithBackStack(
            fm,
            WebContentFragment.newInstance(title = title, url = url)
        )
    }

    override fun navigateToManageAccount(fm: Any?) {
        replaceFragmentWithBackStack(fm, ManageAccountFragment())
    }

    override fun navigateToCalendarSettings(fm: Any?) {
        replaceFragmentWithBackStack(fm, CalendarFragment())
    }

    override fun navigateToCoursesToSync(fm: Any?) {
        replaceFragmentWithBackStack(fm, CoursesToSyncFragment())
    }
    // endregion

    fun getVisibleFragment(fm: Any?): Fragment? {
        val fragmentManager = fm as? FragmentManager ?: return null
        return fragmentManager.fragments.firstOrNull { it.isVisible }
    }

    private fun replaceFragmentWithBackStack(fm: Any?, fragment: Fragment) {
        val fragmentManager = fm as? FragmentManager ?: return
        try {
            fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, fragment.javaClass.simpleName)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun replaceFragment(
        fm: Any?,
        fragment: Fragment,
        transaction: Int = FragmentTransaction.TRANSIT_NONE,
    ) {
        val fragmentManager = fm as? FragmentManager ?: return
        try {
            fragmentManager.beginTransaction()
                .setTransition(transaction)
                .replace(R.id.container, fragment, fragment.javaClass.simpleName)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // App upgrade
    override fun navigateToUserProfile(fm: Any?) {
        try {
            (fm as? FragmentManager)?.popBackStack()
            (fm as? FragmentManager)?.beginTransaction()
                ?.replace(R.id.container, ProfileFragment())
                ?.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // endregion
}
