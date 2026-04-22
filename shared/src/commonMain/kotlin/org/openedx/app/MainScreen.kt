package org.openedx.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.openedx.app.deeplink.HomeTab
import org.openedx.app.navigation.AppNavRoutes
import org.openedx.core.AppUpdateState
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRecommendDialog
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRecommendedBox
import org.openedx.core.system.PlatformActions
import org.openedx.core.system.notifier.app.AppUpgradeEvent
import org.openedx.core.ui.theme.appColors
import org.openedx.courses.presentation.DashboardGalleryView
import org.openedx.dates.presentation.dates.DatesScreen
import org.openedx.dates.presentation.dates.DatesViewActions
import org.openedx.dates.presentation.dates.DatesViewModel
import org.openedx.discovery.presentation.NativeDiscoveryView
import org.openedx.downloads.presentation.download.DownloadsScreen
import org.openedx.downloads.presentation.download.DownloadsViewActions
import org.openedx.downloads.presentation.download.DownloadsViewModel
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.profile.presentation.profile.ProfileViewModel
import org.openedx.profile.presentation.profile.compose.ProfileView
import org.openedx.profile.presentation.profile.compose.ProfileViewAction

private const val ROUTE_LEARN = "learn"
private const val ROUTE_DISCOVER = "discover"
private const val ROUTE_DOWNLOADS = "downloads"
private const val ROUTE_DATES = "dates"
private const val ROUTE_PROFILE = "profile"

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun MainScreen(
    navController: NavHostController,
    openTab: String = HomeTab.LEARN.name,
    courseId: String? = null,
    infoType: String? = null,
) {
    val viewModel: MainViewModel = koinViewModel()
    val platformActions: PlatformActions = koinInject()
    val isBottomBarEnabled by viewModel.isBottomBarEnabled.collectAsState()
    val appUpgradeEvent by viewModel.appUpgradeEvent.collectAsState()
    val wasUpgradeDialogClosed by remember { AppUpdateState.wasUpgradeDialogClosed }
    var showRecommendDialog by rememberSaveable { mutableStateOf(false) }
    var deepLinkConsumed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(courseId, infoType) {
        if (!deepLinkConsumed && !courseId.isNullOrBlank()) {
            deepLinkConsumed = true
            if (viewModel.isDiscoveryTypeWebView && !infoType.isNullOrBlank()) {
                navController.navigate(AppNavRoutes.CourseInfo(courseId, infoType))
            } else {
                navController.navigate(AppNavRoutes.CourseDetails(courseId))
            }
        }
    }

    val tabs = remember(viewModel.isDownloadsFragmentEnabled, viewModel.isDatesFragmentEnabled) {
        buildList {
            add(BottomNavItem("Learn", Icons.Default.School, ROUTE_LEARN))
            add(BottomNavItem("Discover", Icons.Default.Explore, ROUTE_DISCOVER))
            if (viewModel.isDownloadsFragmentEnabled) {
                add(BottomNavItem("Downloads", Icons.Default.Download, ROUTE_DOWNLOADS))
            }
            if (viewModel.isDatesFragmentEnabled) {
                add(BottomNavItem("Dates", Icons.Default.DateRange, ROUTE_DATES))
            }
            add(BottomNavItem("Profile", Icons.Default.Person, ROUTE_PROFILE))
        }
    }

    val initialIndex = remember(tabs, openTab) {
        val route = when (openTab) {
            HomeTab.LEARN.name, HomeTab.PROGRAMS.name -> ROUTE_LEARN
            HomeTab.DISCOVER.name -> ROUTE_DISCOVER
            HomeTab.DOWNLOADS.name -> if (viewModel.isDownloadsFragmentEnabled) {
                ROUTE_DOWNLOADS
            } else {
                ROUTE_LEARN
            }
            HomeTab.DATES.name -> ROUTE_DATES
            HomeTab.PROFILE.name -> ROUTE_PROFILE
            else -> ROUTE_LEARN
        }
        tabs.indexOfFirst { it.route == route }.coerceAtLeast(0)
    }

    var selectedIndex by rememberSaveable { mutableIntStateOf(initialIndex) }

    LaunchedEffect(Unit) {
        viewModel.navigateToDiscovery.collect { shouldNavigate ->
            if (shouldNavigate) {
                val idx = tabs.indexOfFirst { it.route == ROUTE_DISCOVER }
                if (idx >= 0) selectedIndex = idx
            }
        }
    }

    LaunchedEffect(appUpgradeEvent) {
        when (appUpgradeEvent) {
            is AppUpgradeEvent.UpgradeRecommendedEvent -> {
                if (!AppUpdateState.wasUpgradeDialogClosed.value &&
                    !AppUpdateState.wasUpdateDialogDisplayed
                ) {
                    AppUpdateState.wasUpdateDialogDisplayed = true
                    showRecommendDialog = true
                }
            }
            is AppUpgradeEvent.UpgradeRequiredEvent -> {
                if (!AppUpdateState.wasUpdateDialogDisplayed) {
                    AppUpdateState.wasUpdateDialogDisplayed = true
                    val profileIdx = tabs.indexOfFirst { it.route == ROUTE_PROFILE }
                    if (profileIdx >= 0) selectedIndex = profileIdx
                    viewModel.enableBottomBar(false)
                    navController.navigate(AppNavRoutes.UpgradeRequired)
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (appUpgradeEvent is AppUpgradeEvent.UpgradeRecommendedEvent &&
                    wasUpgradeDialogClosed
                ) {
                    AppUpgradeRecommendedBox(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { platformActions.openAppInMarket() },
                    )
                }
                NavigationBar(
                    containerColor = MaterialTheme.appColors.background,
                ) {
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selectedIndex == index,
                            enabled = isBottomBarEnabled,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.appColors.primary,
                                selectedTextColor = MaterialTheme.appColors.primary,
                                unselectedIconColor = MaterialTheme.appColors.textFieldHint,
                                unselectedTextColor = MaterialTheme.appColors.textFieldHint,
                                indicatorColor = Color.Transparent,
                            ),
                            onClick = {
                                selectedIndex = index
                                when (item.route) {
                                    ROUTE_LEARN -> viewModel.logLearnTabClickedEvent()
                                    ROUTE_DISCOVER -> viewModel.logDiscoveryTabClickedEvent()
                                    ROUTE_DOWNLOADS -> viewModel.logDownloadsTabClickedEvent()
                                    ROUTE_DATES -> viewModel.logDatesTabClickedEvent()
                                    ROUTE_PROFILE -> viewModel.logProfileTabClickedEvent()
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            when (tabs.getOrNull(selectedIndex)?.route) {
                ROUTE_LEARN -> LearnTab(navController) {
                    val idx = tabs.indexOfFirst { it.route == ROUTE_DISCOVER }
                    if (idx >= 0) selectedIndex = idx
                }
                ROUTE_DISCOVER -> DiscoverTab(navController)
                ROUTE_DOWNLOADS -> DownloadsTab(navController)
                ROUTE_DATES -> DatesTab(navController)
                ROUTE_PROFILE -> ProfileTab(navController)
            }
            if (showRecommendDialog) {
                AppUpgradeRecommendDialog(
                    modifier = Modifier.fillMaxSize(),
                    onNotNowClick = {
                        AppUpdateState.wasUpgradeDialogClosed.value = true
                        showRecommendDialog = false
                    },
                    onUpdateClick = {
                        AppUpdateState.wasUpgradeDialogClosed.value = true
                        showRecommendDialog = false
                        platformActions.openAppInMarket()
                    },
                )
            }
        }
    }
}

@Composable
private fun LearnTab(
    navController: NavHostController,
    onNavigateToDiscovery: () -> Unit,
) {
    val config: org.openedx.core.config.Config = org.koin.compose.koinInject()
    val dashboardType = remember { config.getDashboardConfig().getType() }
    if (dashboardType == org.openedx.core.config.DashboardConfig.DashboardType.LIST) {
        val vm: org.openedx.dashboard.presentation.DashboardListViewModel = koinViewModel()
        val windowSize = rememberWindowSize()
        val state by vm.uiState.collectAsState()
        val uiMessage by vm.uiMessage.collectAsState(initial = null)
        val refreshing by vm.updating.collectAsState()
        val canLoadMore by vm.canLoadMore.collectAsState()
        org.openedx.dashboard.presentation.DashboardListView(
            windowSize = windowSize,
            apiHostUrl = vm.apiHostUrl,
            state = state,
            uiMessage = uiMessage,
            canLoadMore = canLoadMore,
            refreshing = refreshing,
            hasInternetConnection = vm.hasInternetConnection,
            onReloadClick = { vm.getCourses() },
            onSwipeRefresh = { vm.updateCourses() },
            paginationCallback = { vm.fetchMore() },
            onItemClick = { enrolled ->
                vm.dashboardCourseClickedEvent(enrolled.course.id, enrolled.course.name)
                navController.navigate(
                    AppNavRoutes.CourseContainer(
                        courseId = enrolled.course.id,
                        courseTitle = enrolled.course.name,
                    )
                )
            },
        )
        return
    }
    DashboardGalleryView(
        onSettingsClick = { navController.navigate(AppNavRoutes.Settings) },
        onViewAll = { navController.navigate(AppNavRoutes.AllEnrolledCourses) },
        onOpenCourse = { enrolled ->
            navController.navigate(
                AppNavRoutes.CourseContainer(
                    courseId = enrolled.course.id,
                    courseTitle = enrolled.course.name,
                )
            )
        },
        onNavigateToDiscovery = onNavigateToDiscovery,
        onNavigateToDates = { enrolled ->
            navController.navigate(
                AppNavRoutes.CourseContainer(
                    courseId = enrolled.course.id,
                    courseTitle = enrolled.course.name,
                    openTab = "DATES",
                )
            )
        },
        onOpenBlock = { enrolled, blockId ->
            navController.navigate(
                AppNavRoutes.CourseContainer(
                    courseId = enrolled.course.id,
                    courseTitle = enrolled.course.name,
                    resumeBlockId = blockId,
                )
            )
        },
    )
}

@Composable
private fun DiscoverTab(navController: NavHostController) {
    NativeDiscoveryView(
        onCourseClick = { courseId, _ ->
            navController.navigate(AppNavRoutes.CourseDetails(courseId))
        },
        onSearchClick = {
            navController.navigate(AppNavRoutes.CourseSearch())
        },
        onSettingsClick = {
            navController.navigate(AppNavRoutes.Settings)
        },
    )
}

@Composable
private fun DownloadsTab(navController: NavHostController) {
    val viewModel: DownloadsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
    DownloadsScreen(
        uiState = uiState,
        uiMessage = uiMessage,
        apiHostUrl = "",
        hasInternetConnection = viewModel.hasInternetConnection,
        onAction = { action ->
            when (action) {
                DownloadsViewActions.OpenSettings ->
                    navController.navigate(AppNavRoutes.Settings)
                DownloadsViewActions.SwipeRefresh -> viewModel.refreshData()
                is DownloadsViewActions.OpenCourse ->
                    navController.navigate(
                        AppNavRoutes.CourseContainer(courseId = action.courseId, courseTitle = "")
                    )
                is DownloadsViewActions.DownloadCourse -> viewModel.downloadCourse(action.courseId)
                is DownloadsViewActions.CancelDownloading -> viewModel.cancelDownloading(action.courseId)
                is DownloadsViewActions.RemoveDownloads -> viewModel.removeDownloads(action.courseId)
            }
        },
    )
}

@Composable
private fun DatesTab(navController: NavHostController) {
    val viewModel: DatesViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
    DatesScreen(
        uiState = uiState,
        uiMessage = uiMessage,
        hasInternetConnection = viewModel.hasInternetConnection,
        useRelativeDates = false,
        onAction = { action ->
            when (action) {
                DatesViewActions.OpenSettings ->
                    navController.navigate(AppNavRoutes.Settings)
                DatesViewActions.SwipeRefresh -> viewModel.refreshData()
                DatesViewActions.ShiftDueDate -> viewModel.shiftAllDueDates()
                DatesViewActions.LoadMore -> viewModel.fetchMore()
                is DatesViewActions.OpenEvent -> {
                    viewModel.logAssignmentClick()
                    navController.navigate(
                        AppNavRoutes.CourseContainer(
                            courseId = action.date.courseId,
                            courseTitle = action.date.courseName,
                            resumeBlockId = action.date.firstComponentBlockId,
                        )
                    )
                }
            }
        },
    )
}

@Composable
private fun ProfileTab(navController: NavHostController) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
    val refreshing by viewModel.isUpdating.collectAsState()
    ProfileView(
        windowSize = rememberWindowSize(),
        uiState = uiState,
        uiMessage = uiMessage,
        refreshing = refreshing,
        onAction = { action ->
            when (action) {
                ProfileViewAction.EditAccountClick -> {
                    viewModel.profileEditClicked()
                    navController.navigate(AppNavRoutes.EditProfile(accountJson = ""))
                }
                ProfileViewAction.SwipeRefresh -> viewModel.updateAccount()
            }
        },
        onSettingsClick = { navController.navigate(AppNavRoutes.Settings) },
    )
}
