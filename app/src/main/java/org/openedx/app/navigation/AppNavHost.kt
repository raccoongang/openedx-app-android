package org.openedx.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.app.MainScreen
import org.openedx.auth.presentation.logistration.LogistrationViewModel
import org.openedx.auth.presentation.restore.RestorePasswordUIState
import org.openedx.auth.presentation.restore.RestorePasswordViewModel
import org.openedx.auth.presentation.restore.compose.RestorePasswordScreen
import org.openedx.auth.presentation.signin.AuthEvent
import org.openedx.auth.presentation.signin.SignInViewModel
import org.openedx.auth.presentation.signin.compose.LoginScreen
import org.openedx.auth.presentation.signup.SignUpViewModel
import org.openedx.auth.presentation.signup.compose.SignUpView
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRequiredScreen
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.dates.presentation.dates.DatesScreen
import org.openedx.dates.presentation.dates.DatesViewModel
import org.openedx.dates.presentation.dates.DatesViewActions
import org.openedx.downloads.presentation.download.DownloadsScreen
import org.openedx.downloads.presentation.download.DownloadsUIState
import org.openedx.downloads.presentation.download.DownloadsViewModel
import org.openedx.downloads.presentation.download.DownloadsViewActions
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.profile.presentation.anothersaccount.AnothersProfileViewModel
import org.openedx.profile.presentation.anothersaccount.AnothersProfileUIState
import org.openedx.profile.presentation.delete.DeleteProfileViewModel
import org.openedx.profile.presentation.manageaccount.ManageAccountViewModel
import org.openedx.profile.presentation.manageaccount.ManageAccountUIState
import org.openedx.profile.presentation.profile.ProfileViewModel
import org.openedx.profile.presentation.profile.ProfileUIState
import org.openedx.profile.presentation.profile.compose.ProfileView
import org.openedx.profile.presentation.profile.compose.ProfileViewAction
import org.openedx.profile.presentation.settings.SettingsViewModel
import org.openedx.profile.presentation.settings.SettingsUIState
import org.openedx.profile.presentation.video.VideoSettingsViewModel
import org.openedx.whatsnew.presentation.whatsnew.WhatsNewScreen
import org.openedx.whatsnew.presentation.whatsnew.WhatsNewViewModel

