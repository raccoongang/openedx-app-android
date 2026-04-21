package org.openedx.shared.analytics

import org.openedx.core.presentation.CoreAnalytics
import org.openedx.core.presentation.DownloadsAnalytics
import org.openedx.core.presentation.dialog.appreview.AppReviewAnalytics
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.dashboard.presentation.DashboardAnalytics
import org.openedx.dates.presentation.DatesAnalytics
import org.openedx.discovery.presentation.DiscoveryAnalytics
import org.openedx.discussion.presentation.DiscussionAnalytics
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.whatsnew.presentation.WhatsNewAnalytics

/**
 * iOS no-op analytics stubs.
 *
 * TODO iOS: when Firebase / Braze / Segment iOS SDKs land in the CMP migration,
 * replace these with real impls (mirroring Android's AnalyticsManager dispatch).
 */
class IosCoreAnalytics : CoreAnalytics {
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
}

class IosAppReviewAnalytics : AppReviewAnalytics {
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
}

class IosDashboardAnalytics : DashboardAnalytics {
    override fun logScreenEvent(screenName: String, params: Map<String, Any?>) = Unit
    override fun dashboardCourseClickedEvent(courseId: String, courseName: String) = Unit
}

class IosDiscoveryAnalytics : DiscoveryAnalytics {
    override fun discoverySearchBarClickedEvent() = Unit
    override fun discoveryCourseSearchEvent(label: String, coursesCount: Int) = Unit
    override fun discoveryCourseClickedEvent(courseId: String, courseName: String) = Unit
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
    override fun logScreenEvent(screenName: String, params: Map<String, Any?>) = Unit
}

class IosDownloadsAnalytics : DownloadsAnalytics {
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
    override fun logScreenEvent(screenName: String, params: Map<String, Any?>) = Unit
}

class IosDatesAnalytics : DatesAnalytics {
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
}

class IosProfileAnalytics : ProfileAnalytics {
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
    override fun logScreenEvent(screenName: String, params: Map<String, Any?>) = Unit
}

class IosCourseAnalytics : CourseAnalytics {
    override fun sequentialClickedEvent(courseId: String, courseName: String, blockId: String, blockName: String) = Unit
    override fun nextBlockClickedEvent(courseId: String, courseName: String, blockId: String, blockName: String) = Unit
    override fun prevBlockClickedEvent(courseId: String, courseName: String, blockId: String, blockName: String) = Unit
    override fun finishVerticalClickedEvent(courseId: String, courseName: String, blockId: String, blockName: String) = Unit
    override fun finishVerticalNextClickedEvent(courseId: String, courseName: String, blockId: String, blockName: String) = Unit
    override fun finishVerticalBackClickedEvent(courseId: String, courseName: String) = Unit
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
    override fun logScreenEvent(screenName: String, params: Map<String, Any?>) = Unit
}

class IosDiscussionAnalytics : DiscussionAnalytics {
    override fun discussionAllPostsClickedEvent(courseId: String, courseName: String) = Unit
    override fun discussionFollowingClickedEvent(courseId: String, courseName: String) = Unit
    override fun discussionTopicClickedEvent(courseId: String, courseName: String, topicId: String, topicName: String) = Unit
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
}

class IosWhatsNewAnalytics : WhatsNewAnalytics {
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
}
