package org.openedx.course.presentation.unit.container

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.domain.model.Block
import org.openedx.core.ui.theme.appColors
import org.openedx.course.presentation.ChapterEndDialog
import org.openedx.course.presentation.ui.CourseUnitToolbar
import org.openedx.course.presentation.ui.HorizontalPageIndicator
import org.openedx.course.presentation.ui.NavigationUnitsButtons
import org.openedx.course.presentation.ui.VerticalPageIndicator
import org.openedx.foundation.presentation.WindowSize
import org.openedx.course.*
import org.openedx.course.Res

@Composable
fun CourseUnitContainerScreen(
    windowSize: WindowSize,
    viewModel: CourseUnitContainerViewModel,
    componentId: String = "",
    onBackClick: () -> Unit,
    onBackToOutline: () -> Unit = onBackClick,
    onNavigateToUnit: (courseId: String, unitId: String, componentId: String, mode: String) -> Unit,
    onNavigateToNextSection: (courseId: String, subSectionId: String, unitId: String, mode: String) -> Unit = { cId, _, uId, m -> onNavigateToUnit(cId, uId, "", m) },
    renderBlock: @Composable (Block) -> Unit,
) {
    val descendantsBlocks by viewModel.descendantsBlocks.collectAsState()
    val index by viewModel.indexInContainer.collectAsState(0)
    val coroutineScope = rememberCoroutineScope()

    var showChapterEndDialog by remember { mutableStateOf(false) }
    var chapterEndSectionName by remember { mutableStateOf("") }
    var chapterEndNextSectionName by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(
        initialPage = index,
    ) { descendantsBlocks.size.coerceAtLeast(1) }

    LaunchedEffect(Unit) {
        viewModel.loadBlocks(componentId)
        viewModel.courseUnitContainerShowedEvent()
    }

    // Throttle rapid next/prev taps (500ms) — matches Android restrictDoubleClick()
    var lastNavClickTime by remember { mutableStateOf(0L) }
    fun canHandleNavClick(): Boolean {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        return if (now - lastNavClickTime > 500L) {
            lastNavClickTime = now
            true
        } else false
    }

    // Sync pager page -> VM
    LaunchedEffect(pagerState.currentPage) {
        if (descendantsBlocks.isNotEmpty()) {
            viewModel.getCurrentBlock()
        }
    }

    // Sync VM index -> pager (for initial componentId jump)
    LaunchedEffect(index, descendantsBlocks.size) {
        if (index in descendantsBlocks.indices && pagerState.currentPage != index) {
            pagerState.scrollToPage(index)
        }
    }

    val currentBlockTitle = if (descendantsBlocks.isNotEmpty()) {
        descendantsBlocks.getOrNull(pagerState.currentPage)?.displayName ?: ""
    } else {
        ""
    }

    val subSectionUnitBlocks by viewModel.subSectionUnitBlocks.collectAsState()
    val unitsListShowed by viewModel.unitsListShowed.collectAsState()
    val currentUnit = subSectionUnitBlocks.firstOrNull { it.id == viewModel.unitId }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Toolbar
        CourseUnitToolbar(
            title = currentBlockTitle,
            onBackClick = onBackClick,
        )

        // 1a. SubSection units dropdown (matches Android SubSectionUnitsTitle + SubSectionUnitsList)
        if (viewModel.isCourseExpandableSectionsEnabled && subSectionUnitBlocks.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setUnitsListVisibility(!unitsListShowed) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "${currentUnit?.displayName ?: ""} (${subSectionUnitBlocks.size})",
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (unitsListShowed) 180f else 0f),
                    imageVector = Icons.Default.ExpandMore,
                    tint = MaterialTheme.appColors.textPrimary,
                    contentDescription = null,
                )
            }
            if (unitsListShowed) {
                val selectedIdx = subSectionUnitBlocks.indexOfFirst { it.id == viewModel.unitId }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .background(MaterialTheme.appColors.background),
                ) {
                    items(subSectionUnitBlocks) { unit ->
                        val idx = subSectionUnitBlocks.indexOf(unit)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (idx == selectedIdx) MaterialTheme.appColors.surface
                                    else MaterialTheme.appColors.background,
                                )
                                .clickable {
                                    viewModel.setUnitsListVisibility(false)
                                    if (idx != selectedIdx) {
                                        onNavigateToUnit(viewModel.courseId, unit.id, "", viewModel.mode.name)
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = unit.displayName,
                                color = MaterialTheme.appColors.textPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        // 2. Horizontal progress indicator
        if (viewModel.isCourseUnitProgressEnabled && descendantsBlocks.isNotEmpty()) {
            HorizontalPageIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                blocks = descendantsBlocks,
                selectedPage = pagerState.currentPage,
                completedAndSelectedColor = MaterialTheme.appColors.componentHorizontalProgressCompletedAndSelected,
                completedColor = MaterialTheme.appColors.componentHorizontalProgressCompleted,
                selectedColor = MaterialTheme.appColors.componentHorizontalProgressSelected,
                defaultColor = MaterialTheme.appColors.componentHorizontalProgressDefault,
            )
        }

        // 3. Pager area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (descendantsBlocks.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                ) { page ->
                    renderBlock(descendantsBlocks[page])
                }

                // Vertical dots indicator (when horizontal progress is disabled) —
                // matches Android native CourseUnitContainerFragment (cv_count at
                // constraintEnd_toEndOf=parent, width=24dp, defaultRadius=3dp,
                // selectedLength=5dp).
                if (!viewModel.isCourseUnitProgressEnabled) {
                    VerticalPageIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(24.dp),
                        numberOfPages = descendantsBlocks.size,
                        selectedPage = pagerState.currentPage,
                        selectedColor = MaterialTheme.appColors.primary,
                        defaultColor = MaterialTheme.appColors.bottomSheetToggle,
                        defaultRadius = 3.dp,
                        selectedLength = 5.dp,
                    )
                }
            }
        }

        // 4. Navigation buttons
        if (descendantsBlocks.isNotEmpty()) {
            NavigationUnitsButtons(
                hasPrevBlock = !viewModel.isFirstIndexInContainer,
                nextButtonText = if (viewModel.isLastIndexInContainer) {
                    stringResource(Res.string.course_navigation_finish)
                } else {
                    stringResource(Res.string.course_navigation_next)
                },
                hasNextBlock = !viewModel.isLastIndexInContainer,
                isVerticalNavigation = !viewModel.isCourseUnitProgressEnabled,
                onPrevClick = {
                    if (!canHandleNavClick()) return@NavigationUnitsButtons
                    val block = viewModel.moveToPrevBlock()
                    if (block != null) {
                        viewModel.prevBlockClickedEvent(block.blockId, block.displayName)
                        if (!block.type.isContainer()) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    }
                },
                onNextClick = {
                    if (!canHandleNavClick()) return@NavigationUnitsButtons
                    val block = viewModel.moveToNextBlock()
                    if (block != null) {
                        viewModel.nextBlockClickedEvent(block.blockId, block.displayName)
                        if (!block.type.isContainer()) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    } else {
                        // Last block - show chapter end dialog
                        val currentVerticalBlock = viewModel.getCurrentVerticalBlock()
                        val nextVerticalBlock = viewModel.getNextVerticalBlock()
                        chapterEndSectionName = currentVerticalBlock?.displayName ?: ""
                        chapterEndNextSectionName = nextVerticalBlock?.displayName ?: ""
                        showChapterEndDialog = true
                        currentVerticalBlock?.let {
                            viewModel.finishVerticalClickedEvent(it.blockId, it.displayName)
                        }
                    }
                },
            )
        }
    }

    // Chapter end dialog overlay
    if (showChapterEndDialog) {
        ChapterEndDialog(
            sectionName = chapterEndSectionName,
            nextSectionName = chapterEndNextSectionName,
            isVerticalNavigation = !viewModel.isCourseUnitProgressEnabled,
            onBackToOutlineClick = {
                showChapterEndDialog = false
                viewModel.finishVerticalBackClickedEvent()
                onBackToOutline()
            },
            onProceedClick = {
                showChapterEndDialog = false
                val oldSectionIndex = viewModel.currentSectionIndex
                viewModel.proceedToNext()
                val nextBlock = viewModel.getCurrentVerticalBlock()
                nextBlock?.let {
                    viewModel.finishVerticalNextClickedEvent(it.blockId, it.displayName)
                    if (it.type.isContainer()) {
                        val newSectionIndex = viewModel.currentSectionIndex
                        if (newSectionIndex != oldSectionIndex) {
                            // Crossed sequential boundary — replace section in nav stack
                            val subSection = viewModel.getSubSectionBlock(it.id)
                            onNavigateToNextSection(
                                viewModel.courseId, subSection.id, it.id, viewModel.mode.name
                            )
                        } else {
                            onNavigateToUnit(viewModel.courseId, it.id, "", viewModel.mode.name)
                        }
                    }
                }
            },
            onCancelClick = { showChapterEndDialog = false },
        )
    }
}
