package org.openedx.course.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.NoContentScreenType
import org.openedx.core.domain.model.Block
import org.openedx.core.ui.CircularProgress
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.NoContentScreen
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.*
import org.openedx.course.Res
import org.openedx.course.presentation.container.CourseContentTab
import org.openedx.course.presentation.ui.CourseMessage
import org.openedx.course.presentation.ui.ResumeCourseButton
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.foundation.extension.takeIfNotEmpty
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.core_ic_check
import org.openedx.core.Res as coreRes

@Composable
fun CourseHomeScreen(
    windowSize: WindowSize,
    viewModel: CourseHomeViewModel,
    homePagerState: PagerState,
    onNavigateToContent: (CourseContentTab) -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToCourseContainer: (courseId: String, unitId: String, componentId: String, CourseViewMode) -> Unit = { _, _, _, _ -> },
    onNavigateToCourseSubsections: (courseId: String, subSectionId: String, unitId: String, componentId: String, CourseViewMode) -> Unit = { _, _, _, _, _ -> },
    onNavigateToDownloadQueue: (List<String>) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(null)
    val resumeBlockId by viewModel.resumeBlockId.collectAsState("")
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.navigationAction.collect { action ->
            when (action) {
                is org.openedx.course.presentation.CourseNavigationAction.NavigateToCourseContainer -> {
                    onNavigateToCourseContainer(action.courseId, action.unitId, action.componentId, action.mode)
                }
                is org.openedx.course.presentation.CourseNavigationAction.NavigateToCourseSubsections -> {
                    onNavigateToCourseSubsections(action.courseId, action.subSectionId, action.unitId, action.componentId, action.mode)
                }
                is org.openedx.course.presentation.CourseNavigationAction.NavigateToDownloadQueue -> {
                    onNavigateToDownloadQueue(action.descendants)
                }
            }
        }
    }

    LaunchedEffect(resumeBlockId) {
        if (resumeBlockId.isNotEmpty()) {
            viewModel.openBlock(resumeBlockId)
        }
    }

    CourseHomeUI(
        windowSize = windowSize,
        uiState = uiState,
        uiMessage = uiMessage,
        homePagerState = homePagerState,
        onSubSectionClick = { subSectionBlock ->
            // Log section/subsection click event
            viewModel.logSectionSubsectionClick(
                subSectionBlock.blockId,
                subSectionBlock.displayName
            )
            if (viewModel.isCourseDropdownNavigationEnabled) {
                viewModel.courseSubSectionUnit[subSectionBlock.id]?.let { unit ->
                    onNavigateToCourseContainer(
                        viewModel.courseId,
                        unit.id,
                        "",
                        CourseViewMode.FULL
                    )
                }
            } else {
                onNavigateToCourseSubsections(
                    viewModel.courseId,
                    subSectionBlock.id,
                    "",
                    "",
                    CourseViewMode.FULL
                )
            }
        },
        onResumeClick = { componentId ->
            viewModel.openBlock(componentId)
        },
        onDownloadClick = { blocksIds ->
            viewModel.downloadBlocks(
                blocksIds = blocksIds,
                fragmentManager = null,
            )
        },
        onCertificateClick = {
            viewModel.viewCertificateTappedEvent()
            it.takeIfNotEmpty()
                ?.let { url -> uriHandler.openUri(url) }
        },
        onVideoClick = { videoBlock ->
            val parentId = viewModel.getBlockParent(videoBlock.id)?.id ?: return@CourseHomeUI
            onNavigateToCourseContainer(
                viewModel.courseId,
                parentId,
                "",
                CourseViewMode.VIDEOS
            )
            viewModel.logVideoClick(videoBlock.id)
        },
        onAssignmentClick = { assignmentBlock ->
            val parentId = viewModel.getBlockParent(assignmentBlock.id)?.id ?: return@CourseHomeUI
            onNavigateToCourseContainer(
                viewModel.courseId,
                parentId,
                "",
                CourseViewMode.FULL
            )
            viewModel.logAssignmentClick(assignmentBlock.id)
        },
        onNavigateToContent = onNavigateToContent,
        onNavigateToProgress = onNavigateToProgress,
        getBlockParent = viewModel::getBlockParent,
        onViewAllContentClick = viewModel::logViewAllContentClick,
        onViewAllVideosClick = viewModel::logViewAllVideosClick,
        onViewAllAssignmentsClick = viewModel::logViewAllAssignmentsClick,
        onViewProgressClick = viewModel::logViewProgressClick
    )
}

