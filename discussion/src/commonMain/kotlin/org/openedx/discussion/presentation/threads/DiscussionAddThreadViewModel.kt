package org.openedx.discussion.presentation.threads

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.discussion.domain.interactor.DiscussionInteractor
import org.openedx.discussion.domain.model.Thread
import org.openedx.discussion.system.notifier.DiscussionNotifier
import org.openedx.discussion.system.notifier.DiscussionThreadAdded
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class DiscussionAddThreadViewModel(
    private val interactor: DiscussionInteractor,
    private val resourceManager: ResourceManager,
    private val notifier: DiscussionNotifier,
    private val courseId: String
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val _newThread = MutableStateFlow<Thread?>(null)
    val newThread: StateFlow<Thread?> = _newThread.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun createThread(
        topicId: String,
        type: String,
        title: String,
        rawBody: String,
        follow: Boolean
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _newThread.value = interactor.createThread(topicId, courseId, type, title, rawBody, follow)
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
            _isLoading.value = false
        }
    }

    private fun getCachedTopics() = interactor.getCachedTopics(courseId)

    fun getHandledTopics(): List<Pair<String, String>> {
        val topics = getCachedTopics().filterNot { it.id == "" }
        return topics.map { Pair(it.name, it.id) }
    }

    fun getHandledTopicById(topicId: String): Pair<String, String> {
        val topics = getHandledTopics()
        return topics.find { it.second == topicId } ?: topics.firstOrNull() ?: Pair("", "")
    }

    fun sendThreadAdded() {
        viewModelScope.launch {
            notifier.send(DiscussionThreadAdded())
        }
    }
}
