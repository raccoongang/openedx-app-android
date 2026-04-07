package org.openedx.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.app.MainScreen
import org.openedx.auth.presentation.restore.RestorePasswordUIState
import org.openedx.auth.presentation.restore.RestorePasswordViewModel
import org.openedx.auth.presentation.restore.compose.RestorePasswordScreen
import org.openedx.auth.presentation.signin.AuthEvent
import org.openedx.auth.presentation.signin.SignInViewModel
import org.openedx.auth.presentation.signin.compose.LoginScreen
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRequiredScreen
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.dates.presentation.dates.DatesScreen
import org.openedx.dates.presentation.dates.DatesViewModel
import org.openedx.dates.presentation.dates.DatesViewActions
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.profile.presentation.manageaccount.ManageAccountViewModel
import org.openedx.profile.presentation.manageaccount.ManageAccountUIState
import org.openedx.profile.presentation.profile.ProfileViewModel
import org.openedx.profile.presentation.profile.ProfileUIState
import org.openedx.profile.presentation.settings.SettingsViewModel
import org.openedx.profile.presentation.settings.SettingsUIState

/**
 * Root navigation host for the app.
 * Contains composable destinations that replace Fragments.
 *
 * Currently supports: Main, SignIn, RestorePassword, UpgradeRequired,
 * Settings, ManageAccount, and placeholder destinations for others.
 *
 * Feature flag USE_COMPOSE_NAVIGATION in AppActivity controls activation.
 */
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
            // ============================================
            // MAIN SCREEN (bottom navigation)
            // ============================================
            composable<AppNavRoutes.Main> {
                MainScreen(
                    onTabSelected = { /* tab switching handled internally */ },
                )
            }

            // ============================================
            // AUTH FLOW
            // ============================================
            composable<AppNavRoutes.SignIn> { entry ->
                val route = entry.toRoute<AppNavRoutes.SignIn>()
                val viewModel: SignInViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "", route.infoType ?: "", route.authCode)
                }
                val windowSize = rememberWindowSize()
                val state by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

                LoginScreen(
                    windowSize = windowSize,
                    state = state,
                    uiMessage = uiMessage,
                    onEvent = { event ->
                        when (event) {
                            is AuthEvent.SignIn -> viewModel.login(event.login, event.password)
                            AuthEvent.ForgotPasswordClick -> navController.navigate(AppNavRoutes.RestorePassword)
                            AuthEvent.RegisterClick -> navController.navigate(
                                AppNavRoutes.SignUp(route.courseId, route.infoType)
                            )
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
                    windowSize = windowSize,
                    uiState = uiState,
                    uiMessage = uiMessage,
                    onBackClick = { navController.popBackStack() },
                    onRestoreButtonClick = { viewModel.passwordReset(it) },
                )
            }

            composable<AppNavRoutes.SignUp> {
                // TODO: Wire SignUpView - needs social auth integration
            }

            composable<AppNavRoutes.Logistration> {
                // TODO: Wire LogistrationScreen
            }

            composable<AppNavRoutes.WhatsNew> {
                // TODO: Wire WhatsNewScreen
            }

            // ============================================
            // UPGRADE REQUIRED
            // ============================================
            composable<AppNavRoutes.UpgradeRequired> {
                AppUpgradeRequiredScreen(
                    onUpdateClick = { /* open play market */ },
                )
            }

            // ============================================
            // PROFILE FLOW
            // ============================================
            composable<AppNavRoutes.Settings> {
                val viewModel: SettingsViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

                org.openedx.profile.presentation.settings.SettingsScreen(
                    windowSize = windowSize,
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onAction = { action ->
                        // TODO: Wire settings actions
                    },
                )
            }

            composable<AppNavRoutes.ManageAccount> {
                val viewModel: ManageAccountViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

                org.openedx.profile.presentation.manageaccount.compose.ManageAccountView(
                    windowSize = windowSize,
                    uiState = uiState,
                    uiMessage = uiMessage,
                    refreshing = false,
                    onAction = { action ->
                        // TODO: Wire manage account actions
                    },
                )
            }

            composable<AppNavRoutes.VideoQuality> { entry ->
                // TODO: Wire VideoQualityScreen
            }

            composable<AppNavRoutes.VideoSettings> {
                // TODO: Wire VideoSettingsScreen
            }

            composable<AppNavRoutes.DeleteProfile> {
                // TODO: Wire DeleteProfileScreen
            }

            composable<AppNavRoutes.EditProfile> {
                // TODO: Wire EditProfileScreen
            }

            composable<AppNavRoutes.WebContent> { entry ->
                val route = entry.toRoute<AppNavRoutes.WebContent>()
                org.openedx.core.ui.WebContentScreen(
                    windowSize = rememberWindowSize(),
                    apiHostUrl = "", // TODO: inject from Config
                    title = route.title,
                    contentUrl = route.url,
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable<AppNavRoutes.CalendarSettings> {
                // TODO: Wire CalendarSettingsScreen
            }

            composable<AppNavRoutes.CoursesToSync> {
                // TODO: Wire CoursesToSyncScreen
            }

            composable<AppNavRoutes.AnothersProfile> { entry ->
                // TODO: Wire AnothersProfileScreen
            }

            // ============================================
            // DISCOVERY
            // ============================================
            composable<AppNavRoutes.CourseDetails> { entry ->
                // TODO: Wire CourseDetailsScreen
            }

            composable<AppNavRoutes.CourseSearch> {
                // TODO: Wire CourseSearchScreen
            }

            composable<AppNavRoutes.CourseInfo> { entry ->
                // TODO: Wire CourseInfoScreen
            }

            composable<AppNavRoutes.AllEnrolledCourses> {
                // TODO: Wire AllEnrolledCoursesView
            }

            // ============================================
            // COURSE
            // ============================================
            composable<AppNavRoutes.CourseContainer> { entry ->
                // TODO: Wire CourseContainerScreen (complex - has tabs)
            }

            composable<AppNavRoutes.CourseSection> { entry ->
                // TODO: Wire CourseSectionScreen
            }

            composable<AppNavRoutes.CourseUnitContainer> { entry ->
                // TODO: Wire CourseUnitContainerScreen
            }

            composable<AppNavRoutes.HandoutsWebView> { entry ->
                // TODO: Wire HandoutsWebViewScreen
            }

            composable<AppNavRoutes.VideoFullScreen> { entry ->
                // TODO: Wire VideoFullScreenScreen
            }

            composable<AppNavRoutes.DownloadQueue> { entry ->
                // TODO: Wire DownloadQueueScreen
            }

            // ============================================
            // DISCUSSION
            // ============================================
            composable<AppNavRoutes.DiscussionThreads> { entry ->
                // TODO: Wire DiscussionThreadsScreen
            }

            composable<AppNavRoutes.DiscussionComments> { entry ->
                // TODO: Wire DiscussionCommentsScreen
            }

            composable<AppNavRoutes.DiscussionResponses> { entry ->
                // TODO: Wire DiscussionResponsesScreen
            }

            composable<AppNavRoutes.DiscussionAddThread> { entry ->
                // TODO: Wire DiscussionAddThreadScreen
            }

            composable<AppNavRoutes.DiscussionSearchThread> { entry ->
                // TODO: Wire DiscussionSearchThreadScreen
            }
        }
    }
}