@Composable
private fun CourseHomeUI(
    windowSize: WindowSize,
    uiState: CourseHomeUIState,
    uiMessage: UIMessage?,
    homePagerState: PagerState,
    onSubSectionClick: (Block) -> Unit,
    onResumeClick: (String) -> Unit,
    onDownloadClick: (blockIds: List<String>) -> Unit,
    onCertificateClick: (String) -> Unit,
    onVideoClick: (Block) -> Unit,
    onAssignmentClick: (Block) -> Unit,
    onNavigateToContent: (CourseContentTab) -> Unit,
    onNavigateToProgress: () -> Unit,
    getBlockParent: (blockId: String) -> Block?,
    onViewAllContentClick: () -> Unit,
    onViewAllVideosClick: () -> Unit,
    onViewAllAssignmentsClick: () -> Unit,
    onViewProgressClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.appColors.background
    ) {
        val screenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = screenWidth,
                color = MaterialTheme.appColors.background
            ) {
                when (uiState) {
                    is CourseHomeUIState.CourseData -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            val certificate = uiState.courseStructure.certificate
                            if (certificate?.isCertificateEarned() == true) {
                                CourseMessage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 12.dp,
                                            horizontal = 24.dp
                                        ),
                                    icon = painterResource(Res.drawable.course_ic_certificate),
                                    message = stringResource(
                                        Res.string.course_you_earned_certificate,
                                        uiState.courseStructure.name
                                    ),
                                    action = stringResource(Res.string.course_view_certificate),
                                    onActionClick = {
                                        onCertificateClick(
                                            certificate.certificateURL ?: ""
                                        )
                                    }
                                )
                            }

                            if (uiState.resumeComponent != null) {
                                ResumeCourseButton(
                                    modifier = Modifier.padding(16.dp),
                                    block = uiState.resumeComponent,
                                    displayName = uiState.resumeUnitTitle,
                                    onResumeClick = onResumeClick
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            CourseHomePager(
                                modifier = Modifier.fillMaxSize(),
                                pages = CourseHomePagerTab.entries,
                                pagerState = homePagerState
                            ) { tab ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.appColors.cardViewBackground
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.appColors.cardViewBorder
                                    ),
                                    shape = MaterialTheme.appShapes.cardShape,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                ) {
                                    when (tab) {
                                        CourseHomePagerTab.COURSE_COMPLETION -> {
                                            CourseCompletionHomePagerCardContent(
                                                uiState = uiState,
                                                onViewAllContentClick = {
                                                    onViewAllContentClick()
                                                    onNavigateToContent(CourseContentTab.ALL)
                                                },
                                                onDownloadClick = onDownloadClick,
                                                onSubSectionClick = onSubSectionClick
                                            )
                                        }

                                        CourseHomePagerTab.VIDEOS -> {
                                            VideosHomePagerCardContent(
                                                uiState = uiState,
                                                onVideoClick = onVideoClick,
                                                onViewAllVideosClick = {
                                                    onViewAllVideosClick()
                                                    onNavigateToContent(CourseContentTab.VIDEOS)
                                                }
                                            )
                                        }

                                        CourseHomePagerTab.ASSIGNMENT -> {
                                            AssignmentsHomePagerCardContent(
                                                uiState = uiState,
                                                onAssignmentClick = onAssignmentClick,
                                                getBlockParent = getBlockParent,
                                                onViewAllAssignmentsClick = {
                                                    onViewAllAssignmentsClick()
                                                    onNavigateToContent(CourseContentTab.ASSIGNMENTS)
                                                }
                                            )
                                        }

                                        CourseHomePagerTab.GRADES -> {
                                            GradesHomePagerCardContent(
                                                uiState = uiState,
                                                onViewProgressClick = {
                                                    onViewProgressClick()
                                                    onNavigateToProgress()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    CourseHomeUIState.Error -> {
                        NoContentScreen(noContentScreenType = NoContentScreenType.COURSE_OUTLINE)
                    }

                    CourseHomeUIState.Loading -> {
                        CircularProgress()
                    }

                    CourseHomeUIState.Waiting -> {}
                }
            }
        }
    }
}

@Composable
fun <T> CourseHomePager(
    modifier: Modifier = Modifier,
    pages: List<T>,
    pagerState: PagerState,
    pageContent: @Composable (T) -> Unit
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 8.dp,
        beyondViewportPageCount = pages.size,
        verticalAlignment = Alignment.Top
    ) { page ->
        pageContent(pages[page])
    }
}

