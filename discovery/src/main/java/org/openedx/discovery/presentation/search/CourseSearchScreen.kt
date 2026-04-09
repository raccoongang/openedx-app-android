package org.openedx.discovery.presentation.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.pluralStringResource
import androidx.compose.ui.res.stringResource as androidStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.SearchBar
import org.openedx.core.ui.shouldLoadMore
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.discovery.DiscoveryMocks
import org.openedx.discovery.presentation.ui.DiscoveryCourseItem
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.discovery.*
import org.openedx.discovery.Res as discoveryRes

private const val LOAD_MORE_THRESHOLD = 4

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CourseSearchScreen(
    windowSize: WindowSize,
    state: CourseSearchUIState,
    uiMessage: UIMessage?,
    apiHostUrl: String,
    canLoadMore: Boolean,
    refreshing: Boolean,
    querySearch: String,
    isUserLoggedIn: Boolean,
    isRegistrationEnabled: Boolean,
    onBackClick: () -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSwipeRefresh: () -> Unit,
    paginationCallback: () -> Unit,
    onItemClick: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    val firstVisibleIndex = remember {
        mutableStateOf(scrollState.firstVisibleItemIndex)
    }
    val pullToRefreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = querySearch,
                selection = TextRange(querySearch.length)
            )
        )
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = scrollState.isScrollInProgress) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.appColors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!isUserLoggedIn) {
                Box(
                    modifier = Modifier
                        .padding(
                            horizontal = 16.dp,
                            vertical = 32.dp,
                        )
                ) {
                    AuthButtonsPanel(
                        onRegisterClick = onRegisterClick,
                        onSignInClick = onSignInClick,
                        showRegisterButton = isRegistrationEnabled
                    )
                }
            }
        }
    ) {
        val screenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }
        val searchTab by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.width(420.dp),
                    compact = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            )
        }
        val contentPaddings by remember {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = PaddingValues(
                        top = 32.dp,
                        bottom = 40.dp
                    ),
                    compact = PaddingValues(horizontal = 24.dp, vertical = 28.dp)
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .statusBarsInset(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(screenWidth) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .zIndex(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BackBtn {
                            onBackClick()
                        }
                        Text(
                            modifier = Modifier
                                .testTag("txt_search_title")
                                .fillMaxWidth()
                                .padding(horizontal = 56.dp),
                            text = androidStringResource(id = org.openedx.core.R.string.core_search),
                            color = MaterialTheme.appColors.textPrimary,
                            style = MaterialTheme.appTypography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    SearchBar(
                        modifier = Modifier
                            .height(48.dp)
                            .then(searchTab),
                        label = "",
                        requestFocus = true,
                        searchValue = textFieldValue,
                        keyboardActions = {
                            focusManager.clearFocus()
                        },
                        onValueChanged = { text ->
                            textFieldValue = text
                            onSearchTextChanged(textFieldValue.text)
                        },
                        onClearValue = {
                            textFieldValue = TextFieldValue("")
                            onSearchTextChanged(textFieldValue.text)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                Surface(
                    color = MaterialTheme.appColors.background
                ) {
                    val typingText =
                        if (textFieldValue.text.isEmpty()) {
                            stringResource(discoveryRes.string.discovery_start_typing_to_find)
                        } else {
                            pluralStringResource(
                                discoveryRes.plurals.discovery_found_courses,
                                (state as? CourseSearchUIState.Courses)?.numCourses ?: 0,
                                (state as? CourseSearchUIState.Courses)?.numCourses ?: 0
                            )
                        }
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxWidth(),
                        state = pullToRefreshState,
                        isRefreshing = refreshing,
                        onRefresh = { onSwipeRefresh() }
                    ) {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = contentPaddings,
                            state = scrollState
                        ) {
                            item {
                                Column {
                                    Text(
                                        modifier = Modifier.testTag("txt_search_results_title"),
                                        text = stringResource(discoveryRes.string.discovery_search_results),
                                        color = MaterialTheme.appColors.textPrimary,
                                        style = MaterialTheme.appTypography.displaySmall
                                    )
                                    Text(
                                        modifier = Modifier
                                            .testTag("txt_search_results_subtitle")
                                            .padding(top = 4.dp),
                                        text = typingText,
                                        color = MaterialTheme.appColors.textPrimary,
                                        style = MaterialTheme.appTypography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                            }
                            when (state) {
                                is CourseSearchUIState.Loading -> {
                                    item {
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 25.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                                        }
                                    }
                                }

                                is CourseSearchUIState.Courses -> {
                                    items(state.courses) { course ->
                                        DiscoveryCourseItem(
                                            apiHostUrl = apiHostUrl,
                                            course,
                                            windowSize = windowSize,
                                            onClick = { courseId ->
                                                onItemClick(courseId)
                                            }
                                        )
                                        HorizontalDivider()
                                    }
                                    item {
                                        if (canLoadMore) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                                            }
                                        }
                                    }
                                    if (scrollState.shouldLoadMore(firstVisibleIndex, LOAD_MORE_THRESHOLD)) {
                                        paginationCallback()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(rememberSaveable { true }) {
        onSearchTextChanged(querySearch)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CourseSearchScreenPreview() {
    OpenEdXTheme {
        CourseSearchScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = CourseSearchUIState.Courses(DiscoveryMocks.courses(2), 2),
            uiMessage = null,
            apiHostUrl = "",
            canLoadMore = false,
            refreshing = false,
            querySearch = "",
            isUserLoggedIn = true,
            isRegistrationEnabled = true,
            onBackClick = {},
            onSearchTextChanged = {},
            onSwipeRefresh = {},
            paginationCallback = {},
            onItemClick = {},
            onSignInClick = {},
            onRegisterClick = {},
        )
    }
}

@Preview(device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CourseSearchScreenTabletPreview() {
    OpenEdXTheme {
        CourseSearchScreen(
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            state = CourseSearchUIState.Courses(DiscoveryMocks.courses(2), 2),
            uiMessage = null,
            apiHostUrl = "",
            canLoadMore = false,
            refreshing = false,
            querySearch = "",
            isUserLoggedIn = false,
            isRegistrationEnabled = true,
            onBackClick = {},
            onSearchTextChanged = {},
            onSwipeRefresh = {},
            paginationCallback = {},
            onItemClick = {},
            onSignInClick = {},
            onRegisterClick = {},
        )
    }
}
