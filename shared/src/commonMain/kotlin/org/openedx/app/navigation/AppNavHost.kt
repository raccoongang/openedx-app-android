package org.openedx.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.openedx.core.core_leaving_the_app
import org.openedx.core.core_leaving_the_app_message
import org.openedx.core.core_video_download_quality
import org.openedx.core.core_video_streaming_quality
import org.openedx.core.presentation.logExternalLinkAlert
import org.openedx.core.presentation.logExternalLinkAlertAction
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
private fun HtmlBlockContent(
    block: org.openedx.core.domain.model.Block,
    courseId: String,
    url: String,
) {
    val viewModel: org.openedx.course.presentation.unit.html.HtmlUnitViewModel = koinViewModel {
        parametersOf(block.id, courseId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val injectJSList by viewModel.injectJSList.collectAsState()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.onWebPageLoading() }

    if (uiState is org.openedx.course.presentation.unit.html.HtmlUnitUIState.Initialization) return

    val loadedJsonProgress =
        (uiState as? org.openedx.course.presentation.unit.html.HtmlUnitUIState.Loaded)?.jsonProgress

    org.openedx.shared.ui.PlatformWebView(
        url = url,
        modifier = Modifier.fillMaxSize(),
        onPageStarted = { viewModel.onWebPageLoading() },
        onPageFinished = {
            viewModel.onWebPageLoaded()
            viewModel.setWebPageLoaded()
        },
        onPageError = { viewModel.onWebPageLoadError() },
        onCompletionSet = { viewModel.notifyCompletionSet() },
        onPostMessage = { jsonProgress -> viewModel.saveXBlockProgress(jsonProgress) },
        injectJSList = injectJSList,
        jsonProgress = loadedJsonProgress,
        isDarkMode = isDark,
    )
}

@Composable
private fun UnitBlockContent(
    block: org.openedx.core.domain.model.Block,
    config: org.openedx.core.config.Config,
    courseId: String = "",
    onNavigateToFullScreen: (videoUrl: String, isYoutube: Boolean, videoTime: Long, isPlaying: Boolean) -> Unit = { _, _, _, _ -> },
) {
    fun resolveUrl(url: String): String =
        if (url.startsWith("http://") || url.startsWith("https://")) url
        else "${config.getApiHostURL()}$url"

    when {
        block.type == org.openedx.core.BlockType.HTML ||
        block.type == org.openedx.core.BlockType.PROBLEM ||
        block.type == org.openedx.core.BlockType.DRAG_AND_DROP_V2 ||
        block.type == org.openedx.core.BlockType.OPENASSESSMENT ||
        block.type == org.openedx.core.BlockType.WORD_CLOUD ||
        block.type == org.openedx.core.BlockType.SURVEY ||
        block.type == org.openedx.core.BlockType.LTI_CONSUMER -> {
            HtmlBlockContent(
                block = block,
                courseId = courseId,
                url = resolveUrl(block.studentViewUrl),
            )
        }
        block.type == org.openedx.core.BlockType.VIDEO -> {
            VideoUnitContent(
                block = block,
                config = config,
                courseId = courseId,
                onNavigateToFullScreen = onNavigateToFullScreen,
            )
        }
        block.type == org.openedx.core.BlockType.DISCUSSION -> {
            org.openedx.shared.ui.PlatformWebView(
                url = resolveUrl(block.studentViewUrl),
                modifier = Modifier.fillMaxSize(),
            )
        }
        block.type == org.openedx.core.BlockType.OTHERS -> {
            org.openedx.course.presentation.unit.NotAvailableUnitScreen(
                windowSize = org.openedx.foundation.presentation.rememberWindowSize(),
                unitType = org.openedx.course.presentation.unit.NotAvailableUnitType.MOBILE_UNSUPPORTED,
                blockUrl = block.lmsWebUrl,
            )
        }
        else -> {
            org.openedx.shared.ui.PlatformWebView(
                url = resolveUrl(block.studentViewUrl),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Video unit content — mirrors the original YoutubeVideoUnitFragment / VideoUnitFragment:
 *  - VideoUnitViewModel for progress tracking, completion, analytics, subtitles
 *  - Video title
 *  - Player in CardView (rounded corners)
 *  - Subtitles with auto-scroll and language selector
 *  - Connection error view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoUnitContent(
    block: org.openedx.core.domain.model.Block,
    config: org.openedx.core.config.Config,
    courseId: String = "",
    onNavigateToFullScreen: (videoUrl: String, isYoutube: Boolean, videoTime: Long, isPlaying: Boolean) -> Unit = { _, _, _, _ -> },
) {
    fun resolveUrl(url: String): String =
        if (url.startsWith("http://") || url.startsWith("https://")) url
        else "${config.getApiHostURL()}$url"

    val encodedVideos = block.studentViewData?.encodedVideos
    val videoUrl = encodedVideos?.videoUrl ?: ""
    val youtubeUrl = encodedVideos?.youtube?.url ?: ""
    val effectiveVideoUrl = videoUrl.ifEmpty { youtubeUrl }
    val isYoutube = videoUrl.isEmpty() && youtubeUrl.isNotEmpty()
    val medium = if (isYoutube) "youtube" else "native"

    // Create VideoUnitViewModel (same as original fragment — params: courseId, videoUrl, blockId)
    val viewModel: org.openedx.course.presentation.unit.video.VideoUnitViewModel = koinViewModel(
        key = block.blockId,
    ) {
        parametersOf(courseId, effectiveVideoUrl, block.blockId)
    }
    val appReviewManager: org.openedx.core.presentation.dialog.appreview.AppReviewManager =
        org.koin.compose.koinInject()

    // Initialize transcripts from block data
    androidx.compose.runtime.LaunchedEffect(block.blockId) {
        block.studentViewData?.transcripts?.let { transcripts ->
            viewModel.transcripts = transcripts
            viewModel.downloadSubtitles()
        }
    }

    // Save progress when leaving
    androidx.compose.runtime.DisposableEffect(block.blockId) {
        onDispose { /* VM onPause saves progress via lifecycle observer */ }
    }

    val currentVideoTime by viewModel.currentVideoTime.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState(0)
    val transcriptObject by viewModel.transcriptObject.collectAsState()
    val startSeconds = currentVideoTime / 1000f

    // Track completion at 80% (matches original: VIDEO_COMPLETION_THRESHOLD = 0.8f)
    var isCompletionCalled by remember { mutableStateOf(false) }

    // Player composable — reused in both portrait and landscape layouts.
    val playerContent: @Composable (Modifier) -> Unit = { playerModifier ->
        Card(
            modifier = playerModifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            when {
                videoUrl.isNotEmpty() -> {
                    val corePrefs: org.openedx.core.data.storage.CorePreferences =
                        org.koin.compose.koinInject()
                    org.openedx.shared.ui.PlatformVideoPlayer(
                        url = videoUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        maxVideoHeight = corePrefs.videoSettings.videoStreamingQuality.height,
                        onProgressChanged = { positionMs ->
                            viewModel.setCurrentVideoTime(positionMs)
                            if (viewModel.duration > 0) {
                                val pct = positionMs.toDouble() / viewModel.duration.toDouble()
                                if (pct >= 0.8 && !isCompletionCalled) {
                                    isCompletionCalled = true
                                    viewModel.markBlockCompleted(block.blockId, medium)
                                }
                                if (pct >= 0.99 && !appReviewManager.isDialogShowed) {
                                    appReviewManager.tryToOpenRateDialog()
                                }
                            }
                        },
                        onEnded = {
                            if (!isCompletionCalled) {
                                isCompletionCalled = true
                                viewModel.markBlockCompleted(block.blockId, medium)
                            }
                            if (!appReviewManager.isDialogShowed) {
                                appReviewManager.tryToOpenRateDialog()
                            }
                        },
                        onPlayPauseChanged = { playing ->
                            viewModel.isPlaying = playing
                            viewModel.logPlayPauseEvent(
                                effectiveVideoUrl,
                                playing,
                                viewModel.getCurrentVideoTime(),
                                medium,
                            )
                        },
                        onSpeedChanged = { speed ->
                            viewModel.logVideoSpeedEvent(
                                effectiveVideoUrl,
                                speed,
                                viewModel.getCurrentVideoTime(),
                                medium,
                            )
                        },
                    )
                }
                youtubeUrl.isNotEmpty() -> {
                    val videoId = extractYouTubeVideoId(youtubeUrl)
                    org.openedx.shared.ui.PlatformYouTubePlayer(
                        videoId = videoId,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        startSeconds = startSeconds,
                        onReady = {
                            viewModel.logLoadedCompletedEvent(effectiveVideoUrl, true, currentVideoTime, medium)
                        },
                        onStateChange = { isPlaying ->
                            viewModel.isPlaying = isPlaying
                            viewModel.logPlayPauseEvent(effectiveVideoUrl, isPlaying, viewModel.getCurrentVideoTime(), medium)
                        },
                        onCurrentSecond = { second ->
                            viewModel.setCurrentVideoTime((second * 1000).toLong())
                            if (viewModel.duration > 0) {
                                val pct = second / (viewModel.duration / 1000f)
                                if (pct >= 0.8f && !isCompletionCalled) {
                                    isCompletionCalled = true
                                    viewModel.markBlockCompleted(block.blockId, medium)
                                }
                                if (pct >= 0.99f && !appReviewManager.isDialogShowed) {
                                    appReviewManager.tryToOpenRateDialog()
                                }
                            }
                        },
                        onVideoDuration = { duration ->
                            viewModel.duration = (duration * 1000).toLong()
                        },
                        onFullscreenClick = {
                            onNavigateToFullScreen(youtubeUrl, true, viewModel.getCurrentVideoTime(), viewModel.isPlaying)
                        },
                    )
                }
                else -> {
                    org.openedx.shared.ui.PlatformWebView(
                        url = resolveUrl(block.studentViewUrl),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                }
            }
        }
    }

    var showLanguageSheet by remember { mutableStateOf(false) }
    val languageSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCoroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Subtitles composable — reused in both orientations.
    val subtitlesContent: @Composable (Modifier) -> Unit = { subtitlesModifier ->
        val typedTranscript = transcriptObject as? org.openedx.core.domain.model.TranscriptObject
        if (typedTranscript != null) {
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            org.openedx.course.presentation.ui.VideoSubtitles(
                listState = listState,
                timedTextObject = typedTranscript,
                subtitleLanguage = viewModel.transcriptLanguage,
                showSubtitleLanguage = viewModel.transcripts.size > 1,
                currentIndex = currentIndex,
                onTranscriptClick = { caption ->
                    // Seek to caption start time — parse "HH:mm:ss,SSS" format to seconds
                    val parts = caption.startTime.replace(",", ".").split(":")
                    val seekSeconds = if (parts.size == 3) {
                        parts[0].toFloatOrNull()?.times(3600) ?: 0f +
                        (parts[1].toFloatOrNull()?.times(60) ?: 0f) +
                        (parts[2].toFloatOrNull() ?: 0f)
                    } else 0f
                    viewModel.setCurrentVideoTime((seekSeconds * 1000).toLong())
                },
                onSettingsClick = {
                    if (viewModel.transcripts.size > 1) showLanguageSheet = true
                },
            )
        }
    }

    if (showLanguageSheet) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = languageSheetState,
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
            ) {
                androidx.compose.material3.Text(
                    text = "Subtitle language",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                viewModel.transcripts.keys.forEach { lang ->
                    val selected = lang == viewModel.transcriptLanguage
                    androidx.compose.material3.Text(
                        text = lang,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = if (selected)
                            androidx.compose.material3.MaterialTheme.colorScheme.primary
                        else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setTranscriptLanguage(lang)
                                sheetCoroutineScope.launch {
                                    languageSheetState.hide()
                                    showLanguageSheet = false
                                }
                            }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            // Landscape — mirrors native `layout-land/fragment_youtube_video_unit.xml`:
            // Card on the left (60% width, 16:9), subtitles on the right, with a
            // connection-error overlay that covers the whole content when offline.
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    playerContent(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(16f / 9f)
                    )
                    subtitlesContent(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp)
                    )
                }
                if (!viewModel.hasInternetConnection) {
                    org.openedx.core.ui.ConnectionErrorView(
                        onReloadClick = { /* retry */ }
                    )
                }
            }
        } else {
            // Portrait — original stacked layout.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            ) {
                Text(
                    text = block.displayName,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.padding(top = 16.dp))

                if (!viewModel.hasInternetConnection) {
                    org.openedx.core.ui.ConnectionErrorView(
                        onReloadClick = { /* retry */ }
                    )
                }

                playerContent(Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.padding(top = 28.dp))

                subtitlesContent(Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * Extracts the 11-character YouTube video ID from various URL formats:
 * - https://www.youtube.com/watch?v=VIDEO_ID
 * - https://youtu.be/VIDEO_ID
 * - https://www.youtube.com/embed/VIDEO_ID
 * - Just the video ID itself
 */
private fun extractYouTubeVideoId(url: String): String {
    // If it's already just a video ID (11 chars, no slashes or dots)
    if (url.length == 11 && !url.contains("/") && !url.contains(".")) return url

    val regex = Regex(
        "^(?:https?://)?(?:www\\.)?(?:youtube\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?)|.*[?&]v=)|youtu\\.be/)" +
                "([^\"&?/\\s]{11})",
        RegexOption.IGNORE_CASE,
    )
    return regex.find(url)?.groups?.get(1)?.value ?: url
}

/**
 * Builds a responsive HTML page with a YouTube iframe embed.
 * Matches the original Android app's IFrame player behavior.
 */
private fun buildYouTubeEmbedHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; }
                html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
                .container {
                    position: relative;
                    width: 100%;
                    padding-bottom: 56.25%; /* 16:9 aspect ratio */
                }
                .container iframe {
                    position: absolute;
                    top: 0; left: 0;
                    width: 100%;
                    height: 100%;
                    border: 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <iframe
                    src="https://www.youtube.com/embed/$videoId?rel=0&playsinline=1&enablejsapi=1"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                    allowfullscreen>
                </iframe>
            </div>
        </body>
        </html>
    """.trimIndent()
}

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
            composable<AppNavRoutes.Main> { entry ->
                val route = entry.toRoute<AppNavRoutes.Main>()
                MainScreen(
                    navController = navController,
                    openTab = route.openTab,
                    courseId = route.courseId,
                    infoType = route.infoType,
                )
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
                val appUpgradeEvent by viewModel.appUpgradeEvent.collectAsState()

                androidx.compose.runtime.LaunchedEffect(viewModel.authCode) {
                    if (viewModel.authCode.isNotEmpty() &&
                        !state.loginFailure && !state.loginSuccess
                    ) {
                        viewModel.signInAuthCode(viewModel.authCode)
                    }
                }

                androidx.compose.runtime.LaunchedEffect(state.loginSuccess) {
                    if (state.loginSuccess) {
                        when (val dest = viewModel.getPostLoginDestination()) {
                            is SignInViewModel.PostLoginDestination.WhatsNew ->
                                navController.navigate(
                                    AppNavRoutes.WhatsNew(
                                        courseId = dest.courseId,
                                        infoType = dest.infoType,
                                    )
                                ) { popUpTo(0) { inclusive = true } }
                            is SignInViewModel.PostLoginDestination.Main ->
                                navController.navigate(
                                    AppNavRoutes.Main(
                                        courseId = dest.courseId,
                                        infoType = dest.infoType,
                                    )
                                ) { popUpTo(0) { inclusive = true } }
                        }
                    }
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    viewModel.webContentEvent.collect { (title, url) ->
                        navController.navigate(AppNavRoutes.WebContent(title = title, url = url))
                    }
                }

                if (appUpgradeEvent != null) {
                    val _platformActions: org.openedx.core.system.PlatformActions = org.koin.compose.koinInject()
                    AppUpgradeRequiredScreen(onUpdateClick = { _platformActions.openAppInMarket() })
                } else {
                    LoginScreen(
                        windowSize = windowSize, state = state, uiMessage = uiMessage,
                        onEvent = { event ->
                            when (event) {
                                is AuthEvent.SignIn -> viewModel.login(event.login, event.password)
                                is AuthEvent.SocialSignIn -> viewModel.socialAuth(Unit, event.authType)
                                is AuthEvent.OpenLink -> viewModel.openLink(event.links, event.link)
                                AuthEvent.SignInBrowser -> viewModel.signInBrowser(Unit)
                                AuthEvent.ForgotPasswordClick -> navController.navigate(AppNavRoutes.RestorePassword)
                                AuthEvent.RegisterClick -> navController.navigate(AppNavRoutes.SignUp(route.courseId, route.infoType))
                                AuthEvent.BackClick -> navController.navigateUp()
                            }
                        },
                    )
                }
            }

            composable<AppNavRoutes.RestorePassword> {
                val viewModel: RestorePasswordViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState(null)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                val appUpgradeEvent by viewModel.appUpgradeEventUIState.collectAsState()
                if (appUpgradeEvent != null) {
                    val _platformActions: org.openedx.core.system.PlatformActions = org.koin.compose.koinInject()
                    AppUpgradeRequiredScreen(onUpdateClick = { _platformActions.openAppInMarket() })
                } else {
                    RestorePasswordScreen(
                        windowSize = windowSize,
                        uiState = uiState ?: RestorePasswordUIState.Initial,
                        uiMessage = uiMessage,
                        onBackClick = { navController.navigateUp() },
                        onRestoreButtonClick = { viewModel.passwordReset(it) },
                    )
                }
            }

            composable<AppNavRoutes.SignUp> { entry ->
                val route = entry.toRoute<AppNavRoutes.SignUp>()
                val viewModel: SignUpViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "", route.infoType ?: "")
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

                androidx.compose.runtime.LaunchedEffect(uiState.successLogin) {
                    if (uiState.successLogin) {
                        navController.navigate(
                            AppNavRoutes.Main(
                                courseId = viewModel.courseId,
                                infoType = viewModel.infoType,
                            )
                        ) { popUpTo(0) { inclusive = true } }
                    }
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    viewModel.webContentEvent.collect { (title, url) ->
                        navController.navigate(AppNavRoutes.WebContent(title = title, url = url))
                    }
                }

                if (uiState.appUpgradeEvent != null) {
                    val _platformActions: org.openedx.core.system.PlatformActions = org.koin.compose.koinInject()
                    AppUpgradeRequiredScreen(onUpdateClick = { _platformActions.openAppInMarket() })
                } else {
                    SignUpView(
                        windowSize = windowSize, uiState = uiState, uiMessage = uiMessage,
                        onBackClick = { navController.navigateUp() },
                        onFieldUpdated = { key, value -> viewModel.updateField(key, value) },
                        onRegisterClick = { authType ->
                            when (authType) {
                                org.openedx.auth.data.model.AuthType.PASSWORD -> viewModel.register()
                                org.openedx.auth.data.model.AuthType.GOOGLE,
                                org.openedx.auth.data.model.AuthType.FACEBOOK,
                                org.openedx.auth.data.model.AuthType.MICROSOFT ->
                                    viewModel.socialAuth(Unit, authType)
                                org.openedx.auth.data.model.AuthType.BROWSER -> Unit
                            }
                        },
                        onHyperLinkClick = { links, link -> viewModel.openLink(links, link) },
                    )
                }
            }

            composable<AppNavRoutes.NativeDiscovery> { entry ->
                val route = entry.toRoute<AppNavRoutes.NativeDiscovery>()
                org.openedx.discovery.presentation.NativeDiscoveryView(
                    onCourseClick = { courseId, _ ->
                        navController.navigate(
                            AppNavRoutes.CourseDetails(courseId = courseId)
                        )
                    },
                    onSearchClick = { navController.navigate(AppNavRoutes.CourseSearch(querySearch = route.querySearch)) },
                    onSignInClick = { navController.navigate(AppNavRoutes.SignIn()) },
                    onRegisterClick = { navController.navigate(AppNavRoutes.SignUp()) },
                    onSettingsClick = { navController.navigate(AppNavRoutes.Settings) },
                )
            }

            composable<AppNavRoutes.WebViewDiscovery> { entry ->
                val route = entry.toRoute<AppNavRoutes.WebViewDiscovery>()
                val viewModel: org.openedx.discovery.presentation.WebViewDiscoveryViewModel = koinViewModel {
                    parametersOf(route.querySearch)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                var hasInternet by remember { mutableStateOf(viewModel.hasInternetConnection) }
                var showExternalLinkDialog by androidx.compose.runtime.saveable.rememberSaveable {
                    androidx.compose.runtime.mutableStateOf<String?>(null)
                }
                val coreAnalytics: org.openedx.core.presentation.CoreAnalytics =
                    org.koin.compose.koinInject()
                val platformActions: org.openedx.core.system.PlatformActions =
                    org.koin.compose.koinInject()
                val config: org.openedx.core.config.Config = org.koin.compose.koinInject()
                val platformName = config.getPlatformName()
                val discoveryScreen =
                    org.openedx.discovery.presentation.DiscoveryAnalyticsScreen.DISCOVERY.screenName

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    viewModel.navigationEvent.collect { event ->
                        when (event) {
                            is org.openedx.discovery.presentation.WebViewDiscoveryNavEvent.CourseInfo ->
                                navController.navigate(
                                    AppNavRoutes.CourseInfo(
                                        courseId = event.pathId,
                                        infoType = event.infoType,
                                    )
                                )
                            org.openedx.discovery.presentation.WebViewDiscoveryNavEvent.SignIn ->
                                navController.navigate(AppNavRoutes.SignIn())
                            org.openedx.discovery.presentation.WebViewDiscoveryNavEvent.SignUp ->
                                navController.navigate(AppNavRoutes.SignUp())
                            org.openedx.discovery.presentation.WebViewDiscoveryNavEvent.Settings ->
                                navController.navigate(AppNavRoutes.Settings)
                        }
                    }
                }
                org.openedx.discovery.presentation.WebViewDiscoveryScreen(
                    windowSize = windowSize,
                    uiState = uiState,
                    isPreLogin = viewModel.isPreLogin,
                    contentUrl = viewModel.discoveryUrl,
                    uriScheme = viewModel.uriScheme,
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    userAgent = viewModel.appUserAgent,
                    hasInternetConnection = hasInternet,
                    onWebViewUIAction = { action ->
                        when (action) {
                            org.openedx.core.presentation.global.webview.WebViewUIAction.WEB_PAGE_LOADED ->
                                viewModel.onWebPageLoaded()
                            org.openedx.core.presentation.global.webview.WebViewUIAction.WEB_PAGE_ERROR ->
                                viewModel.onWebPageLoadError()
                            org.openedx.core.presentation.global.webview.WebViewUIAction.RELOAD_WEB_PAGE -> {
                                hasInternet = viewModel.hasInternetConnection
                                viewModel.onWebPageLoading()
                            }
                        }
                    },
                    onWebPageUpdated = { url -> viewModel.updateDiscoveryUrl(url) },
                    onUriClick = { param, authority ->
                        when (authority) {
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.COURSE_INFO -> {
                                viewModel.courseInfoClickedEvent(param)
                                viewModel.infoCardClicked(pathId = param, infoType = authority.name)
                            }
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.PROGRAM_INFO -> {
                                viewModel.programInfoClickedEvent(param)
                                viewModel.infoCardClicked(pathId = param, infoType = authority.name)
                            }
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.EXTERNAL -> {
                                coreAnalytics.logExternalLinkAlert(param, discoveryScreen)
                                showExternalLinkDialog = param
                            }
                            else -> {}
                        }
                    },
                    onRegisterClick = { viewModel.navigateToSignUp() },
                    onSignInClick = { viewModel.navigateToSignIn() },
                    onSettingsClick = { viewModel.navigateToSettings() },
                    onBackClick = { navController.navigateUp() },
                )

                showExternalLinkDialog?.let { url ->
                    org.openedx.core.presentation.dialog.alert.ActionDialog(
                        title = org.jetbrains.compose.resources.stringResource(
                            org.openedx.core.Res.string.core_leaving_the_app
                        ),
                        message = org.jetbrains.compose.resources.stringResource(
                            org.openedx.core.Res.string.core_leaving_the_app_message,
                            platformName,
                        ),
                        onCancelClick = {
                            coreAnalytics.logExternalLinkAlertAction(url, discoveryScreen, cancelled = true)
                            showExternalLinkDialog = null
                        },
                        onContinueClick = {
                            coreAnalytics.logExternalLinkAlertAction(url, discoveryScreen, cancelled = false)
                            platformActions.openLink(url)
                            showExternalLinkDialog = null
                        },
                    )
                }
            }

            composable<AppNavRoutes.Logistration> { entry ->
                val route = entry.toRoute<AppNavRoutes.Logistration>()
                val viewModel: LogistrationViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "")
                }
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                org.openedx.auth.presentation.logistration.LogistrationScreen(
                    onSearchClick = { query ->
                        viewModel.logDiscoverySearch(query)
                        if (viewModel.isDiscoveryTypeWebView) {
                            navController.navigate(AppNavRoutes.WebViewDiscovery(query))
                        } else {
                            navController.navigate(AppNavRoutes.NativeDiscovery(query))
                        }
                    },
                    onRegisterClick = {
                        viewModel.logRegisterClicked()
                        if (viewModel.isBrowserRegistrationEnabled) {
                            uriHandler.openUri(viewModel.apiHostUrl + org.openedx.core.ApiConstants.URL_REGISTER_BROWSER)
                        } else {
                            navController.navigate(AppNavRoutes.SignUp(route.courseId))
                        }
                    },
                    onSignInClick = {
                        viewModel.logSignInClicked()
                        if (viewModel.isBrowserLoginEnabled) {
                            viewModel.signInBrowser(Unit)
                        } else {
                            navController.navigate(AppNavRoutes.SignIn(route.courseId))
                        }
                    },
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
                val platformActions: org.openedx.core.system.PlatformActions =
                    org.koin.compose.koinInject()
                AppUpgradeRequiredScreen(
                    showAccountSettingsButton = true,
                    onAccountSettingsClick = {
                        navController.popBackStack()
                        navController.navigate(AppNavRoutes.Settings)
                    },
                    onUpdateClick = { platformActions.openAppInMarket() },
                )
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
                val refreshing by viewModel.isUpdating.collectAsState()
                org.openedx.profile.presentation.manageaccount.compose.ManageAccountView(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    refreshing = refreshing, onAction = { action ->
                        when (action) {
                            org.openedx.profile.presentation.manageaccount.compose.ManageAccountViewAction.BackClick ->
                                navController.navigateUp()
                            org.openedx.profile.presentation.manageaccount.compose.ManageAccountViewAction.EditAccountClick -> {
                                viewModel.profileEditClicked()
                                navController.navigate(AppNavRoutes.EditProfile(accountJson = ""))
                            }
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
                val config: org.openedx.core.config.Config = org.koin.compose.koinInject()
                org.openedx.core.ui.WebContentScreen(
                    windowSize = rememberWindowSize(),
                    apiHostUrl = config.getApiHostURL(),
                    title = route.title, contentUrl = route.url,
                    onBackClick = { navController.navigateUp() },
                )
            }

            composable<AppNavRoutes.CalendarSettings> {
                val vm: org.openedx.profile.presentation.calendar.CalendarViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState()
                val calendarManager: org.openedx.core.system.CalendarManager = org.koin.compose.koinInject()
                val config: org.openedx.core.config.Config = org.koin.compose.koinInject()
                var showAccessDialog by remember { mutableStateOf(false) }
                var showDisableDialog by remember { mutableStateOf(false) }
                var showNewCalendarDialog by remember { mutableStateOf<org.openedx.profile.presentation.calendar.NewCalendarDialogType?>(null) }

                org.openedx.profile.presentation.calendar.CalendarSettingsView(
                    windowSize = windowSize, uiState = uiState,
                    onCalendarSyncSwitchClick = { isEnabled ->
                        if (isEnabled) {
                            if (!calendarManager.hasPermissions()) {
                                showAccessDialog = true
                            } else if (uiState.calendarData == null) {
                                showNewCalendarDialog =
                                    org.openedx.profile.presentation.calendar.NewCalendarDialogType.CREATE_NEW
                            } else {
                                vm.setCalendarSyncEnabled(true)
                            }
                        } else {
                            showDisableDialog = true
                        }
                    },
                    onRelativeDateSwitchClick = { vm.setRelativeDateEnabled(it) },
                    onChangeSyncOptionClick = {
                        showNewCalendarDialog =
                            org.openedx.profile.presentation.calendar.NewCalendarDialogType.UPDATE
                    },
                    onCourseToSyncClick = { navController.navigate(AppNavRoutes.CoursesToSync) },
                    onBackClick = { navController.navigateUp() },
                )

                if (showAccessDialog) {
                    org.openedx.profile.presentation.calendar.CalendarAccessDialog(
                        onCancelClick = { showAccessDialog = false },
                        onGrantCalendarAccessClick = {
                            showAccessDialog = false
                            vm.setUpCalendarSync(Any())
                        },
                    )
                }

                if (showDisableDialog) {
                    val disableVm: org.openedx.profile.presentation.calendar.DisableCalendarSyncDialogViewModel =
                        koinViewModel()
                    val deletionState by disableVm.deletionState.collectAsState()
                    androidx.compose.runtime.LaunchedEffect(deletionState) {
                        if (deletionState == org.openedx.profile.presentation.calendar.DeletionState.DELETED) {
                            showDisableDialog = false
                            vm.setCalendarSyncEnabled(false)
                        }
                    }
                    org.openedx.profile.presentation.calendar.DisableCalendarSyncDialog(
                        calendarData = uiState.calendarData,
                        isDeleting = deletionState ==
                            org.openedx.profile.presentation.calendar.DeletionState.DELETING,
                        onCancelClick = { showDisableDialog = false },
                        onDisableSyncingClick = { disableVm.disableSyncingClick() },
                    )
                }

                showNewCalendarDialog?.let { dialogType ->
                    val createVm: org.openedx.profile.presentation.calendar.NewCalendarDialogViewModel =
                        koinViewModel()
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        createVm.isSuccess.collect { success ->
                            if (success) {
                                showNewCalendarDialog = null
                                vm.setCalendarSyncEnabled(true)
                            }
                        }
                    }
                    val googleCalendars by createVm.googleCalendars.collectAsState()
                    val showLocalSection by createVm.showLocalCalendarSection.collectAsState()
                    org.openedx.profile.presentation.calendar.NewCalendarDialog(
                        dialogType = dialogType,
                        platformName = config.getPlatformName(),
                        googleCalendars = googleCalendars,
                        showLocalCalendarSection = showLocalSection,
                        onCancelClick = { showNewCalendarDialog = null },
                        onBeginSyncingClick = { title, color ->
                            createVm.createCalendar(title, color)
                        },
                        onGoogleCalendarClick = { calendarId ->
                            createVm.syncWithGoogleCalendar(calendarId)
                        },
                    )
                }
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
                val title = org.jetbrains.compose.resources.stringResource(
                    if (viewModel.getQualityType() == org.openedx.core.presentation.settings.video.VideoQualityType.Streaming) {
                        org.openedx.core.Res.string.core_video_streaming_quality
                    } else {
                        org.openedx.core.Res.string.core_video_download_quality
                    }
                )
                org.openedx.core.presentation.settings.video.VideoQualityScreen(
                    windowSize = windowSize, title = title,
                    selectedVideoQuality = quality,
                    onQualityChanged = { viewModel.setVideoQuality(it) },
                    onBackClick = { navController.navigateUp() },
                )
            }
            composable<AppNavRoutes.EditProfile> { entry ->
                val route = entry.toRoute<AppNavRoutes.EditProfile>()
                val profileInteractor: org.openedx.profile.domain.interactor.ProfileInteractor = org.koin.compose.koinInject()
                val cachedAccount = profileInteractor.getCachedAccount() ?: return@composable
                val vm: org.openedx.profile.presentation.edit.EditProfileViewModel = koinViewModel {
                    parametersOf(cachedAccount)
                }
                // Store selected image for save
                val pendingImageBodyState = androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf<org.openedx.profile.data.repository.ImageBody?>(null)
                }
                val windowSize = rememberWindowSize()
                val uiState by vm.uiState.collectAsState()
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val selectedImage by vm.selectedImageUri.collectAsState(null)
                val isDeleted by vm.deleteImage.collectAsState(false)
                val leaveDialog by vm.showLeaveDialog.collectAsState(false)
                org.openedx.profile.presentation.edit.EditProfileScreen(
                    windowSize = windowSize,
                    uiState = uiState ?: org.openedx.profile.presentation.edit.EditProfileUIState(
                        account = cachedAccount, isLimited = cachedAccount.isLimited()
                    ),
                    uiMessage = uiMessage,
                    selectedImageUri = selectedImage, isImageDeleted = isDeleted,
                    leaveDialog = leaveDialog,
                    onKeepEdit = { vm.setShowLeaveDialog(false) },
                    onDataChanged = { vm.profileDataChanged = it },
                    onLimitedProfileChange = { vm.isLimitedProfile = it },
                    onBackClick = { hasChanges ->
                        if (hasChanges && vm.profileDataChanged) {
                            vm.setShowLeaveDialog(true)
                        } else {
                            navController.navigateUp()
                        }
                    },
                    onSaveClick = { fields ->
                        vm.profileEditDoneClickedEvent()
                        val imageBody = pendingImageBodyState.value
                        if (imageBody != null) {
                            vm.updateAccountAndImage(fields, imageBody)
                            pendingImageBodyState.value = null
                        } else {
                            vm.updateAccount(fields)
                        }
                    },
                    onSelectImageClick = {
                        org.openedx.shared.ui.showImagePicker { bytes, ext, previewUri ->
                            pendingImageBodyState.value = org.openedx.profile.data.repository.ImageBody(bytes, ext)
                            vm.setImageUri(previewUri)
                        }
                    },
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
                val bgColor = androidx.compose.material3.MaterialTheme.colorScheme.background.value
                val textColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.value
                val htmlBody = viewModel.getCourseAboutBody(bgColor, textColor)
                var showAuthDialog by remember { mutableStateOf(false) }
                org.openedx.discovery.presentation.detail.CourseDetailsScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    apiHostUrl = viewModel.apiHostUrl, htmlBody = htmlBody,
                    hasInternetConnection = viewModel.hasInternetConnection,
                    isUserLoggedIn = viewModel.isUserLoggedIn,
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    onReloadClick = { viewModel.getCourseDetail() },
                    onBackClick = { navController.navigateUp() },
                    onButtonClick = {
                        val state = viewModel.uiState.value
                        if (state is org.openedx.discovery.presentation.detail.CourseDetailsUIState.CourseData) {
                            when {
                                !viewModel.isUserLoggedIn -> showAuthDialog = true
                                state.course.isEnrolled -> navController.navigate(
                                    AppNavRoutes.CourseContainer(
                                        courseId = state.course.courseId,
                                        courseTitle = state.course.name,
                                    )
                                )
                                else -> viewModel.enrollInACourse(state.course.courseId, state.course.name)
                            }
                        }
                    },
                    onRegisterClick = { navController.navigate(AppNavRoutes.SignUp(courseId = route.courseId)) },
                    onSignInClick = { navController.navigate(AppNavRoutes.SignIn(courseId = route.courseId)) },
                )
                if (showAuthDialog) {
                    org.openedx.core.presentation.dialog.alert.AuthorizationDialog(
                        onCancelClick = { showAuthDialog = false },
                        onSignInClick = {
                            showAuthDialog = false
                            navController.navigate(AppNavRoutes.SignIn(courseId = route.courseId))
                        },
                        onRegisterClick = {
                            showAuthDialog = false
                            navController.navigate(AppNavRoutes.SignUp(courseId = route.courseId))
                        },
                        showRegisterButton = viewModel.isRegistrationEnabled,
                    )
                }
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
                var hasInternet by androidx.compose.runtime.saveable.rememberSaveable {
                    androidx.compose.runtime.mutableStateOf(vm.hasInternetConnection)
                }
                var showExternalLinkDialog by androidx.compose.runtime.saveable.rememberSaveable {
                    androidx.compose.runtime.mutableStateOf<String?>(null)
                }
                val coreAnalytics: org.openedx.core.presentation.CoreAnalytics =
                    org.koin.compose.koinInject()
                val platformActions: org.openedx.core.system.PlatformActions =
                    org.koin.compose.koinInject()
                val config: org.openedx.core.config.Config = org.koin.compose.koinInject()
                val platformName = config.getPlatformName()
                val screenName =
                    org.openedx.discovery.presentation.DiscoveryAnalyticsScreen.COURSE_INFO.screenName

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    vm.navigationEvent.collect { event ->
                        when (event) {
                            is org.openedx.discovery.presentation.info.CourseInfoNavEvent.CourseInfo ->
                                navController.navigate(
                                    AppNavRoutes.CourseInfo(event.pathId, event.infoType)
                                )
                            is org.openedx.discovery.presentation.info.CourseInfoNavEvent.CourseOutline ->
                                navController.navigate(
                                    AppNavRoutes.CourseContainer(
                                        courseId = event.courseId,
                                        courseTitle = event.courseTitle,
                                    )
                                )
                            is org.openedx.discovery.presentation.info.CourseInfoNavEvent.SignIn ->
                                navController.navigate(
                                    AppNavRoutes.SignIn(
                                        courseId = event.courseId,
                                        infoType = event.infoType,
                                    )
                                )
                            is org.openedx.discovery.presentation.info.CourseInfoNavEvent.SignUp ->
                                navController.navigate(
                                    AppNavRoutes.SignUp(
                                        courseId = event.courseId,
                                        infoType = event.infoType,
                                    )
                                )
                        }
                    }
                }

                androidx.compose.runtime.LaunchedEffect(
                    (uiState as? org.openedx.discovery.presentation.info.CourseInfoUIState.CourseInfo)
                        ?.enrolledCourseId
                ) {
                    val enrolled = (uiState as? org.openedx.discovery.presentation.info.CourseInfoUIState.CourseInfo)
                        ?.enrolledCourseId
                    if (!enrolled.isNullOrEmpty()) {
                        vm.onSuccessfulCourseEnrollment(enrolled)
                    }
                }

                org.openedx.discovery.presentation.info.CourseInfoScreen(
                    windowSize = windowSize, uiState = uiState,
                    webViewUIState = vm.webViewState.value, uiMessage = uiMessage,
                    uriScheme = vm.uriScheme, isRegistrationEnabled = vm.isRegistrationEnabled,
                    userAgent = vm.appUserAgent, hasInternetConnection = hasInternet,
                    onWebViewUIAction = { action ->
                        when (action) {
                            org.openedx.core.presentation.global.webview.WebViewUIAction.WEB_PAGE_LOADED ->
                                vm.onWebPageLoaded()
                            org.openedx.core.presentation.global.webview.WebViewUIAction.WEB_PAGE_ERROR ->
                                vm.onWebPageError()
                            org.openedx.core.presentation.global.webview.WebViewUIAction.RELOAD_WEB_PAGE -> {
                                hasInternet = vm.hasInternetConnection
                                vm.onWebPageLoading()
                            }
                        }
                    },
                    onRegisterClick = {
                        vm.navigateToSignUp(vm.pathId, vm.infoType)
                    },
                    onSignInClick = {
                        vm.navigateToSignIn(vm.pathId, vm.infoType)
                    },
                    onUriClick = { param, type ->
                        when (type) {
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.PROGRAM_INFO -> {
                                vm.programInfoClickedEvent(param)
                                vm.infoCardClicked(pathId = param, infoType = type.name)
                            }
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.COURSE_INFO -> {
                                vm.courseInfoClickedEvent(param)
                                vm.infoCardClicked(pathId = param, infoType = type.name)
                            }
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.EXTERNAL -> {
                                coreAnalytics.logExternalLinkAlert(param, screenName)
                                showExternalLinkDialog = param
                            }
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.ENROLL -> {
                                vm.courseEnrollClickedEvent(param)
                                val preLogin = (uiState as? org.openedx.discovery.presentation.info.CourseInfoUIState.CourseInfo)
                                    ?.isPreLogin == true
                                if (preLogin) {
                                    vm.navigateToSignUp(vm.pathId, vm.infoType)
                                } else {
                                    vm.enrollInACourse(param)
                                }
                            }
                            else -> Unit
                        }
                    },
                    onBackClick = { navController.navigateUp() },
                )

                showExternalLinkDialog?.let { url ->
                    org.openedx.core.presentation.dialog.alert.ActionDialog(
                        title = org.jetbrains.compose.resources.stringResource(
                            org.openedx.core.Res.string.core_leaving_the_app
                        ),
                        message = org.jetbrains.compose.resources.stringResource(
                            org.openedx.core.Res.string.core_leaving_the_app_message,
                            platformName,
                        ),
                        onCancelClick = {
                            coreAnalytics.logExternalLinkAlertAction(url, screenName, cancelled = true)
                            showExternalLinkDialog = null
                        },
                        onContinueClick = {
                            coreAnalytics.logExternalLinkAlertAction(url, screenName, cancelled = false)
                            platformActions.openLink(url)
                            showExternalLinkDialog = null
                        },
                    )
                }
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
                var hasInternet by androidx.compose.runtime.saveable.rememberSaveable {
                    androidx.compose.runtime.mutableStateOf(vm.hasInternetConnection)
                }
                var showExternalLinkDialog by androidx.compose.runtime.saveable.rememberSaveable {
                    androidx.compose.runtime.mutableStateOf<String?>(null)
                }
                val coreAnalytics: org.openedx.core.presentation.CoreAnalytics =
                    org.koin.compose.koinInject()
                val platformActions: org.openedx.core.system.PlatformActions =
                    org.koin.compose.koinInject()
                val config: org.openedx.core.config.Config = org.koin.compose.koinInject()
                val platformName = config.getPlatformName()
                val screenName =
                    org.openedx.discovery.presentation.DiscoveryAnalyticsScreen.PROGRAM.screenName

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    vm.navigationEvent.collect { event ->
                        when (event) {
                            is org.openedx.discovery.presentation.program.ProgramNavEvent.EnrolledProgramInfo ->
                                navController.navigate(
                                    AppNavRoutes.Program(pathId = event.pathId)
                                )
                            is org.openedx.discovery.presentation.program.ProgramNavEvent.CourseInfo ->
                                navController.navigate(
                                    AppNavRoutes.CourseInfo(event.courseId, event.infoType)
                                )
                            is org.openedx.discovery.presentation.program.ProgramNavEvent.CourseOutline ->
                                navController.navigate(
                                    AppNavRoutes.CourseContainer(
                                        courseId = event.courseId,
                                        courseTitle = event.courseTitle,
                                    )
                                )
                            org.openedx.discovery.presentation.program.ProgramNavEvent.Settings ->
                                navController.navigate(AppNavRoutes.Settings)
                        }
                    }
                }

                org.openedx.discovery.presentation.program.ProgramInfoScreen(
                    windowSize = windowSize, uiState = uiState,
                    contentUrl = vm.programConfig.programUrl,
                    cookieManager = vm.cookieManager,
                    uriScheme = vm.uriScheme, userAgent = vm.appUserAgent,
                    canShowBackBtn = !route.isNestedFragment,
                    isNestedFragment = route.isNestedFragment,
                    hasInternetConnection = hasInternet,
                    onWebViewUIAction = { action ->
                        when (action) {
                            org.openedx.core.presentation.global.webview.WebViewUIAction.WEB_PAGE_LOADED ->
                                vm.showLoading(false)
                            org.openedx.core.presentation.global.webview.WebViewUIAction.WEB_PAGE_ERROR ->
                                vm.onPageLoadError()
                            org.openedx.core.presentation.global.webview.WebViewUIAction.RELOAD_WEB_PAGE -> {
                                hasInternet = vm.hasInternetConnection
                                vm.showLoading(true)
                            }
                        }
                    },
                    onSettingsClick = { vm.navigateToSettings() },
                    onUriClick = { param, type ->
                        when (type) {
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.ENROLLED_COURSE_INFO ->
                                vm.onEnrolledCourseClick(courseId = param)
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.ENROLLED_PROGRAM_INFO ->
                                vm.onProgramCardClick(pathId = param)
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.PROGRAM_INFO,
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.COURSE_INFO ->
                                vm.onViewCourseClick(courseId = param, infoType = type.name)
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.ENROLL ->
                                vm.enrollInACourse(param)
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.COURSE ->
                                vm.navigateToDiscovery()
                            org.openedx.discovery.presentation.catalog.WebViewLink.Authority.EXTERNAL -> {
                                coreAnalytics.logExternalLinkAlert(param, screenName)
                                showExternalLinkDialog = param
                            }
                        }
                    },
                    onBackClick = { navController.navigateUp() },
                )

                showExternalLinkDialog?.let { url ->
                    org.openedx.core.presentation.dialog.alert.ActionDialog(
                        title = org.jetbrains.compose.resources.stringResource(
                            org.openedx.core.Res.string.core_leaving_the_app
                        ),
                        message = org.jetbrains.compose.resources.stringResource(
                            org.openedx.core.Res.string.core_leaving_the_app_message,
                            platformName,
                        ),
                        onCancelClick = {
                            coreAnalytics.logExternalLinkAlertAction(url, screenName, cancelled = true)
                            showExternalLinkDialog = null
                        },
                        onContinueClick = {
                            coreAnalytics.logExternalLinkAlertAction(url, screenName, cancelled = false)
                            platformActions.openLink(url)
                            showExternalLinkDialog = null
                        },
                    )
                }
            }

            // =================== COURSE ===================
            composable<AppNavRoutes.CourseContainer> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseContainer>()
                org.openedx.course.presentation.container.CourseContainerScreen(
                    courseId = route.courseId,
                    courseTitle = route.courseTitle,
                    openTab = route.openTab,
                    resumeBlockId = route.resumeBlockId,
                    onBackClick = { navController.navigateUp() },
                    onNoAccess = { title ->
                        navController.navigate(AppNavRoutes.NoAccessCourseContainer(title = title)) {
                            popUpTo<AppNavRoutes.CourseContainer> { inclusive = true }
                        }
                    },
                    onNavigateToCourseContainer = { courseId, unitId, componentId, mode ->
                        navController.navigate(
                            AppNavRoutes.CourseUnitContainer(
                                courseId = courseId,
                                unitId = unitId,
                                componentId = componentId,
                                mode = mode.name,
                            )
                        )
                    },
                    onNavigateToCourseSubsections = { courseId, subSectionId, unitId, componentId, mode ->
                        navController.navigate(
                            AppNavRoutes.CourseSection(
                                courseId = courseId,
                                subSectionId = subSectionId,
                                unitId = unitId,
                                componentId = componentId,
                                mode = mode.name,
                            )
                        )
                    },
                    onNavigateToDownloadQueue = { descendants ->
                        navController.navigate(AppNavRoutes.DownloadQueue(descendants = descendants))
                    },
                    onNavigateToHandoutsWebView = { type ->
                        navController.navigate(AppNavRoutes.HandoutsWebView(courseId = route.courseId, type = type))
                    },
                    onNavigateToCalendarSettings = {
                        navController.navigate(AppNavRoutes.CalendarSettings)
                    },
                    discussionsContent = { courseId, courseTitle ->
                        val vm: org.openedx.discussion.presentation.topics.DiscussionTopicsViewModel = koinViewModel {
                            parametersOf(courseId, courseTitle)
                        }
                        org.openedx.discussion.presentation.topics.DiscussionTopicsScreen(
                            windowSize = rememberWindowSize(),
                            discussionTopicsViewModel = vm,
                            onItemClick = { action, cId, topicId, title ->
                                navController.navigate(
                                    AppNavRoutes.DiscussionThreads(
                                        action = action, courseId = cId,
                                        topicId = topicId, title = title, viewType = "TOPIC",
                                    )
                                )
                            },
                            onSearchClick = { cId ->
                                navController.navigate(AppNavRoutes.DiscussionSearchThread(courseId = cId))
                            },
                        )
                    },
                )
            }

            composable<AppNavRoutes.CourseContentAll> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseContentAll>()
                val vm: org.openedx.course.presentation.outline.CourseContentAllViewModel = koinViewModel {
                    parametersOf(route.courseId, route.courseTitle)
                }
                val windowSize = rememberWindowSize()
                androidx.compose.material3.Scaffold(
                    topBar = {
                        org.openedx.core.ui.Toolbar(
                            label = route.courseTitle.ifEmpty { "Content" },
                            canShowBackBtn = true,
                            onBackClick = { navController.navigateUp() },
                        )
                    }
                ) { pad ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(pad)) {
                        org.openedx.course.presentation.outline.CourseContentAllScreen(
                            windowSize = windowSize, viewModel = vm,
                            onNavigateToHome = { navController.navigateUp() },
                            onNavigateToCourseContainer = { courseId, unitId, componentId, mode ->
                                navController.navigate(
                                    AppNavRoutes.CourseUnitContainer(
                                        courseId = courseId, unitId = unitId,
                                        componentId = componentId, mode = mode.name,
                                    )
                                )
                            },
                            onNavigateToCourseSubsections = { courseId, subSectionId, unitId, componentId, mode ->
                                navController.navigate(
                                    AppNavRoutes.CourseSection(
                                        courseId = courseId, subSectionId = subSectionId,
                                        unitId = unitId, componentId = componentId,
                                        mode = mode.name,
                                    )
                                )
                            },
                            onNavigateToDownloadQueue = { descendants ->
                                navController.navigate(AppNavRoutes.DownloadQueue(descendants = descendants))
                            },
                        )
                    }
                }
            }

            composable<AppNavRoutes.CourseProgress> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseProgress>()
                val vm: org.openedx.course.presentation.progress.CourseProgressViewModel = koinViewModel {
                    parametersOf(route.courseId)
                }
                val windowSize = rememberWindowSize()
                androidx.compose.material3.Scaffold(
                    topBar = {
                        org.openedx.core.ui.Toolbar(
                            label = "Progress",
                            canShowBackBtn = true,
                            onBackClick = { navController.navigateUp() },
                        )
                    }
                ) { pad ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(pad)) {
                        org.openedx.course.presentation.progress.CourseProgressScreen(
                            windowSize = windowSize, viewModel = vm,
                        )
                    }
                }
            }

            composable<AppNavRoutes.CourseDates> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseDates>()
                val vm: org.openedx.course.presentation.dates.CourseDatesViewModel = koinViewModel {
                    parametersOf(route.courseId, route.enrollmentMode)
                }
                val windowSize = rememberWindowSize()
                androidx.compose.material3.Scaffold(
                    topBar = {
                        org.openedx.core.ui.Toolbar(
                            label = "Important Dates",
                            canShowBackBtn = true,
                            onBackClick = { navController.navigateUp() },
                        )
                    }
                ) { pad ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(pad)) {
                        org.openedx.course.presentation.dates.CourseDatesScreen(
                            windowSize = windowSize, viewModel = vm,
                            updateCourseStructure = { },
                            onNavigateToCourseContainer = { courseId, unitId, componentId, mode ->
                                navController.navigate(
                                    AppNavRoutes.CourseUnitContainer(
                                        courseId = courseId, unitId = unitId,
                                        componentId = componentId, mode = mode.name,
                                    )
                                )
                            },
                            onNavigateToCourseSubsections = { courseId, subSectionId, unitId, componentId, mode ->
                                navController.navigate(
                                    AppNavRoutes.CourseSection(
                                        courseId = courseId, subSectionId = subSectionId,
                                        unitId = unitId, componentId = componentId, mode = mode.name,
                                    )
                                )
                            },
                            onNavigateToCalendarSettings = {
                                navController.navigate(AppNavRoutes.CalendarSettings)
                            },
                        )
                    }
                }
            }

            composable<AppNavRoutes.DiscussionTopics> { entry ->
                val route = entry.toRoute<AppNavRoutes.DiscussionTopics>()
                val vm: org.openedx.discussion.presentation.topics.DiscussionTopicsViewModel = koinViewModel {
                    parametersOf(route.courseId, route.courseTitle)
                }
                val windowSize = rememberWindowSize()
                androidx.compose.material3.Scaffold(
                    topBar = {
                        org.openedx.core.ui.Toolbar(
                            label = route.courseTitle.ifEmpty { "Discussion Topics" },
                            canShowBackBtn = true,
                            onBackClick = { navController.navigateUp() },
                        )
                    }
                ) { pad ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(pad)) {
                        org.openedx.discussion.presentation.topics.DiscussionTopicsScreen(
                            windowSize = windowSize, discussionTopicsViewModel = vm,
                            onItemClick = { action, courseId, topicId, title ->
                                navController.navigate(
                                    AppNavRoutes.DiscussionThreads(
                                        action = action, courseId = courseId,
                                        topicId = topicId, title = title, viewType = "TOPIC",
                                    )
                                )
                            },
                            onSearchClick = { courseId ->
                                navController.navigate(AppNavRoutes.DiscussionSearchThread(courseId = courseId))
                            },
                        )
                    }
                }
            }

            composable<AppNavRoutes.CourseSection> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseSection>()
                val viewModel: org.openedx.course.presentation.section.CourseSectionViewModel = koinViewModel {
                    parametersOf(route.courseId, route.subSectionId)
                }
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState(org.openedx.course.presentation.section.CourseSectionUIState.Loading)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                val mode = androidx.compose.runtime.remember(route.mode) {
                    runCatching { org.openedx.course.presentation.unit.container.CourseViewMode.valueOf(route.mode) }
                        .getOrDefault(org.openedx.course.presentation.unit.container.CourseViewMode.FULL)
                }
                androidx.compose.runtime.LaunchedEffect(route.subSectionId, mode) {
                    viewModel.mode = mode
                    viewModel.getBlocks(route.subSectionId, mode)
                }
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (route.unitId.isNotEmpty()) {
                        navController.navigate(
                            AppNavRoutes.CourseUnitContainer(
                                courseId = route.courseId,
                                unitId = route.unitId,
                                componentId = route.componentId,
                                mode = mode.name,
                            )
                        )
                    }
                }
                org.openedx.course.presentation.section.CourseSectionScreen(
                    windowSize = windowSize, uiState = uiState ?: return@composable, uiMessage = uiMessage,
                    onBackClick = { navController.navigateUp() },
                    onItemClick = { block ->
                        if (block.descendants.isNotEmpty()) {
                            viewModel.verticalClickedEvent(block.blockId)
                            navController.navigate(
                                AppNavRoutes.CourseUnitContainer(
                                    courseId = route.courseId,
                                    unitId = block.id,
                                    mode = mode.name,
                                )
                            )
                        }
                    },
                )
            }
            composable<AppNavRoutes.CourseUnitContainer> { entry ->
                val route = entry.toRoute<AppNavRoutes.CourseUnitContainer>()
                val vm: org.openedx.course.presentation.unit.container.CourseUnitContainerViewModel = koinViewModel {
                    parametersOf(route.courseId, route.unitId, org.openedx.course.presentation.unit.container.CourseViewMode.valueOf(route.mode))
                }
                val windowSize = rememberWindowSize()
                val config: org.openedx.core.config.Config = org.koin.compose.koinInject()

                org.openedx.course.presentation.unit.container.CourseUnitContainerScreen(
                    windowSize = windowSize,
                    viewModel = vm,
                    componentId = route.componentId,
                    onBackClick = { navController.navigateUp() },
                    onBackToOutline = {
                        // Pop all the way back to CourseContainer
                        navController.popBackStack<AppNavRoutes.CourseContainer>(inclusive = false)
                    },
                    onNavigateToUnit = { courseId, unitId, componentId, mode ->
                        // Replace current UnitContainer (same sequential)
                        navController.navigate(
                            AppNavRoutes.CourseUnitContainer(
                                courseId = courseId,
                                unitId = unitId,
                                componentId = componentId,
                                mode = mode,
                            )
                        ) {
                            popUpTo<AppNavRoutes.CourseUnitContainer> { inclusive = true }
                        }
                    },
                    onNavigateToNextSection = { courseId, subSectionId, unitId, mode ->
                        // Crossed sequential boundary: pop CourseSection + UnitContainer,
                        // push new CourseSection + UnitContainer
                        // Stack: ...→Section(old)→Unit(old) → ...→Section(new)→Unit(new)
                        navController.navigate(
                            AppNavRoutes.CourseSection(
                                courseId = courseId,
                                subSectionId = subSectionId,
                                mode = mode,
                            )
                        ) {
                            popUpTo<AppNavRoutes.CourseSection> { inclusive = true }
                        }
                        navController.navigate(
                            AppNavRoutes.CourseUnitContainer(
                                courseId = courseId,
                                unitId = unitId,
                                mode = mode,
                            )
                        )
                    },
                    renderBlock = { block ->
                        val noNetwork = !vm.hasNetworkConnection
                        val downloadModel = remember(block.blockId, noNetwork) { vm.getDownloadModelById(block.blockId) }
                        val isOfflineDownloaded = downloadModel != null
                        when {
                            noNetwork && block.isDownloadable && !isOfflineDownloaded -> {
                                org.openedx.course.presentation.unit.NotAvailableUnitScreen(
                                    windowSize = rememberWindowSize(),
                                    unitType = org.openedx.course.presentation.unit.NotAvailableUnitType.NOT_DOWNLOADED,
                                )
                            }
                            noNetwork && !block.isDownloadable -> {
                                org.openedx.course.presentation.unit.NotAvailableUnitScreen(
                                    windowSize = rememberWindowSize(),
                                    unitType = org.openedx.course.presentation.unit.NotAvailableUnitType.OFFLINE_UNSUPPORTED,
                                )
                            }
                            else -> UnitBlockContent(
                            block = block,
                            config = config,
                            courseId = route.courseId,
                            onNavigateToFullScreen = { videoUrl, isYoutube, videoTime, isPlaying ->
                                if (isYoutube) {
                                    navController.navigate(
                                        AppNavRoutes.YoutubeVideoFullScreen(
                                            videoUrl = videoUrl,
                                            videoTime = videoTime,
                                            blockId = block.blockId,
                                            courseId = route.courseId,
                                            isPlaying = isPlaying,
                                        )
                                    )
                                } else {
                                    navController.navigate(
                                        AppNavRoutes.VideoFullScreen(
                                            videoUrl = videoUrl,
                                            videoTime = videoTime,
                                            blockId = block.blockId,
                                            courseId = route.courseId,
                                            isPlaying = isPlaying,
                                        )
                                    )
                                }
                            },
                        )
                        }
                    },
                )
            }
            composable<AppNavRoutes.HandoutsWebView> { entry ->
                val route = entry.toRoute<AppNavRoutes.HandoutsWebView>()
                val viewModel: org.openedx.course.presentation.handouts.HandoutsViewModel = koinViewModel {
                    parametersOf(route.courseId, route.type)
                }
                androidx.compose.runtime.LaunchedEffect(route.type) {
                    val event = if (
                        org.openedx.course.presentation.handouts.HandoutsType.valueOf(route.type)
                        == org.openedx.course.presentation.handouts.HandoutsType.Handouts
                    ) {
                        org.openedx.course.presentation.CourseAnalyticsEvent.HANDOUTS
                    } else {
                        org.openedx.course.presentation.CourseAnalyticsEvent.ANNOUNCEMENTS
                    }
                    viewModel.logEvent(event)
                }
                org.openedx.course.presentation.handouts.HandoutsWebViewScreen(
                    windowSize = rememberWindowSize(),
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                )
            }
            composable<AppNavRoutes.VideoFullScreen> { entry ->
                val route = entry.toRoute<AppNavRoutes.VideoFullScreen>()
                val vm: org.openedx.course.presentation.unit.video.VideoViewModel = koinViewModel(
                    key = "fullscreen-${route.blockId}",
                ) { parametersOf(route.courseId) }
                androidx.compose.runtime.LaunchedEffect(route.blockId) {
                    vm.videoUrl = route.videoUrl
                    if (vm.currentVideoTime == 0L) vm.currentVideoTime = route.videoTime
                    if (vm.isPlaying == null) vm.isPlaying = route.isPlaying
                }
                androidx.compose.runtime.DisposableEffect(route.blockId) {
                    onDispose { vm.sendTime() }
                }
                org.openedx.shared.ui.EnterFullscreenImmersiveMode()
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    val fullscreenReviewManager: org.openedx.core.presentation.dialog.appreview.AppReviewManager =
                        org.koin.compose.koinInject()
                    org.openedx.shared.ui.PlatformVideoPlayer(
                        url = route.videoUrl,
                        modifier = Modifier.aspectRatio(16f / 9f),
                        isPlaying = route.isPlaying,
                        startPositionMs = route.videoTime,
                        maxVideoHeight = vm.getVideoQuality().height,
                        onProgressChanged = { positionMs ->
                            vm.currentVideoTime = positionMs
                        },
                        onEnded = {
                            vm.markBlockCompleted(route.blockId, "native")
                            if (!fullscreenReviewManager.isDialogShowed) {
                                fullscreenReviewManager.tryToOpenRateDialog()
                            }
                        },
                        onPlayPauseChanged = { playing ->
                            vm.isPlaying = playing
                            vm.logPlayPauseEvent(
                                route.videoUrl,
                                playing,
                                vm.currentVideoTime,
                                "native",
                            )
                        },
                        onSpeedChanged = { speed ->
                            vm.logVideoSpeedEvent(
                                route.videoUrl,
                                speed,
                                vm.currentVideoTime,
                                "native",
                            )
                        },
                    )
                    androidx.compose.material3.IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart),
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }
            }
            composable<AppNavRoutes.YoutubeVideoFullScreen> { entry ->
                val route = entry.toRoute<AppNavRoutes.YoutubeVideoFullScreen>()
                val videoId = extractYouTubeVideoId(route.videoUrl)
                val vm: org.openedx.course.presentation.unit.video.VideoViewModel = koinViewModel(
                    key = "yt-fullscreen-${route.blockId}",
                ) { parametersOf(route.courseId) }
                val appReviewManager: org.openedx.core.presentation.dialog.appreview.AppReviewManager =
                    org.koin.compose.koinInject()
                androidx.compose.runtime.LaunchedEffect(route.blockId) {
                    vm.videoUrl = route.videoUrl
                    if (vm.currentVideoTime == 0L) vm.currentVideoTime = route.videoTime
                    if (vm.isPlaying == null) vm.isPlaying = route.isPlaying
                }
                androidx.compose.runtime.DisposableEffect(route.blockId) {
                    onDispose { vm.sendTime() }
                }
                var isCompletionCalled by remember { mutableStateOf(false) }
                org.openedx.shared.ui.EnterFullscreenImmersiveMode()
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    org.openedx.shared.ui.PlatformYouTubePlayer(
                        videoId = videoId,
                        modifier = Modifier.aspectRatio(16f / 9f),
                        startSeconds = route.videoTime / 1000f,
                        onStateChange = { isPlaying -> vm.isPlaying = isPlaying },
                        onCurrentSecond = { second ->
                            vm.currentVideoTime = (second * 1000).toLong()
                            if (vm.duration > 0) {
                                val pct = second / (vm.duration / 1000f)
                                if (pct >= 0.8f && !isCompletionCalled) {
                                    isCompletionCalled = true
                                    vm.markBlockCompleted(route.blockId, "youtube")
                                }
                                if (pct >= 0.99f && !appReviewManager.isDialogShowed) {
                                    appReviewManager.tryToOpenRateDialog()
                                }
                            }
                        },
                        onVideoDuration = { duration ->
                            vm.duration = (duration * 1000).toLong()
                        },
                        onFullscreenClick = { navController.navigateUp() },
                    )
                    androidx.compose.material3.IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart),
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }
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
                    onDownloadClick = { model -> viewModel.removeDownloadModels(model.id, "") },
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
                val vm: org.openedx.discussion.presentation.threads.DiscussionThreadsViewModel = koinViewModel {
                    parametersOf(route.courseId, route.topicId, route.action)
                }
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
                    onSwipeRefresh = {
                        vm.updateThread(
                            org.openedx.discussion.presentation.threads.SortType.LAST_ACTIVITY_AT.queryParam
                        )
                    },
                    updatedOrder = { vm.getThreadByType(it) },
                    updatedFilter = { vm.filterThreads(it) }, onItemClick = { thread ->
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
                        onItemClick = { action, id, bool ->
                            if (!vm.thread.closed) {
                                when (action) {
                                    org.openedx.discussion.presentation.DiscussionActions.ACTION_UPVOTE_COMMENT -> vm.setCommentUpvoted(id, bool)
                                    org.openedx.discussion.presentation.DiscussionActions.ACTION_UPVOTE_THREAD -> vm.setThreadUpvoted(bool)
                                    org.openedx.discussion.presentation.DiscussionActions.ACTION_FOLLOW_THREAD -> vm.setThreadFollowed(bool)
                                    org.openedx.discussion.presentation.DiscussionActions.ACTION_REPORT_COMMENT -> vm.setCommentReported(id, bool)
                                    org.openedx.discussion.presentation.DiscussionActions.ACTION_REPORT_THREAD -> vm.setThreadReported(bool)
                                }
                            } else {
                                when (action) {
                                    org.openedx.discussion.presentation.DiscussionActions.ACTION_REPORT_COMMENT -> vm.setCommentReported(id, bool)
                                    org.openedx.discussion.presentation.DiscussionActions.ACTION_REPORT_THREAD -> vm.setThreadReported(bool)
                                }
                            }
                        },
                        onCommentClick = { comment ->
                            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; encodeDefaults = true }
                                .encodeToString(org.openedx.discussion.domain.model.DiscussionComment.serializer(), comment)
                            navController.navigate(AppNavRoutes.DiscussionResponses(commentJson = json, isClosed = vm.thread.closed))
                        },
                        onAddResponseClick = { vm.createComment(it) },
                        onBackClick = { navController.navigateUp() },
                        onUserPhotoClick = { username ->
                            navController.navigate(AppNavRoutes.AnothersProfile(username = username))
                        },
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
                        addCommentClick = { vm.createComment(it) },
                        onItemClick = { action, id, bool ->
                            when (action) {
                                org.openedx.discussion.presentation.DiscussionActions.ACTION_UPVOTE_COMMENT -> vm.setCommentUpvoted(id, bool)
                                org.openedx.discussion.presentation.DiscussionActions.ACTION_REPORT_COMMENT -> vm.setCommentReported(id, bool)
                            }
                        },
                        onBackClick = { navController.navigateUp() },
                        onUserPhotoClick = { username ->
                            navController.navigate(AppNavRoutes.AnothersProfile(username = username))
                        },
                    )
                } else PlaceholderDestination("Responses")
            }

            composable<AppNavRoutes.DiscussionAddThread> { entry ->
                val route = entry.toRoute<AppNavRoutes.DiscussionAddThread>()
                val vm: org.openedx.discussion.presentation.threads.DiscussionAddThreadViewModel = koinViewModel { parametersOf(route.courseId, route.topicId) }
                val windowSize = rememberWindowSize()
                val uiMessage by vm.uiMessage.collectAsState(initial = null)
                val isLoading by vm.isLoading.collectAsState(false)
                val newThread by vm.newThread.collectAsState(null)
                androidx.compose.runtime.LaunchedEffect(newThread) {
                    if (newThread != null) {
                        vm.sendThreadAdded()
                        navController.navigateUp()
                    }
                }
                org.openedx.discussion.presentation.threads.DiscussionAddThreadScreen(
                    windowSize = windowSize,
                    topicData = route.topicId to "",
                    topics = vm.getHandledTopics(),
                    uiMessage = uiMessage, isLoading = isLoading,
                    onPostDiscussionClick = { type, title, body, topicId, follow ->
                        vm.createThread(topicId, type, title, body, follow)
                    },
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
                    onItemClick = { thread ->
                        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; encodeDefaults = true }
                            .encodeToString(org.openedx.discussion.domain.model.Thread.serializer(), thread)
                        navController.navigate(AppNavRoutes.DiscussionComments(threadJson = json))
                    },
                    onSearchTextChanged = { vm.searchThreads(it) },
                    onSwipeRefresh = { vm.searchThreads("") }, paginationCallback = { vm.fetchMore() },
                    onBackClick = { navController.navigateUp() },
                )
            }
        }
    }
}

