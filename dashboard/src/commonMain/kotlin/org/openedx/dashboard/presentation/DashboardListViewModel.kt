package org.openedx.dashboard.presentation

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseDashboardUpdate
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.dashboard.domain.interactor.DashboardInteractor
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class DashboardListViewModel(
    private val config: Config,
    private val networkConnection: NetworkConnection,
    private val interactor: DashboardInteractor,
    private val resourceManager: ResourceManager,
    private val discoveryNotifier: DiscoveryNotifier,
    private val analytics: DashboardAnalytics,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val coursesList = mutableListOf<EnrolledCourse>()
    private var page = 1
    private var isLoading = false

    val apiHostUrl get() = config.getApiHostURL()

    private val _uiState = MutableStateFlow<DashboardUIState>(DashboardUIState.Loading)
    val uiState: StateFlow<DashboardUIState> = _uiState.asStateFlow()

    private val _updating = MutableStateFlow(false)
    val updating: StateFlow<Boolean> = _updating.asStateFlow()

    val hasInternetConnection: Boolean
        get() = networkConnection.isOnline()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            discoveryNotifier.notifier.collect {
                if (it is CourseDashboardUpdate) {
                    updateCourses()
                }
            }
        }
    }

    init {
        getCourses()
    }

    fun getCourses() {
        _uiState.value = DashboardUIState.Loading
        coursesList.clear()
        internalLoadingCourses()
    }

    fun updateCourses() {
        if (isLoading) {
            return
        }
        viewModelScope.launch {
            try {
                _updating.value = true
                isLoading = true
                page = 1
                val response = interactor.getEnrolledCourses(page)
                if (response.pagination.next.isNotEmpty() && page != response.pagination.numPages) {
                    _canLoadMore.value = true
                    page++
                } else {
                    _canLoadMore.value = false
                    page = -1
                }
                coursesList.clear()
                coursesList.addAll(response.courses)
                if (coursesList.isEmpty()) {
                    _uiState.value = DashboardUIState.Empty
                } else {
                    _uiState.value = DashboardUIState.Courses(ArrayList(coursesList))
                }
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
            _updating.value = false
            isLoading = false
        }
    }

    private fun internalLoadingCourses() {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = if (networkConnection.isOnline() || page > 1) {
                    interactor.getEnrolledCourses(page)
                } else {
                    null
                }
                if (response != null) {
                    if (response.pagination.next.isNotEmpty() && page != response.pagination.numPages) {
                        _canLoadMore.value = true
                        page++
                    } else {
                        _canLoadMore.value = false
                        page = -1
                    }
                    coursesList.addAll(response.courses)
                } else {
                    val cachedList = interactor.getEnrolledCoursesFromCache()
                    _canLoadMore.value = false
                    page = -1
                    coursesList.addAll(cachedList)
                }
                if (coursesList.isEmpty()) {
                    _uiState.value = DashboardUIState.Empty
                } else {
                    _uiState.value = DashboardUIState.Courses(ArrayList(coursesList))
                }
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
            _updating.value = false
            isLoading = false
        }
    }

    fun fetchMore() {
        if (!isLoading && page != -1) {
            internalLoadingCourses()
        }
    }

    fun dashboardCourseClickedEvent(courseId: String, courseName: String) {
        analytics.dashboardCourseClickedEvent(courseId, courseName)
    }
}
