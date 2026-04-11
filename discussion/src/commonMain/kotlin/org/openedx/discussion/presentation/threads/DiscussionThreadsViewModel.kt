package org.openedx.discussion.presentation.threads

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.discussion.domain.interactor.DiscussionInteractor
import org.openedx.discussion.presentation.topics.DiscussionTopicsViewModel
import org.openedx.discussion.system.notifier.DiscussionNotifier
import org.openedx.discussion.system.notifier.DiscussionThreadAdded
import org.openedx.discussion.system.notifier.DiscussionThreadDataChanged
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class DiscussionThreadsViewModel(
    private val interactor: DiscussionInteractor,
    private val resourceManager: ResourceManager,
    private val notifier: DiscussionNotifier,
    val courseId: String,
    val topicId: String,
    private val threadType: String
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val _uiState = MutableStateFlow<DiscussionThreadsUIState?>(null)
    val uiState: StateFlow<DiscussionThreadsUIState?> = _uiState.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private val threadsList = mutableListOf<org.openedx.discussion.domain.model.Thread>()
    private var nextPage = 1
    private var isLoading = false
    private var lastOrderBy = ""
    private var filterType: String? = null

    private var isBlockAlreadyCompleted = false

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            notifier.notifier.collect {
                if (it is DiscussionThreadAdded) {
                    if (lastOrderBy.isNotEmpty()) {
                        updateThread(lastOrderBy)
                    }
                } else if (it is DiscussionThreadDataChanged) {
                    val index = threadsList.indexOfFirst { thread ->
                        thread.id == it.thread.id
                    }
                    if (index >= 0) {
                        threadsList[index] = it.thread
                        _uiState.value = DiscussionThreadsUIState.Threads(threadsList.toList())
                    }
                }
            }
        }
    }

    init {
        getThreadByType(SortType.LAST_ACTIVITY_AT.queryParam)
    }

    fun getThreadByType(orderBy: String) {
        _uiState.value = DiscussionThreadsUIState.Loading
        internalLoadThreads(orderBy)
    }

    fun updateThread(orderBy: String) {
        _isUpdating.value = true
        threadsList.clear()
        nextPage = 1
        internalLoadThreads(orderBy)
    }

    fun fetchMore() {
        if (!isLoading && nextPage != -1) {
            isLoading = true
            internalLoadThreads(lastOrderBy)
        }
    }

    private fun internalLoadThreads(orderBy: String) {
        if (lastOrderBy != orderBy) {
            threadsList.clear()
            nextPage = 1
        }
        lastOrderBy = orderBy
        when (threadType) {
            DiscussionTopicsViewModel.ALL_POSTS -> {
                getAllThreads(orderBy)
            }

            DiscussionTopicsViewModel.FOLLOWING_POSTS -> {
                getFollowingThreads(orderBy)
            }

            DiscussionTopicsViewModel.TOPIC -> {
                getThreads(
                    topicId,
                    orderBy
                )
            }
        }
    }

    fun filterThreads(filter: String?) {
        if (filterType != filter || (filter != FilterType.ALL_POSTS.value && filterType.isNullOrEmpty())) {
            threadsList.clear()
            nextPage = 1
        }
        filterType = if (filter == FilterType.ALL_POSTS.value) {
            null
        } else {
            filter
        }
        when (threadType) {
            DiscussionTopicsViewModel.ALL_POSTS -> {
                getAllThreads(lastOrderBy)
            }

            DiscussionTopicsViewModel.FOLLOWING_POSTS -> {
                getFollowingThreads(lastOrderBy)
            }

            DiscussionTopicsViewModel.TOPIC -> {
                getThreads(
                    topicId,
                    lastOrderBy
                )
            }
        }
    }

    private fun getThreads(topicId: String, orderBy: String) {
        viewModelScope.launch {
            try {
                val response =
                    interactor.getThreads(courseId, topicId, orderBy, filterType, nextPage)
                if (response.pagination.next.isNotEmpty()) {
                    _canLoadMore.value = true
                    nextPage++
                } else {
                    _canLoadMore.value = false
                    nextPage = -1
                }
                threadsList.addAll(response.results)
                _uiState.value = DiscussionThreadsUIState.Threads(threadsList.toList())
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
            _isUpdating.value = false
            isLoading = false
        }
    }

    private fun getAllThreads(orderBy: String) {
        viewModelScope.launch {
            try {
                val response = interactor.getAllThreads(courseId, orderBy, filterType, nextPage)
                if (response.pagination.next.isNotEmpty()) {
                    _canLoadMore.value = true
                    nextPage++
                } else {
                    _canLoadMore.value = false
                    nextPage = -1
                }
                threadsList.addAll(response.results)
                _uiState.value = DiscussionThreadsUIState.Threads(threadsList.toList())
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
            _isUpdating.value = false
            isLoading = false
        }
    }

    private fun getFollowingThreads(orderBy: String) {
        viewModelScope.launch {
            try {
                val response =
                    interactor.getFollowingThreads(courseId, true, orderBy, page = nextPage)
                if (response.pagination.next.isNotEmpty()) {
                    _canLoadMore.value = true
                    nextPage++
                } else {
                    _canLoadMore.value = false
                    nextPage = -1
                }
                threadsList.addAll(response.results)
                _uiState.value = DiscussionThreadsUIState.Threads(threadsList.toList())
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
            _isUpdating.value = false
            isLoading = false
        }
    }

    fun markBlockCompleted(blockId: String) {
        if (!isBlockAlreadyCompleted) {
            viewModelScope.launch {
                try {
                    isBlockAlreadyCompleted = true
                    interactor.markBlocksCompletion(
                        courseId,
                        listOf(blockId)
                    )
                } catch (e: Exception) {
                    isBlockAlreadyCompleted = false
                    e.printStackTrace()
                }
            }
        }
    }
}