@Composable
private fun PlaceholderDestination(name: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = name,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = AppNavRoutes.Main(),
    modifier: Modifier = Modifier,
) {
    OpenEdXTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
        ) {
            // =================== MAIN ===================
            composable<AppNavRoutes.Main> {
                MainScreen()
            }

            // =================== AUTH ===================
            composable<AppNavRoutes.SignIn> { entry ->
                val route = entry.toRoute<AppNavRoutes.SignIn>()
                val viewModel: SignInViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "", route.infoType ?: "", route.authCode)
                }
                val windowSize = rememberWindowSize()
                val state by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                LoginScreen(
                    windowSize = windowSize, state = state, uiMessage = uiMessage,
                    onEvent = { event ->
                        when (event) {
                            is AuthEvent.SignIn -> viewModel.login(event.login, event.password)
                            AuthEvent.ForgotPasswordClick -> navController.navigate(AppNavRoutes.RestorePassword)
                            AuthEvent.RegisterClick -> navController.navigate(AppNavRoutes.SignUp(route.courseId, route.infoType))
                            AuthEvent.BackClick -> navController.popBackStack()
                            else -> {}
                        }
                    },
                )
            }

            composable<AppNavRoutes.RestorePassword> {
                val viewModel: RestorePasswordViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.observeAsState(RestorePasswordUIState.Initial)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                RestorePasswordScreen(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    onBackClick = { navController.popBackStack() },
                    onRestoreButtonClick = { viewModel.passwordReset(it) },
                )
            }

            composable<AppNavRoutes.SignUp> { entry ->
                val route = entry.toRoute<AppNavRoutes.SignUp>()
                val viewModel: SignUpViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "", route.infoType ?: "")
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                SignUpView(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    onBackClick = { navController.popBackStack() },
                    onFieldUpdated = { key, value -> viewModel.updateField(key, value) },
                    onRegisterClick = { viewModel.register() },
                    onHyperLinkClick = { links, link -> viewModel.openLink(null, links, link) },
                )
            }

            composable<AppNavRoutes.Logistration> { entry ->
                val route = entry.toRoute<AppNavRoutes.Logistration>()
                val viewModel: LogistrationViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "")
                }
                org.openedx.auth.presentation.logistration.LogistrationScreen(
                    onSearchClick = { viewModel.navigateToDiscovery(null, it) },
                    onRegisterClick = { viewModel.navigateToSignUp(null) },
                    onSignInClick = { viewModel.navigateToSignIn(null) },
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                )
            }

            composable<AppNavRoutes.WhatsNew> { entry ->
                val route = entry.toRoute<AppNavRoutes.WhatsNew>()
                val viewModel: WhatsNewViewModel = koinViewModel {
                    parametersOf(route.courseId, route.infoType)
                }
                val windowSize = rememberWindowSize()
                WhatsNewScreen(
                    windowSize = windowSize,
                    whatsNewItem = viewModel.whatsNewItem.value,
                    onCloseClick = {
                        viewModel.logWhatsNewDismissed(it)
                        viewModel.navigateToMain(null)
                    },
                    onDoneClick = {
                        viewModel.logWhatsNewCompleted()
                        viewModel.navigateToMain(null)
                    },
                )
            }

            // =================== UPGRADE ===================
            composable<AppNavRoutes.UpgradeRequired> {
                AppUpgradeRequiredScreen(onUpdateClick = {})
            }

            // =================== PROFILE ===================
            composable<AppNavRoutes.Settings> {
                val viewModel: SettingsViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                org.openedx.profile.presentation.settings.SettingsScreen(
                    windowSize = windowSize, uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onAction = {},
                )
            }

            composable<AppNavRoutes.ManageAccount> {
                val viewModel: ManageAccountViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.profile.presentation.manageaccount.compose.ManageAccountView(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    refreshing = false, onAction = {},
                )
            }

            composable<AppNavRoutes.DeleteProfile> {
                val viewModel: DeleteProfileViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.observeAsState(org.openedx.profile.presentation.delete.DeleteProfileFragmentUIState.Initial)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.profile.presentation.delete.DeleteProfileScreen(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    onDeleteClick = { viewModel.deleteProfile(it) },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable<AppNavRoutes.AnothersProfile> { entry ->
                val route = entry.toRoute<AppNavRoutes.AnothersProfile>()
                val viewModel: AnothersProfileViewModel = koinViewModel {
                    parametersOf(route.username)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.profile.presentation.anothersaccount.AnothersProfileScreen(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable<AppNavRoutes.VideoSettings> {
                val viewModel: VideoSettingsViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val videoSettings by viewModel.videoSettings.observeAsState()
                videoSettings?.let { vs -> org.openedx.profile.presentation.video.VideoSettingsScreen(
                    windowSize = windowSize, videoSettings = vs,
                    wifiDownloadChanged = { viewModel.setWifiDownloadOnly(it) },
                    videoStreamingQualityClick = { viewModel.navigateToVideoStreamingQuality(null) },
                    videoDownloadQualityClick = { viewModel.navigateToVideoDownloadQuality(null) },
                    onBackClick = { navController.popBackStack() },
                ) }
            }

            composable<AppNavRoutes.WebContent> { entry ->
                val route = entry.toRoute<AppNavRoutes.WebContent>()
                org.openedx.core.ui.WebContentScreen(
                    windowSize = rememberWindowSize(), apiHostUrl = "",
                    title = route.title, contentUrl = route.url,
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable<AppNavRoutes.CalendarSettings> { PlaceholderDestination("Calendar Settings") }
            composable<AppNavRoutes.CoursesToSync> {
                val viewModel: org.openedx.profile.presentation.calendar.CoursesToSyncViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.profile.presentation.calendar.CoursesToSyncView(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    onBackClick = { navController.popBackStack() },
                    onHideInactiveCoursesSwitchClick = { viewModel.setHideInactiveCoursesEnabled(it) },
                    onCourseSyncCheckChange = { enabled, courseId -> viewModel.setCourseSyncEnabled(enabled, courseId) },
                )
            }
            composable<AppNavRoutes.VideoQuality> { entry ->
                val route = entry.toRoute<AppNavRoutes.VideoQuality>()
                val viewModel: org.openedx.core.presentation.settings.video.VideoQualityViewModel = koinViewModel {
                    parametersOf(route.videoQualityType)
                }
                val windowSize = rememberWindowSize()
                val quality by viewModel.videoQuality.observeAsState(viewModel.getCurrentVideoQuality())
                val title = if (viewModel.getQualityType() == org.openedx.core.presentation.settings.video.VideoQualityType.Streaming)
                    "Video Streaming Quality" else "Video Download Quality"
                org.openedx.core.presentation.settings.video.VideoQualityScreen(
                    windowSize = windowSize, title = title,
                    selectedVideoQuality = quality,
                    onQualityChanged = { viewModel.setVideoQuality(it) },
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable<AppNavRoutes.EditProfile> { PlaceholderDestination("Edit Profile") }

            // =================== DISCOVERY ===================
            composable<AppNavRoutes.CourseDetails> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseDetails>()
                val viewModel: org.openedx.discovery.presentation.detail.CourseDetailsViewModel = koinViewModel {
                    parametersOf(route.courseId)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.observeAsState(org.openedx.discovery.presentation.detail.CourseDetailsUIState.Loading)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.discovery.presentation.detail.CourseDetailsScreen(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    apiHostUrl = viewModel.apiHostUrl, htmlBody = "",
                    hasInternetConnection = viewModel.hasInternetConnection,
                    isUserLoggedIn = viewModel.isUserLoggedIn,
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    onReloadClick = { viewModel.getCourseDetail() },
                    onBackClick = { navController.popBackStack() },
                    onButtonClick = {},
                    onRegisterClick = { navController.navigate(AppNavRoutes.SignUp()) },
                    onSignInClick = { navController.navigate(AppNavRoutes.SignIn()) },
                )
            }

            composable<AppNavRoutes.CourseSearch> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseSearch>()
                val vm: org.openedx.discovery.presentation.search.CourseSearchViewModel = koinViewModel { parametersOf(route.querySearch) }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.observeAsState(org.openedx.discovery.presentation.search.CourseSearchUIState.Courses(emptyList(), 0))
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val canLoad by vm.canLoadMore.observeAsState(false)
                val updating by vm.isUpdating.observeAsState(false)
                org.openedx.discovery.presentation.search.CourseSearchScreen(
                    windowSize = windowSize, state = uiState, uiMessage = uiMessage,
                    apiHostUrl = vm.apiHostUrl, canLoadMore = canLoad, refreshing = updating,
                    querySearch = route.querySearch, isUserLoggedIn = vm.isUserLoggedIn,
                    isRegistrationEnabled = vm.isRegistrationEnabled,
                    onBackClick = { navController.popBackStack() },
                    onSearchTextChanged = { vm.search(it) },
                    onSwipeRefresh = {}, paginationCallback = { vm.fetchMore() },
                    onItemClick = { navController.navigate(AppNavRoutes.CourseDetails(it)) },
                    onRegisterClick = {}, onSignInClick = {},
                )
            }

            composable<AppNavRoutes.CourseInfo> { PlaceholderDestination("Course Info") }
            composable<AppNavRoutes.AllEnrolledCourses> {
                org.openedx.courses.presentation.AllEnrolledCoursesView(fragmentManager = null)
            }
            composable<AppNavRoutes.Program> { PlaceholderDestination("Programs") }

            // =================== COURSE ===================
            composable<AppNavRoutes.CourseContainer> { PlaceholderDestination("Course") }

            composable<AppNavRoutes.CourseSection> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseSection>()
                val viewModel: org.openedx.course.presentation.section.CourseSectionViewModel = koinViewModel {
                    parametersOf(route.courseId, route.subSectionId)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.observeAsState(org.openedx.course.presentation.section.CourseSectionUIState.Loading)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.course.presentation.section.CourseSectionScreen(
                    windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                    onBackClick = { navController.popBackStack() },
                    onItemClick = {},
                )
            }
            composable<AppNavRoutes.CourseUnitContainer> { PlaceholderDestination("Course Unit") }
            composable<AppNavRoutes.HandoutsWebView> { entry ->
                val route = entry.toRoute<AppNavRoutes.HandoutsWebView>()
                val viewModel: org.openedx.course.presentation.handouts.HandoutsViewModel = koinViewModel {
                    parametersOf(route.courseId, route.type)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                // HandoutsWebView displays HTML content - needs WebView composable
                // For now showing handouts/announcements title
                PlaceholderDestination("${route.type}: ${route.courseId}")
            }
            composable<AppNavRoutes.VideoFullScreen> { PlaceholderDestination("Video") }
            composable<AppNavRoutes.YoutubeVideoFullScreen> { PlaceholderDestination("YouTube") }
            composable<AppNavRoutes.DownloadQueue> { entry ->
                val route = entry.toRoute<AppNavRoutes.DownloadQueue>()
                val viewModel: org.openedx.course.settings.download.DownloadQueueViewModel = koinViewModel {
                    parametersOf(route.descendants)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                org.openedx.course.settings.download.DownloadQueueScreen(
                    windowSize = windowSize, uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onDownloadClick = {},
                )
            }
            composable<AppNavRoutes.NoAccessCourseContainer> { entry ->
                val route = entry.toRoute<AppNavRoutes.NoAccessCourseContainer>()
                org.openedx.course.presentation.container.NoAccessCourseContainerScreen(
                    windowSize = rememberWindowSize(), title = route.title,
                    onBackClick = { navController.popBackStack() },
                )
            }

            // =================== DISCUSSION ===================
            composable<AppNavRoutes.DiscussionThreads> { PlaceholderDestination("Discussion Threads") }

            composable<AppNavRoutes.DiscussionComments> { PlaceholderDestination("Comments") }
            composable<AppNavRoutes.DiscussionResponses> { PlaceholderDestination("Responses") }

            composable<AppNavRoutes.DiscussionAddThread> { PlaceholderDestination("Add Thread") }

            composable<AppNavRoutes.DiscussionSearchThread> { PlaceholderDestination("Search") }
        }
    }
}
