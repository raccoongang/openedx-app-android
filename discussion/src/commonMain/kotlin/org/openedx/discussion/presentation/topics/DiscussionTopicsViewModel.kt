package org.openedx.discussion.presentation.topics

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.system.notifier.CourseLoading
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.RefreshDiscussions
import org.openedx.discussion.domain.interactor.DiscussionInteractor
import org.openedx.discussion.presentation.DiscussionAnalytics
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class DiscussionTopicsViewModel(
    val courseId: String,
    private val courseTitle: String,
    private val interactor: DiscussionInteractor,
    private val resourceManager: ResourceManager,
    private val analytics: DiscussionAnalytics,
    private val courseNotifier: CourseNotifier,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val _uiState = MutableStateFlow<DiscussionTopicsUIState>(DiscussionTopicsUIState.Loading)
    val uiState: StateFlow<DiscussionTopicsUIState> = _uiState.asStateFlow()

    init {
        collectCourseNotifier()

        getCourseTopic()
    }

    private fun getCourseTopic() {
        viewModelScope.launch {
            try {
                val response = interactor.getCourseTopics(courseId)
                if (response.isEmpty().not()) {
                    _uiState.value = DiscussionTopicsUIState.Topics(response)
                } else {
                    _uiState.value = DiscussionTopicsUIState.Error
                }
            } catch (e: Exception) {
                _uiState.value = DiscussionTopicsUIState.Error
                if (e.isInternetError()) {
                    handleErrorUiMessage(
                        throwable = e,
                    )
                }
            } finally {
                courseNotifier.send(CourseLoading(false))
            }
        }
    }

    fun discussionClickedEvent(action: String, data: String, title: String) {
        when (action) {
            ALL_POSTS -> {
                analytics.discussionAllPostsClickedEvent(courseId, courseTitle)
            }

            FOLLOWING_POSTS -> {
                analytics.discussionFollowingClickedEvent(courseId, courseTitle)
            }

            TOPIC -> {
                analytics.discussionTopicClickedEvent(courseId, courseTitle, data, title)
            }
        }
    }

    private fun collectCourseNotifier() {
        viewModelScope.launch {
            courseNotifier.notifier.collect { event ->
                when (event) {
                    is RefreshDiscussions -> getCourseTopic()
                }
            }
        }
    }

    companion object DiscussionTopic {
        const val TOPIC = "Topic"
        const val ALL_POSTS = "All posts"
        const val FOLLOWING_POSTS = "Following"
    }
}
