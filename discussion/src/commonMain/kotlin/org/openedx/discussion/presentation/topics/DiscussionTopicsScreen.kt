package org.openedx.discussion.presentation.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.NoContentScreenType
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.NoContentScreen
import org.openedx.core.ui.StaticSearchBar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.discussion.*
import org.openedx.discussion.Res
import org.openedx.discussion.Res as discussionRes
import org.openedx.discussion.presentation.ui.ThreadItemCategory
import org.openedx.discussion.presentation.ui.TopicItem
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue

@Composable
fun DiscussionTopicsScreen(
    discussionTopicsViewModel: DiscussionTopicsViewModel,
    windowSize: WindowSize,
    onSearchClick: (courseId: String) -> Unit = {},
    onItemClick: (action: String, courseId: String, topicId: String, title: String) -> Unit = { _, _, _, _ -> },
) {
    val uiState by discussionTopicsViewModel.uiState.collectAsState()
    val uiMessage by discussionTopicsViewModel.uiMessage.collectAsState(null)

    DiscussionTopicsUI(
        windowSize = windowSize,
        uiState = uiState,
        uiMessage = uiMessage,
        onSearchClick = {
            onSearchClick(discussionTopicsViewModel.courseId)
        },
        onItemClick = { action, data, title ->
            discussionTopicsViewModel.discussionClickedEvent(
                action,
                data,
                title
            )
            onItemClick(
                action,
                discussionTopicsViewModel.courseId,
                data,
                title
            )
        },
    )
}

@Composable
private fun DiscussionTopicsUI(
    windowSize: WindowSize,
    uiState: DiscussionTopicsUIState,
    uiMessage: UIMessage?,
    onSearchClick: () -> Unit,
    onItemClick: (String, String, String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.appColors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val screenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        val searchTabWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.width(420.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        val contentPaddings by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = 0.dp,
                    compact = 24.dp
                )
            )
        }

        val categoriesHeight by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = 86.dp,
                    compact = 77.dp
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .statusBarsInset()
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(screenWidth) {
                if ((uiState is DiscussionTopicsUIState.Error).not()) {
                    StaticSearchBar(
                        modifier = Modifier
                            .height(48.dp)
                            .then(searchTabWidth)
                            .padding(horizontal = contentPaddings)
                            .fillMaxWidth(),
                        text = stringResource(discussionRes.string.discussion_search_all_posts),
                        onClick = onSearchClick
                    )
                }
                Surface(
                    modifier = Modifier.padding(top = 10.dp),
                    color = MaterialTheme.appColors.background,
                    shape = MaterialTheme.appShapes.screenBackgroundShape
                ) {
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.appColors.background)
                                .padding(horizontal = contentPaddings),
                        ) {
                            when (uiState) {
                                is DiscussionTopicsUIState.Topics -> {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(vertical = 24.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        item {
                                            Text(
                                                modifier = Modifier,
                                                text = stringResource(discussionRes.string.discussion_main_categories),
                                                style = MaterialTheme.appTypography.titleMedium,
                                                color = MaterialTheme.appColors.textPrimaryVariant
                                            )
                                        }
                                        item {
                                            val allPostsTitle = stringResource(discussionRes.string.discussion_all_posts)
                                            val followingPostsTitle = stringResource(discussionRes.string.discussion_posts_following)
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                ThreadItemCategory(
                                                    name = allPostsTitle,
                                                    painterResource = painterResource(
                                                        Res.drawable.discussion_all_posts
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(categoriesHeight),
                                                    onClick = {
                                                        onItemClick(
                                                            DiscussionTopicsViewModel.ALL_POSTS,
                                                            "",
                                                            allPostsTitle
                                                        )
                                                    }
                                                )
                                                ThreadItemCategory(
                                                    name = followingPostsTitle,
                                                    painterResource = painterResource(Res.drawable.discussion_star),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(categoriesHeight),
                                                    onClick = {
                                                        onItemClick(
                                                            DiscussionTopicsViewModel.FOLLOWING_POSTS,
                                                            "",
                                                            followingPostsTitle
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                        itemsIndexed(uiState.data) { index, topic ->
                                            if (topic.children.isNotEmpty()) {
                                                Text(
                                                    modifier = Modifier.padding(
                                                        top = 10.dp
                                                    ),
                                                    text = topic.name,
                                                    style = MaterialTheme.appTypography.titleMedium,
                                                    color = MaterialTheme.appColors.textPrimaryVariant
                                                )
                                            } else {
                                                TopicItem(topic = topic, onClick = { id, title ->
                                                    onItemClick(
                                                        DiscussionTopicsViewModel.TOPIC,
                                                        id,
                                                        title
                                                    )
                                                })
                                                if (uiState.data.getOrNull(index + 1)?.children?.isEmpty() == true) {
                                                    HorizontalDivider()
                                                }
                                            }
                                        }
                                    }
                                }

                                DiscussionTopicsUIState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                                    }
                                }
                                else -> {
                                    NoContentScreen(noContentScreenType = NoContentScreenType.COURSE_DISCUSSIONS)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
