package org.openedx.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import org.openedx.app.navigation.AppNavRoutes
import org.openedx.core.ui.theme.appColors
import org.openedx.courses.presentation.DashboardGalleryScreenAction
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

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun MainScreen(
    navController: NavHostController,
    isDownloadsEnabled: Boolean = true,
    isDatesEnabled: Boolean = true,
) {
    val tabs = buildList {
        add(BottomNavItem("Learn", Icons.Default.School, "learn"))
        add(BottomNavItem("Discover", Icons.Default.Explore, "discover"))
        if (isDownloadsEnabled) {
            add(BottomNavItem("Downloads", Icons.Default.Download, "downloads"))
        }
        if (isDatesEnabled) {
            add(BottomNavItem("Dates", Icons.Default.DateRange, "dates"))
        }
        add(BottomNavItem("Profile", Icons.Default.Person, "profile"))
    }

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.appColors.background,
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (tabs.getOrNull(selectedIndex)?.route) {
                "learn" -> LearnTab(navController) {
                    selectedIndex = tabs.indexOfFirst { it.route == "discover" }
                }
                "discover" -> DiscoverTab(navController)
                "downloads" -> DownloadsTab(navController)
                "dates" -> DatesTab(navController)
                "profile" -> ProfileTab(navController)
            }
        }
    }
}

@Composable
private fun LearnTab(
    navController: NavHostController,
    onNavigateToDiscovery: () -> Unit,
) {
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
                DownloadsViewActions.SwipeRefresh -> {}
                is DownloadsViewActions.OpenCourse ->
                    navController.navigate(
                        AppNavRoutes.CourseContainer(courseId = action.courseId, courseTitle = "")
                    )
                is DownloadsViewActions.DownloadCourse -> {}
                is DownloadsViewActions.CancelDownloading -> {}
                is DownloadsViewActions.RemoveDownloads -> {}
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
                DatesViewActions.ShiftDueDate -> {}
                DatesViewActions.LoadMore -> {}
                is DatesViewActions.OpenEvent -> {}
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
                ProfileViewAction.EditAccountClick ->
                    navController.navigate(AppNavRoutes.EditProfile(accountJson = ""))
                ProfileViewAction.SwipeRefresh -> viewModel.updateAccount()
            }
        },
        onSettingsClick = { navController.navigate(AppNavRoutes.Settings) },
    )
}
