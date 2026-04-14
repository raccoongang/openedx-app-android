package org.openedx.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.ktor.http.encodeURLParameter
import org.koin.compose.viewmodel.koinViewModel
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
            modifier = modifier.statusBarsPadding(),
        ) {
            // =================== MAIN ===================
            composable<AppNavRoutes.Main> {
                MainScreen(navController = navController)
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

                androidx.compose.runtime.LaunchedEffect(state.loginSuccess) {
                    if (state.loginSuccess) {
                        navController.navigate(AppNavRoutes.Main()) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    windowSize = windowSize, state = state, uiMessage = uiMessage,
                    onEvent = { event ->
                        when (event) {
                            is AuthEvent.SignIn -> viewModel.login(event.login, event.password)
                            AuthEvent.ForgotPasswordClick -> navController.navigate(AppNavRoutes.RestorePassword)
                            AuthEvent.RegisterClick -> navController.navigate(AppNavRoutes.SignUp(route.courseId, route.infoType))
                            AuthEvent.BackClick -> navController.navigateUp()
                            else -> {}
                        }
                    },
                )
            }

            composable<AppNavRoutes.RestorePassword> {
                val viewModel: RestorePasswordViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState(RestorePasswordUIState.Initial)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                RestorePasswordScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    onBackClick = { navController.navigateUp() },
                    onRestoreButtonClick = { viewModel.passwordReset(it) },
                )
            }

            composable<AppNavRoutes.SignUp> { entry ->
                val route = entry.toRoute<AppNavRoutes.SignUp>()
                val viewModel: SignUpViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "", route.infoType ?: "")
                }
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    viewModel.getRegistrationFields()
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

                // Navigate to main screen on successful registration + login
                androidx.compose.runtime.LaunchedEffect(uiState?.successLogin) {
                    if (uiState?.successLogin == true) {
                        navController.navigate(AppNavRoutes.Main()) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                SignUpView(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    onBackClick = { navController.navigateUp() },
                    onFieldUpdated = { key, value -> viewModel.updateField(key, value) },
                    onRegisterClick = { viewModel.register() },
                    onHyperLinkClick = { links, link -> viewModel.openLink(links, link) },
                )
            }

            composable<AppNavRoutes.Logistration> { entry ->
                val route = entry.toRoute<AppNavRoutes.Logistration>()
                val viewModel: LogistrationViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "")
                }
                org.openedx.auth.presentation.logistration.LogistrationScreen(
                    onSearchClick = { query ->
                        if (viewModel.isDiscoveryTypeWebView) {
                            navController.navigate(AppNavRoutes.WebViewDiscovery(query))
                        } else {
                            navController.navigate(AppNavRoutes.NativeDiscovery(query))
                        }
                    },
                    onRegisterClick = { navController.navigate(AppNavRoutes.SignUp(route.courseId)) },
                    onSignInClick = { navController.navigate(AppNavRoutes.SignIn(route.courseId)) },
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
                        viewModel.saveWhatsNewVersion()
                        navController.navigate(AppNavRoutes.Main()) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onDoneClick = {
                        viewModel.logWhatsNewCompleted()
                        viewModel.saveWhatsNewVersion()
                        navController.navigate(AppNavRoutes.Main()) {
                            popUpTo(0) { inclusive = true }
                        }
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
                    onBackClick = { navController.navigateUp() },
                    onAction = { action ->
                        when (action) {
                            org.openedx.profile.presentation.settings.SettingsScreenAction.VideoSettingsClick ->
                                navController.navigate(AppNavRoutes.VideoSettings)
                            org.openedx.profile.presentation.settings.SettingsScreenAction.ManageAccountClick ->
                                navController.navigate(AppNavRoutes.ManageAccount)
                            org.openedx.profile.presentation.settings.SettingsScreenAction.CalendarSettingsClick ->
                                navController.navigate(AppNavRoutes.CalendarSettings)
                            org.openedx.profile.presentation.settings.SettingsScreenAction.LogoutClick ->
                                viewModel.logout()
                            org.openedx.profile.presentation.settings.SettingsScreenAction.SupportClick ->
                                viewModel.emailSupportClicked()
                            org.openedx.profile.presentation.settings.SettingsScreenAction.PrivacyPolicyClick ->
                                viewModel.privacyPolicyClicked()
                            org.openedx.profile.presentation.settings.SettingsScreenAction.CookiePolicyClick ->
                                viewModel.cookiePolicyClicked()
                            org.openedx.profile.presentation.settings.SettingsScreenAction.DataSellClick ->
                                viewModel.dataSellClicked()
                            org.openedx.profile.presentation.settings.SettingsScreenAction.TermsClick ->
                                viewModel.termsOfUseClicked()
                            org.openedx.profile.presentation.settings.SettingsScreenAction.FaqClick ->
                                viewModel.faqClicked()
                            org.openedx.profile.presentation.settings.SettingsScreenAction.AppVersionClick ->
                                viewModel.appVersionClickedEvent()
                        }
                    },
                )
            }

            composable<AppNavRoutes.ManageAccount> {
                val viewModel: ManageAccountViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.profile.presentation.manageaccount.compose.ManageAccountView(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    refreshing = false, onAction = { action ->
                        when (action) {
                            org.openedx.profile.presentation.manageaccount.compose.ManageAccountViewAction.BackClick ->
                                navController.navigateUp()
                            org.openedx.profile.presentation.manageaccount.compose.ManageAccountViewAction.EditAccountClick ->
                                navController.navigate(AppNavRoutes.EditProfile(accountJson = ""))
                            org.openedx.profile.presentation.manageaccount.compose.ManageAccountViewAction.DeleteAccount ->
                                navController.navigate(AppNavRoutes.DeleteProfile)
                            org.openedx.profile.presentation.manageaccount.compose.ManageAccountViewAction.SwipeRefresh ->
                                viewModel.updateAccount()
                        }
                    },
                )
            }

            composable<AppNavRoutes.DeleteProfile> {
                val viewModel: DeleteProfileViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState(org.openedx.profile.presentation.delete.DeleteProfileFragmentUIState.Initial)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.profile.presentation.delete.DeleteProfileScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    onDeleteClick = { viewModel.deleteProfile(it) },
                    onBackClick = { navController.navigateUp() },
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
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    onBackClick = { navController.navigateUp() },
                )
            }

            composable<AppNavRoutes.VideoSettings> {
                val viewModel: VideoSettingsViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val videoSettings by viewModel.videoSettings.collectAsState()
                videoSettings?.let { vs -> org.openedx.profile.presentation.video.VideoSettingsScreen(
                    windowSize = windowSize, videoSettings = vs,
                    wifiDownloadChanged = { viewModel.setWifiDownloadOnly(it) },
                    videoStreamingQualityClick = { navController.navigate(AppNavRoutes.VideoQuality(org.openedx.core.presentation.settings.video.VideoQualityType.Streaming.name)) },
                    videoDownloadQualityClick = { navController.navigate(AppNavRoutes.VideoQuality(org.openedx.core.presentation.settings.video.VideoQualityType.Download.name)) },
                    onBackClick = { navController.navigateUp() },
                ) }
            }

            composable<AppNavRoutes.WebContent> { entry ->
                val route = entry.toRoute<AppNavRoutes.WebContent>()
                org.openedx.core.ui.WebContentScreen(
                    windowSize = rememberWindowSize(), apiHostUrl = "",
                    title = route.title, contentUrl = route.url,
                    onBackClick = { navController.navigateUp() },
                )
            }

            composable<AppNavRoutes.CalendarSettings> {
                val vm: org.openedx.profile.presentation.calendar.CalendarViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState()
                org.openedx.profile.presentation.calendar.CalendarSettingsView(
                    windowSize = windowSize, uiState = uiState,
                    onCalendarSyncSwitchClick = { vm.setCalendarSyncEnabled(it, null) },
                    onRelativeDateSwitchClick = { vm.setRelativeDateEnabled(it) },
                    onChangeSyncOptionClick = {},
                    onCourseToSyncClick = { navController.navigate(AppNavRoutes.CoursesToSync) },
                    onBackClick = { navController.navigateUp() },
                )
            }
            composable<AppNavRoutes.CoursesToSync> {
                val viewModel: org.openedx.profile.presentation.calendar.CoursesToSyncViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.profile.presentation.calendar.CoursesToSyncView(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    onBackClick = { navController.navigateUp() },
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
                val quality by viewModel.videoQuality.collectAsState(viewModel.getCurrentVideoQuality())
                val title = if (viewModel.getQualityType() == org.openedx.core.presentation.settings.video.VideoQualityType.Streaming)
                    "Video Streaming Quality" else "Video Download Quality"
                org.openedx.core.presentation.settings.video.VideoQualityScreen(
                    windowSize = windowSize, title = title,
                    selectedVideoQuality = quality,
                    onQualityChanged = { viewModel.setVideoQuality(it) },
                    onBackClick = { navController.navigateUp() },
                )
            }
            composable<AppNavRoutes.EditProfile> { entry ->
                val route = entry.toRoute<AppNavRoutes.EditProfile>()
                val vm: org.openedx.profile.presentation.edit.EditProfileViewModel = koinViewModel {
                    parametersOf(null) // Account passed as null - VM fetches from cache
                }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState()
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val selectedImage by vm.selectedImageUri.collectAsState(null)
                val isDeleted by vm.deleteImage.collectAsState(false)
                val leaveDialog by vm.showLeaveDialog.collectAsState(false)
                org.openedx.profile.presentation.edit.EditProfileScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    selectedImageUri = selectedImage, isImageDeleted = isDeleted,
                    leaveDialog = leaveDialog,
                    onKeepEdit = { vm.setShowLeaveDialog(false) },
                    onDataChanged = { vm.profileDataChanged = it },
                    onLimitedProfileChange = {},
                    onBackClick = { navController.navigateUp() },
                    onSaveClick = { vm.updateAccount(it) },
                    onSelectImageClick = {},
                    onDeleteImageClick = { vm.deleteImage() },
                )
            }

            // =================== DISCOVERY ===================
            composable<AppNavRoutes.CourseDetails> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseDetails>()
                val viewModel: org.openedx.discovery.presentation.detail.CourseDetailsViewModel = koinViewModel {
                    parametersOf(route.courseId)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState(org.openedx.discovery.presentation.detail.CourseDetailsUIState.Loading)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.discovery.presentation.detail.CourseDetailsScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    apiHostUrl = viewModel.apiHostUrl, htmlBody = "",
                    hasInternetConnection = viewModel.hasInternetConnection,
                    isUserLoggedIn = viewModel.isUserLoggedIn,
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    onReloadClick = { viewModel.getCourseDetail() },
                    onBackClick = { navController.navigateUp() },
                    onButtonClick = {},
                    onRegisterClick = { navController.navigate(AppNavRoutes.SignUp()) },
                    onSignInClick = { navController.navigate(AppNavRoutes.SignIn()) },
                )
            }

            composable<AppNavRoutes.CourseSearch> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseSearch>()
                val vm: org.openedx.discovery.presentation.search.CourseSearchViewModel = koinViewModel { parametersOf(route.querySearch) }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState(org.openedx.discovery.presentation.search.CourseSearchUIState.Courses(emptyList(), 0))
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val canLoad by vm.canLoadMore.collectAsState(false)
                val updating by vm.isUpdating.collectAsState(false)
                org.openedx.discovery.presentation.search.CourseSearchScreen(
                    windowSize = windowSize, state = uiState, uiMessage = uiMessage,
                    apiHostUrl = vm.apiHostUrl, canLoadMore = canLoad, refreshing = updating,
                    querySearch = route.querySearch, isUserLoggedIn = vm.isUserLoggedIn,
                    isRegistrationEnabled = vm.isRegistrationEnabled,
                    onBackClick = { navController.navigateUp() },
                    onSearchTextChanged = { vm.search(it) },
                    onSwipeRefresh = {}, paginationCallback = { vm.fetchMore() },
                    onItemClick = { navController.navigate(AppNavRoutes.CourseDetails(it)) },
                    onRegisterClick = {}, onSignInClick = {},
                )
            }

            composable<AppNavRoutes.CourseInfo> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseInfo>()
                val vm: org.openedx.discovery.presentation.info.CourseInfoViewModel = koinViewModel {
                    parametersOf(route.courseId, route.infoType)
                }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState()
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                org.openedx.discovery.presentation.info.CourseInfoScreen(
                    windowSize = windowSize, uiState = uiState,
                    webViewUIState = vm.webViewState.value, uiMessage = uiMessage,
                    uriScheme = vm.uriScheme, isRegistrationEnabled = vm.isRegistrationEnabled,
                    userAgent = vm.appUserAgent, hasInternetConnection = vm.hasInternetConnection,
                    onWebViewUIAction = {},
                    onRegisterClick = { navController.navigate(AppNavRoutes.SignUp()) },
                    onSignInClick = { navController.navigate(AppNavRoutes.SignIn()) },
                    onUriClick = { _, _ -> },
                    onBackClick = { navController.navigateUp() },
                )
            }
            composable<AppNavRoutes.AllEnrolledCourses> {
                org.openedx.courses.presentation.AllEnrolledCoursesView(
                    onBack = { navController.navigateUp() },
                    onOpenCourse = { enrolled ->
                        navController.navigate(
                            AppNavRoutes.CourseContainer(
                                courseId = enrolled.course.id,
                                courseTitle = enrolled.course.name,
                            )
                        )
                    },
                    onSearch = { navController.navigate(AppNavRoutes.CourseSearch()) },
                )
            }
            composable<AppNavRoutes.Program> { entry ->
                val route = entry.toRoute<AppNavRoutes.Program>()
                val vm: org.openedx.discovery.presentation.program.ProgramViewModel = koinViewModel {
                    parametersOf(route.pathId)
                }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState()
                org.openedx.discovery.presentation.program.ProgramInfoScreen(
                    windowSize = windowSize, uiState = uiState,
                    contentUrl = vm.programConfig.programUrl,
                    cookieManager = vm.cookieManager,
                    uriScheme = vm.uriScheme, userAgent = vm.appUserAgent,
                    canShowBackBtn = !route.isNestedFragment,
                    isNestedFragment = route.isNestedFragment,
                    hasInternetConnection = vm.hasInternetConnection,
                    onWebViewUIAction = {},
                    onSettingsClick = { navController.navigate(AppNavRoutes.Settings) },
                    onUriClick = { _, _ -> },
                    onBackClick = { navController.navigateUp() },
                )
            }

            // =================== COURSE ===================
            composable<AppNavRoutes.CourseContainer> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseContainer>()
                val vm: org.openedx.course.presentation.home.CourseHomeViewModel = koinViewModel {
                    parametersOf(route.courseId, route.courseTitle)
                }
                val windowSize = rememberWindowSize()
                val pagerState = androidx.compose.foundation.pager.rememberPagerState { 5 }
                org.openedx.course.presentation.home.CourseHomeScreen(
                    windowSize = windowSize, viewModel = vm,
                    homePagerState = pagerState,
                )
            }

            composable<AppNavRoutes.CourseSection> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseSection>()
                val viewModel: org.openedx.course.presentation.section.CourseSectionViewModel = koinViewModel {
                    parametersOf(route.courseId, route.subSectionId)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState(org.openedx.course.presentation.section.CourseSectionUIState.Loading)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                org.openedx.course.presentation.section.CourseSectionScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    onBackClick = { navController.navigateUp() },
                    onItemClick = { block ->
                        navController.navigate(
                            AppNavRoutes.CourseUnitContainer(
                                courseId = route.courseId,
                                unitId = block.id,
                                mode = "FULL",
                            )
                        )
                    },
                )
            }
            composable<AppNavRoutes.CourseUnitContainer> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseUnitContainer>()
                val vm: org.openedx.course.presentation.unit.container.CourseUnitContainerViewModel = koinViewModel {
                    parametersOf(route.courseId, route.unitId, org.openedx.course.presentation.unit.container.CourseViewMode.valueOf(route.mode))
                }
                val blockCount by vm.verticalBlockCounts.collectAsState(0)
                val index by vm.indexInContainer.collectAsState(0)
                val currentBlock by vm.currentBlock.collectAsState(null)
                val config: org.openedx.core.config.Config = org.koin.compose.koinInject()

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    vm.loadBlocks(route.componentId)
                }

                androidx.compose.material3.Scaffold(
                    topBar = {
                        org.openedx.core.ui.Toolbar(
                            label = "Unit ${index + 1}/$blockCount",
                            canShowBackBtn = true,
                            onBackClick = { navController.navigateUp() },
                        )
                    }
                ) { padding ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                    ) {
                        val block = currentBlock
                        when {
                            block == null -> {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = androidx.compose.ui.Alignment.Center,
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator()
                                }
                            }
                            block.type == org.openedx.core.BlockType.HTML ||
                            block.type == org.openedx.core.BlockType.PROBLEM ||
                            block.type == org.openedx.core.BlockType.DRAG_AND_DROP_V2 ||
                            block.type == org.openedx.core.BlockType.OPENASSESSMENT ||
                            block.type == org.openedx.core.BlockType.WORD_CLOUD -> {
                                org.openedx.shared.ui.PlatformWebView(
                                    url = "${config.getApiHostURL()}${block.studentViewUrl}",
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            block.type == org.openedx.core.BlockType.VIDEO -> {
                                val videoUrl = block.studentViewData?.encodedVideos?.fallback?.url
                                    ?: block.studentViewData?.encodedVideos?.hls?.url ?: ""
                                if (videoUrl.isNotEmpty()) {
                                    org.openedx.shared.ui.PlatformVideoPlayer(
                                        url = videoUrl,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    org.openedx.shared.ui.PlatformWebView(
                                        url = "${config.getApiHostURL()}${block.studentViewUrl}",
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                            else -> {
                                org.openedx.shared.ui.PlatformWebView(
                                    url = "${config.getApiHostURL()}${block.studentViewUrl}",
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
            composable<AppNavRoutes.HandoutsWebView> { entry ->
                val route = entry.toRoute<AppNavRoutes.HandoutsWebView>()
                val viewModel: org.openedx.course.presentation.handouts.HandoutsViewModel = koinViewModel {
                    parametersOf(route.courseId, route.type)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                when (val state = uiState) {
                    is org.openedx.course.presentation.handouts.HandoutsUIState.HTMLContent -> {
                        org.openedx.shared.ui.PlatformWebView(
                            url = "data:text/html;charset=utf-8," +
                                state.htmlContent.encodeURLParameter(),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    else -> PlaceholderDestination("Loading ${route.type}...")
                }
            }
            composable<AppNavRoutes.VideoFullScreen> { entry ->
                val route = entry.toRoute<AppNavRoutes.VideoFullScreen>()
                org.openedx.shared.ui.PlatformVideoPlayer(
                    url = route.videoUrl,
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = route.isPlaying,
                )
            }
            composable<AppNavRoutes.YoutubeVideoFullScreen> { entry ->
                val route = entry.toRoute<AppNavRoutes.YoutubeVideoFullScreen>()
                org.openedx.shared.ui.PlatformWebView(
                    url = "https://www.youtube.com/embed/${route.videoUrl}?autoplay=1",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable<AppNavRoutes.DownloadQueue> { entry ->
                val route = entry.toRoute<AppNavRoutes.DownloadQueue>()
                val viewModel: org.openedx.course.settings.download.DownloadQueueViewModel = koinViewModel {
                    parametersOf(route.descendants)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                org.openedx.course.settings.download.DownloadQueueScreen(
                    windowSize = windowSize, uiState = uiState,
                    onBackClick = { navController.navigateUp() },
                    onDownloadClick = {},
                )
            }
            composable<AppNavRoutes.NoAccessCourseContainer> { entry ->
                val route = entry.toRoute<AppNavRoutes.NoAccessCourseContainer>()
                org.openedx.course.presentation.container.NoAccessCourseContainerScreen(
                    windowSize = rememberWindowSize(), title = route.title,
                    onBackClick = { navController.navigateUp() },
                )
            }

            // =================== DISCUSSION ===================
            composable<AppNavRoutes.DiscussionThreads> { entry ->
                val route = entry.toRoute<AppNavRoutes.DiscussionThreads>()
                val vm: org.openedx.discussion.presentation.threads.DiscussionThreadsViewModel = koinViewModel { parametersOf(route.courseId, route.topicId) }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState(org.openedx.discussion.presentation.threads.DiscussionThreadsUIState.Loading)
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val canLoad by vm.canLoadMore.collectAsState(false)
                val updating by vm.isUpdating.collectAsState(false)
                org.openedx.discussion.presentation.threads.DiscussionThreadsScreen(
                    windowSize = windowSize, title = route.title,
                    uiState = uiState ?: return@composable, uiMessage = uiMessage, canLoadMore = canLoad,
                    viewType = org.openedx.core.FragmentViewType.valueOf(route.viewType),
                    refreshing = updating,
                    onSwipeRefresh = { vm.updateThread("") }, updatedOrder = { vm.getThreadByType(it) },
                    updatedFilter = {}, onItemClick = { thread ->
                        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; encodeDefaults = true }
                            .encodeToString(org.openedx.discussion.domain.model.Thread.serializer(), thread)
                        navController.navigate(AppNavRoutes.DiscussionComments(threadJson = json))
                    },
                    onCreatePostClick = {
                        navController.navigate(AppNavRoutes.DiscussionAddThread(courseId = route.courseId, topicId = route.topicId))
                    }, paginationCallback = { vm.fetchMore() },
                    onBackClick = { navController.navigateUp() },
                )
            }

            composable<AppNavRoutes.DiscussionComments> { entry ->
                val route = entry.toRoute<AppNavRoutes.DiscussionComments>()
                // Thread passed as JSON string in route, deserialize
                val thread = try {
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString<org.openedx.discussion.domain.model.Thread>(route.threadJson)
                } catch (_: Exception) { null }
                if (thread != null) {
                    val vm: org.openedx.discussion.presentation.comments.DiscussionCommentsViewModel = koinViewModel {
                        parametersOf(thread)
                    }
                    val windowSize = rememberWindowSize()
                    val uiState by vm.uiState.collectAsState(org.openedx.discussion.presentation.comments.DiscussionCommentsUIState.Loading)
                    val uiMessage by vm.uiMessage.collectAsState(initial = null)
                    val canLoad by vm.canLoadMore.collectAsState(false)
                    val updating by vm.isUpdating.collectAsState(false)
                    org.openedx.discussion.presentation.comments.DiscussionCommentsScreen(
                        windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                        title = vm.title, canLoadMore = canLoad, refreshing = updating,
                        onSwipeRefresh = { vm.updateThreadComments() },
                        paginationCallBack = { vm.fetchMore() },
                        onItemClick = { _, _, _ -> }, onCommentClick = {},
                        onAddResponseClick = {}, onBackClick = { navController.navigateUp() },
                        onUserPhotoClick = {},
                    )
                } else PlaceholderDestination("Comments")
            }

            composable<AppNavRoutes.DiscussionResponses> { entry ->
                val route = entry.toRoute<AppNavRoutes.DiscussionResponses>()
                val comment = try {
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString<org.openedx.discussion.domain.model.DiscussionComment>(route.commentJson)
                } catch (_: Exception) { null }
                if (comment != null) {
                    val vm: org.openedx.discussion.presentation.responses.DiscussionResponsesViewModel = koinViewModel {
                        parametersOf(comment, route.isClosed)
                    }
                    val windowSize = rememberWindowSize()
                    val uiState by vm.uiState.collectAsState(org.openedx.discussion.presentation.responses.DiscussionResponsesUIState.Loading)
                    val uiMessage by vm.uiMessage.collectAsState(initial = null)
                    val canLoad by vm.canLoadMore.collectAsState(false)
                    val updating by vm.isUpdating.collectAsState(false)
                    org.openedx.discussion.presentation.responses.DiscussionResponsesScreen(
                        windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                        canLoadMore = canLoad, refreshing = updating,
                        onSwipeRefresh = { vm.updateCommentResponses() },
                        isClosed = route.isClosed,
                        paginationCallBack = { vm.fetchMore() },
                        addCommentClick = {},
                        onItemClick = { _, _, _ -> }, onBackClick = { navController.navigateUp() },
                        onUserPhotoClick = {},
                    )
                } else PlaceholderDestination("Responses")
            }

            composable<AppNavRoutes.DiscussionAddThread> { entry ->
                val route = entry.toRoute<AppNavRoutes.DiscussionAddThread>()
                val vm: org.openedx.discussion.presentation.threads.DiscussionAddThreadViewModel = koinViewModel { parametersOf(route.courseId, route.topicId) }
                val windowSize = rememberWindowSize()
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val isLoading by vm.isLoading.collectAsState(false)
                org.openedx.discussion.presentation.threads.DiscussionAddThreadScreen(
                    windowSize = windowSize,
                    topicData = route.topicId to "",
                    topics = vm.getHandledTopics(),
                    uiMessage = uiMessage, isLoading = isLoading,
                    onPostDiscussionClick = { type, title, body, topicId, follow -> vm.createThread(title, body, topicId, type, follow) },
                    onBackClick = { navController.navigateUp() },
                )
            }

            composable<AppNavRoutes.DiscussionSearchThread> { entry ->
                val route = entry.toRoute<AppNavRoutes.DiscussionSearchThread>()
                val vm: org.openedx.discussion.presentation.search.DiscussionSearchThreadViewModel = koinViewModel { parametersOf(route.courseId) }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState(org.openedx.discussion.presentation.search.DiscussionSearchThreadUIState.Threads(emptyList(), 0))
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val canLoad by vm.canLoadMore.collectAsState(false)
                val updating by vm.isUpdating.collectAsState(false)
                org.openedx.discussion.presentation.search.DiscussionSearchThreadScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    refreshing = updating, canLoadMore = canLoad,
                    onItemClick = {}, onSearchTextChanged = { vm.searchThreads(it) },
                    onSwipeRefresh = {}, paginationCallback = { vm.fetchMore() },
                    onBackClick = { navController.navigateUp() },
                )
            }
        }
    }
}
