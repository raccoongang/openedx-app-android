package org.openedx.discovery.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.presentation.global.AppData
import org.openedx.core.presentation.global.ErrorType
import org.openedx.core.presentation.global.webview.WebViewUIState
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.utils.UrlUtils

class WebViewDiscoveryViewModel(
    private val querySearch: String,
    private val appData: AppData,
    private val config: Config,
    private val networkConnection: NetworkConnection,
    private val corePreferences: CorePreferences,
    private val analytics: DiscoveryAnalytics,
    private val resourceManager: ResourceManager,
) : BaseViewModel(resourceManager) {

    private val _uiState = MutableStateFlow<WebViewUIState>(WebViewUIState.Loading)
    val uiState: StateFlow<WebViewUIState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<WebViewDiscoveryNavEvent>()
    val navigationEvent: SharedFlow<WebViewDiscoveryNavEvent> get() = _navigationEvent.asSharedFlow()

    val uriScheme: String get() = config.getUriScheme()

    private val webViewConfig get() = config.getDiscoveryConfig().webViewConfig

    val isPreLogin get() = config.isPreLoginExperienceEnabled() && corePreferences.user == null
    val isRegistrationEnabled: Boolean get() = config.isRegistrationEnabled()

    val appUserAgent get() = appData.appUserAgent

    private var _discoveryUrl = webViewConfig.baseUrl
    val discoveryUrl: String
        get() {
            return if (querySearch.isNotBlank()) {
                val queryParams: MutableMap<String, String> = HashMap()
                queryParams[UrlUtils.QUERY_PARAM_SEARCH] = querySearch
                UrlUtils.buildUrlWithQueryParams(_discoveryUrl, queryParams)
            } else {
                _discoveryUrl
            }
        }

    val hasInternetConnection: Boolean
        get() = networkConnection.isOnline()

    fun onWebPageLoading() {
        _uiState.value = WebViewUIState.Loading
    }

    fun onWebPageLoaded() {
        _uiState.value = WebViewUIState.Loaded
    }

    fun onWebPageLoadError() {
        _uiState.value = WebViewUIState.Error(
            if (networkConnection.isOnline()) {
                ErrorType.UNKNOWN_ERROR
            } else {
                ErrorType.CONNECTION_ERROR
            }
        )
    }

    fun updateDiscoveryUrl(url: String) {
        if (url.isNotEmpty()) {
            _discoveryUrl = url
        }
    }

    fun infoCardClicked(pathId: String, infoType: String) {
        if (pathId.isNotEmpty() && infoType.isNotEmpty()) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    WebViewDiscoveryNavEvent.CourseInfo(pathId = pathId, infoType = infoType)
                )
            }
        }
    }

    fun navigateToSignUp() {
        viewModelScope.launch {
            _navigationEvent.emit(WebViewDiscoveryNavEvent.SignUp)
        }
    }

    fun navigateToSignIn() {
        viewModelScope.launch {
            _navigationEvent.emit(WebViewDiscoveryNavEvent.SignIn)
        }
    }

    fun navigateToSettings() {
        viewModelScope.launch {
            _navigationEvent.emit(WebViewDiscoveryNavEvent.Settings)
        }
    }

    fun courseInfoClickedEvent(courseId: String) {
        logEvent(DiscoveryAnalyticsEvent.COURSE_INFO, courseId)
    }

    fun programInfoClickedEvent(courseId: String) {
        logEvent(DiscoveryAnalyticsEvent.PROGRAM_INFO, courseId)
    }

    private fun logEvent(
        event: DiscoveryAnalyticsEvent,
        courseId: String,
    ) {
        analytics.logScreenEvent(
            event.eventName,
            buildMap {
                put(DiscoveryAnalyticsKey.NAME.key, event.biValue)
                put(DiscoveryAnalyticsKey.COURSE_ID.key, courseId)
                put(DiscoveryAnalyticsKey.CATEGORY.key, DiscoveryAnalyticsKey.DISCOVERY.key)
            }
        )
    }
}

sealed class WebViewDiscoveryNavEvent {
    data class CourseInfo(val pathId: String, val infoType: String) : WebViewDiscoveryNavEvent()
    data object SignUp : WebViewDiscoveryNavEvent()
    data object SignIn : WebViewDiscoveryNavEvent()
    data object Settings : WebViewDiscoveryNavEvent()
}
