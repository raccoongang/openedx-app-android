package org.openedx.app.deeplink

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.openedx.app.navigation.AppNavRoutes
import org.openedx.core.FragmentViewType
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.course.domain.interactor.CourseInteractor
import org.openedx.course.presentation.handouts.HandoutsType
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.catalog.WebViewLink
import org.openedx.discussion.domain.interactor.DiscussionInteractor
import org.openedx.discussion.presentation.topics.DiscussionTopicsViewModel
import kotlin.coroutines.CoroutineContext

class DeepLinkRouter(
    private val config: Config,
    private val corePreferences: CorePreferences,
    private val discoveryInteractor: DiscoveryInteractor,
    private val courseInteractor: CourseInteractor,
    private val discussionInteractor: DiscussionInteractor
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private val isUserLoggedIn
        get() = corePreferences.user != null

    private val json = Json { ignoreUnknownKeys = true }

    fun makeRoute(navController: NavController?, deepLink: DeepLink) {
        val nav = navController ?: return
        when (deepLink.type) {
            DeepLinkType.DISCOVERY -> navigateToDiscovery(nav)
            DeepLinkType.DISCOVERY_COURSE_DETAIL -> navigateToCourseDetail(nav, deepLink)
            DeepLinkType.DISCOVERY_PROGRAM_DETAIL -> navigateToProgramDetail(nav, deepLink)
            else -> handleLoggedOutOrUserNavigation(nav, deepLink)
        }
    }

    private fun handleLoggedOutOrUserNavigation(nav: NavController, deepLink: DeepLink) {
        if (!isUserLoggedIn) {
            nav.navigate(AppNavRoutes.SignIn())
        } else {
            when (deepLink.type) {
                DeepLinkType.PROGRAM -> navigateToProgram(nav, deepLink)
                DeepLinkType.PROFILE, DeepLinkType.USER_PROFILE -> {
                    nav.navigate(AppNavRoutes.Main(openTab = "PROFILE"))
                }
                else -> handleCourseRelatedNavigation(nav, deepLink)
            }
        }
    }

    private fun handleCourseRelatedNavigation(nav: NavController, deepLink: DeepLink) {
        launch(Dispatchers.Main) {
            val courseId = deepLink.courseId ?: return@launch
            val course = getCourseDetails(courseId) ?: return@launch
            if (!course.isEnrolled) return@launch

            handleSpecificCourseNavigation(nav, deepLink, course.name)
        }
    }

    private fun handleSpecificCourseNavigation(nav: NavController, deepLink: DeepLink, courseTitle: String) {
        val courseId = deepLink.courseId ?: return
        when (deepLink.type) {
            DeepLinkType.COURSE_DASHBOARD, DeepLinkType.ENROLL, DeepLinkType.ADD_BETA_TESTER -> {
                nav.navigate(AppNavRoutes.CourseContainer(courseId = courseId, courseTitle = courseTitle))
            }
            DeepLinkType.UNENROLL, DeepLinkType.REMOVE_BETA_TESTER -> {}
            DeepLinkType.COURSE_VIDEOS -> {
                nav.navigate(AppNavRoutes.CourseContainer(courseId = courseId, courseTitle = "", openTab = "VIDEOS"))
            }
            DeepLinkType.COURSE_DATES -> {
                nav.navigate(AppNavRoutes.CourseContainer(courseId = courseId, courseTitle = "", openTab = "DATES"))
            }
            DeepLinkType.COURSE_DISCUSSION -> {
                nav.navigate(AppNavRoutes.CourseContainer(courseId = courseId, courseTitle = "", openTab = "DISCUSSIONS"))
            }
            DeepLinkType.COURSE_HANDOUT -> {
                nav.navigate(AppNavRoutes.HandoutsWebView(courseId = courseId, type = HandoutsType.Handouts.name))
            }
            DeepLinkType.COURSE_ANNOUNCEMENT -> {
                nav.navigate(AppNavRoutes.HandoutsWebView(courseId = courseId, type = HandoutsType.Announcements.name))
            }
            DeepLinkType.COURSE_COMPONENT -> navigateToCourseComponent(nav, deepLink)
            DeepLinkType.DISCUSSION_TOPIC -> navigateToDiscussionTopic(nav, deepLink)
            DeepLinkType.DISCUSSION_POST -> navigateToDiscussionPost(nav, deepLink)
            DeepLinkType.DISCUSSION_COMMENT, DeepLinkType.FORUM_RESPONSE -> {
                navigateToDiscussionResponse(nav, deepLink)
            }
            DeepLinkType.FORUM_COMMENT -> navigateToDiscussionComment(nav, deepLink)
            else -> {}
        }
    }

    private fun navigateToDiscovery(nav: NavController) {
        if (isUserLoggedIn) {
            nav.navigate(AppNavRoutes.Main(openTab = "DISCOVER"))
        } else if (!config.isPreLoginExperienceEnabled()) {
            nav.navigate(AppNavRoutes.SignIn())
        }
    }

    private fun navigateToCourseDetail(nav: NavController, deepLink: DeepLink) {
        deepLink.courseId?.let { courseId ->
            nav.navigate(AppNavRoutes.CourseInfo(courseId = courseId, infoType = WebViewLink.Authority.COURSE_INFO.name))
        }
    }

    private fun navigateToProgramDetail(nav: NavController, deepLink: DeepLink) {
        deepLink.pathId?.let { pathId ->
            nav.navigate(AppNavRoutes.CourseInfo(courseId = pathId, infoType = WebViewLink.Authority.PROGRAM_INFO.name))
        }
    }

    private fun navigateToProgram(nav: NavController, deepLink: DeepLink) {
        val pathId = deepLink.pathId
        if (pathId == null) {
            nav.navigate(AppNavRoutes.Main(openTab = "PROGRAMS"))
        } else {
            nav.navigate(AppNavRoutes.Program(pathId = pathId))
        }
    }

    private fun navigateToCourseComponent(nav: NavController, deepLink: DeepLink) {
        val courseId = deepLink.courseId ?: return
        val componentId = deepLink.componentId ?: return
        launch {
            try {
                val courseStructure = courseInteractor.getCourseStructure(courseId)
                courseStructure.blockData
                    .find { it.descendants.contains(componentId) }?.let { block ->
                        launch(Dispatchers.Main) {
                            nav.navigate(
                                AppNavRoutes.CourseUnitContainer(
                                    courseId = courseId,
                                    unitId = block.id,
                                    componentId = componentId,
                                    mode = CourseViewMode.FULL.name
                                )
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToDiscussionTopic(nav: NavController, deepLink: DeepLink) {
        val courseId = deepLink.courseId ?: return
        val topicId = deepLink.topicId ?: return
        launch {
            try {
                discussionInteractor.getCourseTopics(courseId)
                    .find { it.id == topicId }?.let { topic ->
                        launch(Dispatchers.Main) {
                            nav.navigate(
                                AppNavRoutes.DiscussionThreads(
                                    action = DiscussionTopicsViewModel.TOPIC,
                                    courseId = courseId,
                                    topicId = topicId,
                                    title = topic.name,
                                    viewType = FragmentViewType.FULL_CONTENT.name
                                )
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToDiscussionPost(nav: NavController, deepLink: DeepLink) {
        val courseId = deepLink.courseId ?: return
        val topicId = deepLink.topicId ?: return
        val threadId = deepLink.threadId ?: return
        launch {
            try {
                val thread = discussionInteractor.getThread(threadId, courseId, topicId)
                launch(Dispatchers.Main) {
                    nav.navigate(
                        AppNavRoutes.DiscussionComments(
                            threadJson = json.encodeToString(
                                org.openedx.discussion.domain.model.Thread.serializer(),
                                thread
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToDiscussionResponse(nav: NavController, deepLink: DeepLink) {
        val commentId = deepLink.commentId ?: return
        launch {
            try {
                val response = discussionInteractor.getResponse(commentId)
                launch(Dispatchers.Main) {
                    nav.navigate(
                        AppNavRoutes.DiscussionResponses(
                            commentJson = json.encodeToString(
                                org.openedx.discussion.domain.model.DiscussionComment.serializer(),
                                response
                            ),
                            isClosed = false
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToDiscussionComment(nav: NavController, deepLink: DeepLink) {
        val parentId = deepLink.parentId ?: return
        launch {
            try {
                val comment = discussionInteractor.getResponse(parentId)
                launch(Dispatchers.Main) {
                    nav.navigate(
                        AppNavRoutes.DiscussionResponses(
                            commentJson = json.encodeToString(
                                org.openedx.discussion.domain.model.DiscussionComment.serializer(),
                                comment
                            ),
                            isClosed = false
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getCourseDetails(courseId: String): Course? {
        return try {
            discoveryInteractor.getCourseDetails(courseId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
