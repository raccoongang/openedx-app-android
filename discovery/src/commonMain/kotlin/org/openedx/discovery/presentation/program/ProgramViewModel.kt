package org.openedx.discovery.presentation.program

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.presentation.global.AppData
import org.openedx.core.presentation.global.ErrorType
import org.openedx.core.system.AppCookieManager
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseDashboardUpdate
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.core.system.notifier.NavigationToDiscovery
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class ProgramViewModel(
    private val appData: AppData,
    private val config: Config,
    private val networkConnection: NetworkConnection,
    private val notifier: DiscoveryNotifier,
    private val edxCookieManager: AppCookieManager,
    private val resourceManager: ResourceManager,
    private val interactor: DiscoveryInteractor,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {
    val uriScheme: String get() = config.getUriScheme()

    val programConfig get() = config.getProgramConfig().webViewConfig

    val cookieManager get() = edxCookieManager

    val hasInternetConnection: Boolean get() = networkConnection.isOnline()

    val appUserAgent get() = appData.appUserAgent

    private val _uiState = MutableStateFlow<ProgramUIState>(ProgramUIState.Loading)
    val uiState: StateFlow<ProgramUIState> get() = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ProgramNavEvent>()
    val navigationEvent: SharedFlow<ProgramNavEvent> get() = _navigationEvent.asSharedFlow()

    fun showLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _uiState.emit(if (isLoading) ProgramUIState.Loading else ProgramUIState.Loaded)
        }
    }

    fun enrollInACourse(courseId: String) {
        showLoading(true)
        viewModelScope.launch {
            try {
                interactor.enrollInACourse(courseId)
                _uiState.emit(ProgramUIState.CourseEnrolled(courseId, true))
                notifier.send(CourseDashboardUpdate())
            } catch (e: Exception) {
                if (e.isInternetError()) {
                    _uiState.emit(
                        ProgramUIState.UiMessage(
                            UIMessage.SnackBarMessage(
                                resolveErrorMessage(
                                    throwable = e,
                                )
                            )
                        )
                    )
                } else {
                    _uiState.emit(ProgramUIState.CourseEnrolled(courseId, false))
                }
            }
        }
    }

    fun onProgramCardClick(pathId: String) {
        if (pathId.isNotEmpty()) {
            viewModelScope.launch {
                _navigationEvent.emit(ProgramNavEvent.EnrolledProgramInfo(pathId = pathId))
            }
        }
    }

    fun onViewCourseClick(courseId: String, infoType: String) {
        if (courseId.isNotEmpty() && infoType.isNotEmpty()) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    ProgramNavEvent.CourseInfo(courseId = courseId, infoType = infoType)
                )
            }
        }
    }

    fun onEnrolledCourseClick(courseId: String) {
        if (courseId.isNotEmpty()) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    ProgramNavEvent.CourseOutline(courseId = courseId, courseTitle = "")
                )
            }
        }
        viewModelScope.launch {
            _uiState.emit(ProgramUIState.Loaded)
        }
    }

    fun navigateToDiscovery() {
        viewModelScope.launch { notifier.send(NavigationToDiscovery()) }
    }

    fun navigateToSettings() {
        viewModelScope.launch {
            _navigationEvent.emit(ProgramNavEvent.Settings)
        }
    }

    fun onPageLoadError() {
        viewModelScope.launch {
            _uiState.emit(
                ProgramUIState.Error(
                    if (networkConnection.isOnline()) {
                        ErrorType.UNKNOWN_ERROR
                    } else {
                        ErrorType.CONNECTION_ERROR
                    }
                )
            )
        }
    }
}

sealed class ProgramNavEvent {
    data class EnrolledProgramInfo(val pathId: String) : ProgramNavEvent()
    data class CourseInfo(val courseId: String, val infoType: String) : ProgramNavEvent()
    data class CourseOutline(val courseId: String, val courseTitle: String) : ProgramNavEvent()
    data object Settings : ProgramNavEvent()
}
