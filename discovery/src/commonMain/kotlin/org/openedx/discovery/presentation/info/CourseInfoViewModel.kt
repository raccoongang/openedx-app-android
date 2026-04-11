package org.openedx.discovery.presentation.info

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.presentation.CoreAnalyticsKey
import org.openedx.core.presentation.global.AppData
import org.openedx.core.presentation.global.ErrorType
import org.openedx.core.presentation.global.webview.WebViewUIState
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseDashboardUpdate
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.discovery.Res
import org.openedx.discovery.discovery_enrolled_successfully
import org.openedx.discovery.discovery_you_are_already_enrolled
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.discovery.presentation.DiscoveryAnalytics
import org.openedx.discovery.presentation.DiscoveryAnalyticsEvent
import org.openedx.discovery.presentation.DiscoveryAnalyticsKey
import org.openedx.discovery.presentation.catalog.WebViewLink
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class CourseInfoViewModel(
    val pathId: String,
    val infoType: String,
    private val appData: AppData,
    private val config: Config,
    private val networkConnection: NetworkConnection,
    private val interactor: DiscoveryInteractor,
    private val notifier: DiscoveryNotifier,
    private val resourceManager: ResourceManager,
    private val analytics: DiscoveryAnalytics,
    corePreferences: CorePreferences,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val _uiState =
        MutableStateFlow(
            CourseInfoUIState.CourseInfo(
                initialUrl = getInitialUrl(),
                isPreLogin = config.isPreLoginExperienceEnabled() && corePreferences.user == null
            )
        )
    val uiState: StateFlow<CourseInfoUIState> = _uiState

    private val _webViewUIState = MutableStateFlow<WebViewUIState>(WebViewUIState.Loading)
    val webViewState
        get() = _webViewUIState.asStateFlow()

    private val _showAlert = MutableSharedFlow<Boolean>()
    val showAlert: SharedFlow<Boolean>
        get() = _showAlert.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<CourseInfoNavEvent>()
    val navigationEvent: SharedFlow<CourseInfoNavEvent>
        get() = _navigationEvent.asSharedFlow()

    val hasInternetConnection: Boolean
        get() = networkConnection.isOnline()

    val isRegistrationEnabled: Boolean get() = config.isRegistrationEnabled()

    val uriScheme: String get() = config.getUriScheme()

    val appUserAgent get() = appData.appUserAgent

    private val webViewConfig get() = config.getDiscoveryConfig().webViewConfig

    private fun getInitialUrl(): String {
        val urlTemplate = when (infoType) {
            WebViewLink.Authority.COURSE_INFO.name -> webViewConfig.courseUrlTemplate
            WebViewLink.Authority.PROGRAM_INFO.name -> webViewConfig.programUrlTemplate
            else -> webViewConfig.baseUrl
        }
        return if (pathId.isEmpty() || infoType.isEmpty()) {
            webViewConfig.baseUrl
        } else {
            urlTemplate.replace("{${ARG_PATH_ID}}", pathId)
        }
    }

    fun enrollInACourse(courseId: String) {
        viewModelScope.launch {
            _showAlert.emit(false)
            try {
                val isCourseEnrolled = withContext(Dispatchers.Default) {
                    interactor.getCourseDetails(courseId)
                }.isEnrolled

                if (isCourseEnrolled) {
                    sendMessage(
                        UIMessage.ToastMessage(resourceManager.getString(Res.string.discovery_you_are_already_enrolled))
                    )
                    _uiState.update { it.copy(enrolledCourseId = courseId) }
                    return@launch
                }

                interactor.enrollInACourse(courseId)
                courseEnrollSuccessEvent(courseId)
                notifier.send(CourseDashboardUpdate())
                sendMessage(
                    UIMessage.ToastMessage(resourceManager.getString(Res.string.discovery_enrolled_successfully))
                )
                _uiState.update { it.copy(enrolledCourseId = courseId) }
            } catch (e: Exception) {
                if (e.isInternetError()) {
                    handleErrorUiMessage(
                        throwable = e,
                    )
                } else {
                    _showAlert.emit(true)
                }
            }
        }
    }

    fun onSuccessfulCourseEnrollment(courseId: String) {
        if (courseId.isNotEmpty()) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    CourseInfoNavEvent.CourseOutline(
                        courseId = courseId,
                        courseTitle = "",
                    )
                )
            }
        }
    }

    fun infoCardClicked(pathId: String, infoType: String) {
        if (pathId.isNotEmpty() && infoType.isNotEmpty()) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    CourseInfoNavEvent.CourseInfo(
                        pathId = pathId,
                        infoType = infoType,
                    )
                )
            }
        }
    }

    fun navigateToSignUp(courseId: String?, infoType: String) {
        viewModelScope.launch {
            _navigationEvent.emit(
                CourseInfoNavEvent.SignUp(courseId = courseId, infoType = infoType)
            )
        }
    }

    fun navigateToSignIn(courseId: String, infoType: String) {
        viewModelScope.launch {
            _navigationEvent.emit(
                CourseInfoNavEvent.SignIn(courseId = courseId, infoType = infoType)
            )
        }
    }

    fun courseInfoClickedEvent(courseId: String) {
        logScreenEvent(DiscoveryAnalyticsEvent.COURSE_INFO, courseId)
    }

    fun programInfoClickedEvent(courseId: String) {
        logScreenEvent(DiscoveryAnalyticsEvent.PROGRAM_INFO, courseId)
    }

    fun courseEnrollClickedEvent(courseId: String) {
        logEvent(DiscoveryAnalyticsEvent.COURSE_ENROLL_CLICKED, courseId)
    }

    private fun courseEnrollSuccessEvent(courseId: String) {
        logEvent(DiscoveryAnalyticsEvent.COURSE_ENROLL_SUCCESS, courseId)
    }

    private fun logEvent(
        event: DiscoveryAnalyticsEvent,
        courseId: String,
    ) {
        analytics.logEvent(event.eventName, buildEventDataMap(event, courseId))
    }

    private fun logScreenEvent(
        event: DiscoveryAnalyticsEvent,
        courseId: String,
    ) {
        analytics.logScreenEvent(event.eventName, buildEventDataMap(event, courseId))
    }

    private fun buildEventDataMap(
        event: DiscoveryAnalyticsEvent,
        courseId: String,
    ): Map<String, String> {
        return buildMap {
            put(DiscoveryAnalyticsKey.NAME.key, event.biValue)
            put(DiscoveryAnalyticsKey.COURSE_ID.key, courseId)
            put(DiscoveryAnalyticsKey.CATEGORY.key, CoreAnalyticsKey.DISCOVERY.key)
            put(DiscoveryAnalyticsKey.CONVERSION.key, courseId)
        }
    }

    fun onWebPageLoaded() {
        _webViewUIState.value = WebViewUIState.Loaded
    }

    fun onWebPageError() {
        _webViewUIState.value = WebViewUIState.Error(
            if (networkConnection.isOnline()) {
                ErrorType.UNKNOWN_ERROR
            } else {
                ErrorType.CONNECTION_ERROR
            }
        )
    }

    fun onWebPageLoading() {
        _webViewUIState.value = WebViewUIState.Loading
    }

    companion object {
        private const val ARG_PATH_ID = "path_id"
    }
}

sealed class CourseInfoNavEvent {
    data class CourseOutline(val courseId: String, val courseTitle: String) : CourseInfoNavEvent()
    data class CourseInfo(val pathId: String, val infoType: String) : CourseInfoNavEvent()
    data class SignUp(val courseId: String?, val infoType: String) : CourseInfoNavEvent()
    data class SignIn(val courseId: String, val infoType: String) : CourseInfoNavEvent()
}
