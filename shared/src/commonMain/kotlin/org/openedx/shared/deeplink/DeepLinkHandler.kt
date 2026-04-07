package org.openedx.shared.deeplink

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Common deep link handling interface.
 * Platform-specific implementations handle Branch SDK integration.
 */
data class DeepLinkData(
    val courseId: String? = null,
    val screenName: String? = null,
    val pathId: String? = null,
    val componentId: String? = null,
    val topicId: String? = null,
    val threadId: String? = null,
    val commentId: String? = null,
)

interface DeepLinkHandler {
    val deepLinks: Flow<DeepLinkData>
    fun handleDeepLink(params: Map<String, String>)
}

class DefaultDeepLinkHandler : DeepLinkHandler {
    private val _deepLinks = MutableSharedFlow<DeepLinkData>(replay = 1)
    override val deepLinks: Flow<DeepLinkData> = _deepLinks

    override fun handleDeepLink(params: Map<String, String>) {
        _deepLinks.tryEmit(
            DeepLinkData(
                courseId = params["course_id"],
                screenName = params["screen_name"],
                pathId = params["path_id"],
                componentId = params["component_id"],
                topicId = params["topic_id"],
                threadId = params["thread_id"],
                commentId = params["comment_id"],
            )
        )
    }
}
