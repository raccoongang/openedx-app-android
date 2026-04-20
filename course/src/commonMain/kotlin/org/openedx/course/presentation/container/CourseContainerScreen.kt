package org.openedx.course.presentation.container

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.domain.model.CourseAccessError
import org.openedx.core.ui.RoundTabsBar
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.course.presentation.home.CourseHomeScreen
import org.openedx.course.presentation.videos.CourseContentVideoScreen
import org.openedx.course.presentation.outline.CourseContentAllScreen
import org.openedx.course.presentation.progress.CourseProgressScreen
import org.openedx.course.presentation.offline.CourseOfflineScreen
import org.openedx.course.presentation.dates.CourseDatesScreen
import org.openedx.course.presentation.handouts.HandoutsScreen
import org.openedx.foundation.presentation.WindowSize
import androidx.compose.foundation.layout.BoxScope
import org.openedx.foundation.presentation.rememberWindowSize
import androidx.compose.runtime.rememberCoroutineScope

/**
 * CourseContainer root screen — hosts the collapsing header over a 7-tab pager
 * (Home / Content / Progress / Dates / Offline / Discussions / More). Matches the
 * original Android `CourseContainerFragment` layout.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseContainerScreen(
    courseId: String,
    courseTitle: String,
    openTab: String = "COURSE",
    resumeBlockId: String = "",
    onBackClick: () -> Unit,
    onNoAccess: (title: String) -> Unit = {},
    onNavigateToCourseContainer: (courseId: String, unitId: String, componentId: String, mode: CourseViewMode) -> Unit,
    onNavigateToCourseSubsections: (courseId: String, subSectionId: String, unitId: String, componentId: String, mode: CourseViewMode) -> Unit,
    onNavigateToDownloadQueue: (List<String>) -> Unit,
    onNavigateToHandoutsWebView: (type: String) -> Unit,
    onNavigateToCalendarSettings: () -> Unit = {},
    discussionsContent: @Composable BoxScope.(courseId: String, courseTitle: String) -> Unit,
) {
    val windowSize = rememberWindowSize()
    val coroutineScope = rememberCoroutineScope()

    val containerVm: CourseContainerViewModel = koinViewModel {
        parametersOf(courseId, courseTitle, resumeBlockId)
    }
    val courseImage by containerVm.courseImage.collectAsState(initial = null)
    val accessStatus by containerVm.courseAccessStatus.collectAsState(initial = null)
    val isNavigationEnabled by containerVm.isNavigationEnabled.collectAsState(initial = false)
    val dataReady by containerVm.dataReady.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        containerVm.fetchCourseDetails()
    }

    LaunchedEffect(dataReady) {
        if (dataReady == false) {
            onNoAccess(containerVm.courseName)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = CourseContainerTab.entries
            .indexOfFirst { it.name.equals(openTab, ignoreCase = true) }
            .coerceAtLeast(0),
        pageCount = { CourseContainerTab.entries.size },
    )
    val tabRowState = rememberLazyListState()

    val isLayoutEnabled = accessStatus == CourseAccessError.NONE || accessStatus == null

    CollapsingLayout(
        modifier = Modifier.fillMaxSize(),
        courseImage = (courseImage as? String).orEmpty(),
        imageHeight = 200,
        isEnabled = isLayoutEnabled,
        onBackClick = onBackClick,
        expandedTop = {
            ExpandedHeaderContent(
                courseTitle = containerVm.courseName,
                org = containerVm.courseDetails?.courseInfoOverview?.org ?: "",
            )
        },
        collapsedTop = {
            CollapsedHeaderContent(courseTitle = containerVm.courseName)
        },
        navigation = {
            if (isNavigationEnabled || dataReady != null) {
                RoundTabsBar(
                    items = CourseContainerTab.entries,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    rowState = tabRowState,
                    pagerState = pagerState,
                    withPager = true,
                    onTabClicked = containerVm::courseContainerTabClickedEvent,
                )
            }
        },
        bodyContent = {
            CourseContainerPager(
                windowSize = windowSize,
                courseId = courseId,
                courseTitle = containerVm.courseName,
                pagerState = pagerState,
                isNavigationEnabled = isNavigationEnabled,
                onNavigateToCourseContainer = onNavigateToCourseContainer,
                onNavigateToCourseSubsections = onNavigateToCourseSubsections,
                onNavigateToDownloadQueue = onNavigateToDownloadQueue,
                onNavigateToHandoutsWebView = onNavigateToHandoutsWebView,
                onNavigateToTab = { tab ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(tab.ordinal)
                    }
                },
                onNavigateToCalendarSettings = onNavigateToCalendarSettings,
                onRefreshCourse = { containerVm.fetchCourseDetails() },
                discussionsContent = discussionsContent,
            )
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CourseContainerPager(
    windowSize: WindowSize,
    courseId: String,
    courseTitle: String,
    pagerState: PagerState,
    isNavigationEnabled: Boolean,
    onNavigateToCourseContainer: (courseId: String, unitId: String, componentId: String, mode: CourseViewMode) -> Unit,
    onNavigateToCourseSubsections: (courseId: String, subSectionId: String, unitId: String, componentId: String, mode: CourseViewMode) -> Unit,
    onNavigateToDownloadQueue: (List<String>) -> Unit,
    onNavigateToHandoutsWebView: (type: String) -> Unit,
    onNavigateToTab: (CourseContainerTab) -> Unit,
    onNavigateToCalendarSettings: () -> Unit = {},
    onRefreshCourse: () -> Unit = {},
    discussionsContent: @Composable BoxScope.(courseId: String, courseTitle: String) -> Unit,
) {
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        userScrollEnabled = isNavigationEnabled,
        beyondViewportPageCount = 1,
    ) { page ->
        when (CourseContainerTab.entries[page]) {
            CourseContainerTab.HOME -> {
                val homeVm: org.openedx.course.presentation.home.CourseHomeViewModel = koinViewModel {
                    parametersOf(courseId, courseTitle)
                }
                val homePagerState = rememberPagerState {
                    org.openedx.course.presentation.home.CourseHomePagerTab.entries.size
                }
                CourseHomeScreen(
                    windowSize = windowSize,
                    viewModel = homeVm,
                    homePagerState = homePagerState,
                    onNavigateToContent = { _ -> onNavigateToTab(CourseContainerTab.CONTENT) },
                    onNavigateToProgress = { onNavigateToTab(CourseContainerTab.PROGRESS) },
                    onNavigateToCourseContainer = onNavigateToCourseContainer,
                    onNavigateToCourseSubsections = onNavigateToCourseSubsections,
                    onNavigateToDownloadQueue = onNavigateToDownloadQueue,
                )
            }

            CourseContainerTab.CONTENT -> {
                val contentVm: org.openedx.course.presentation.contenttab.ContentTabViewModel = koinViewModel {
                    parametersOf(courseId, courseTitle)
                }
                val contentPagerState = rememberPagerState { CourseContentTab.entries.size }
                org.openedx.course.presentation.contenttab.ContentTabScreen(
                    viewModel = contentVm,
                    windowSize = windowSize,
                    courseId = courseId,
                    courseName = courseTitle,
                    pagerState = contentPagerState,
                    onNavigateToCourseContainer = onNavigateToCourseContainer,
                    onNavigateToCourseSubsections = onNavigateToCourseSubsections,
                    onNavigateToDownloadQueue = onNavigateToDownloadQueue,
                )
            }

            CourseContainerTab.PROGRESS -> {
                val vm: org.openedx.course.presentation.progress.CourseProgressViewModel = koinViewModel {
                    parametersOf(courseId)
                }
                CourseProgressScreen(windowSize = windowSize, viewModel = vm)
            }

            CourseContainerTab.DATES -> {
                val vm: org.openedx.course.presentation.dates.CourseDatesViewModel = koinViewModel {
                    parametersOf(courseId, "")
                }
                CourseDatesScreen(
                    windowSize = windowSize,
                    viewModel = vm,
                    updateCourseStructure = onRefreshCourse,
                    onNavigateToCourseContainer = onNavigateToCourseContainer,
                    onNavigateToCourseSubsections = onNavigateToCourseSubsections,
                    onNavigateToCalendarSettings = onNavigateToCalendarSettings,
                )
            }

            CourseContainerTab.OFFLINE -> {
                val vm: org.openedx.course.presentation.offline.CourseOfflineViewModel = koinViewModel {
                    parametersOf(courseId, courseTitle)
                }
                CourseOfflineScreen(windowSize = windowSize, viewModel = vm)
            }

            CourseContainerTab.DISCUSSIONS -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    discussionsContent(courseId, courseTitle)
                }
            }

            CourseContainerTab.MORE -> {
                HandoutsScreen(
                    windowSize = windowSize,
                    onHandoutsClick = { onNavigateToHandoutsWebView("Handouts") },
                    onAnnouncementsClick = { onNavigateToHandoutsWebView("Announcements") },
                )
            }
        }
    }
}
