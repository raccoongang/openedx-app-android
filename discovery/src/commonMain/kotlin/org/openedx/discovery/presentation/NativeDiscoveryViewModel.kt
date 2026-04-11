package org.openedx.discovery.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.discovery.domain.model.Course
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class NativeDiscoveryViewModel(
    private val config: Config,
    private val networkConnection: NetworkConnection,
    private val interactor: DiscoveryInteractor,
    private val resourceManager: ResourceManager,
    private val analytics: DiscoveryAnalytics,
    private val corePreferences: CorePreferences,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    val apiHostUrl get() = config.getApiHostURL()
    val isUserLoggedIn get() = corePreferences.user != null
    val canShowBackButton get() = config.isPreLoginExperienceEnabled() && !isUserLoggedIn
    val isRegistrationEnabled: Boolean get() = config.isRegistrationEnabled()

    private val _uiState = MutableStateFlow<DiscoveryUIState>(DiscoveryUIState.Loading)
    val uiState: StateFlow<DiscoveryUIState> = _uiState.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    val hasInternetConnection: Boolean
        get() = networkConnection.isOnline()

    private var page = 1
    private val coursesList = mutableListOf<Course>()
    private var isLoading = false

    init {
        getCoursesList()
    }

    private fun loadCoursesInternal(
        username: String? = null,
        organization: String? = null
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = if (networkConnection.isOnline() || page > 1) {
                    interactor.getCoursesList(username, organization, page)
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
                    coursesList.addAll(response.results)
                } else {
                    val cachedList = interactor.getCoursesListFromCache()
                    _canLoadMore.value = false
                    page = -1
                    coursesList.addAll(cachedList)
                }
                _uiState.value = DiscoveryUIState.Courses(ArrayList(coursesList))
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun getCoursesList(
        username: String? = null,
        organization: String? = null
    ) {
        _uiState.value = DiscoveryUIState.Loading
        coursesList.clear()
        loadCoursesInternal(username, organization)
    }

    fun updateData(
        username: String? = null,
        organization: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isUpdating.value = true
                isLoading = true
                page = 1
                val response = interactor.getCoursesList(username, organization, page)
                if (response.pagination.next.isNotEmpty() && page != response.pagination.numPages) {
                    _canLoadMore.value = true
                    page++
                } else {
                    _canLoadMore.value = false
                    page = -1
                }
                coursesList.clear()
                coursesList.addAll(response.results)
                _uiState.value = DiscoveryUIState.Courses(ArrayList(coursesList))
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            } finally {
                isLoading = false
                _isUpdating.value = false
            }
        }
    }

    fun fetchMore() {
        if (!isLoading && page != -1) {
            loadCoursesInternal()
        }
    }

    fun discoverySearchBarClickedEvent() {
        analytics.discoverySearchBarClickedEvent()
    }

    fun discoveryCourseClicked(courseId: String, courseName: String) {
        analytics.discoveryCourseClickedEvent(courseId, courseName)
    }

    fun courseDetailClickedEvent(courseId: String, courseTitle: String) {
        val event = DiscoveryAnalyticsEvent.COURSE_INFO
        analytics.logEvent(
            event.eventName,
            buildMap {
                put(DiscoveryAnalyticsKey.NAME.key, event.biValue)
                put(DiscoveryAnalyticsKey.COURSE_ID.key, courseId)
                put(DiscoveryAnalyticsKey.COURSE_NAME.key, courseTitle)
                put(DiscoveryAnalyticsKey.CATEGORY.key, DiscoveryAnalyticsKey.DISCOVERY.key)
            }
        )
    }
}
