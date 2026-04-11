package org.openedx.discovery.presentation.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryAnalytics
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class CourseSearchViewModel(
    private val config: Config,
    private val corePreferences: CorePreferences,
    private val interactor: DiscoveryInteractor,
    private val resourceManager: ResourceManager,
    private val analytics: DiscoveryAnalytics
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    val apiHostUrl get() = config.getApiHostURL()
    val isUserLoggedIn get() = corePreferences.user != null
    val isRegistrationEnabled: Boolean get() = config.isRegistrationEnabled()

    private val _uiState =
        MutableStateFlow<CourseSearchUIState>(CourseSearchUIState.Courses(emptyList(), 0))
    val uiState: StateFlow<CourseSearchUIState> = _uiState.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private var nextPage: Int? = 1
    private var currentQuery: String? = null
    private val coursesList = mutableListOf<Course>()
    private var isLoading = false

    private val queryChannel = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 0)

    init {
        observeQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            queryChannel
                .asSharedFlow()
                .debounce(SEARCH_DEBOUNCE)
                .collect {
                    nextPage = 1
                    currentQuery = it
                    coursesList.clear()
                    _uiState.value = CourseSearchUIState.Loading
                    loadCoursesInternal(currentQuery!!, nextPage!!)
                }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            if (query.trim().isNotEmpty()) {
                queryChannel.emit(query.trim())
            } else {
                currentQuery = null
                nextPage = 1
                coursesList.clear()
                _uiState.value = CourseSearchUIState.Courses(emptyList(), 0)
            }
        }
    }

    fun fetchMore() {
        if (!isLoading && nextPage != null) {
            currentQuery?.let {
                loadCoursesInternal(it, nextPage!!)
            }
        }
    }

    fun updateSearchQuery() {
        currentQuery?.let {
            nextPage = 1
            isLoading = true
            _isUpdating.value = true
            coursesList.clear()
            loadCoursesInternal(it, nextPage!!)
        }
    }

    private fun loadCoursesInternal(query: String, page: Int) {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = interactor.getCoursesListByQuery(query, page)
                if (response.pagination.next.isNotEmpty() && page < response.pagination.numPages) {
                    _canLoadMore.value = true
                    nextPage = page + 1
                } else {
                    _canLoadMore.value = false
                    nextPage = null
                }
                discoveryCourseSearchEvent(query, response.pagination.count)
                coursesList.addAll(response.results)
                _uiState.value = CourseSearchUIState.Courses(coursesList, response.pagination.count)
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

    private fun discoveryCourseSearchEvent(query: String, coursesCount: Int) {
        if (query.isNotEmpty()) {
            analytics.discoveryCourseSearchEvent(query, coursesCount)
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE = 400L
    }
}
