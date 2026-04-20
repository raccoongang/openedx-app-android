package org.openedx.discussion.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.discussion.domain.model.TopicsData

@Serializable
data class TopicsResponse(
    @SerialName("courseware_topics")
    val coursewareTopics: List<Topic>? = null,
    @SerialName("non_courseware_topics")
    val nonCoursewareTopics: List<Topic>? = null
) {

    @Serializable
    data class Topic(
        @SerialName("id")
        val id: String? = null,
        @SerialName("name")
        val name: String? = null,
        @SerialName("thread_list_url")
        val threadListUrl: String? = null,
        @SerialName("children")
        val children: List<Topic>? = null
    ) {
        fun mapToDomain(): org.openedx.discussion.domain.model.Topic {
            return org.openedx.discussion.domain.model.Topic(
                id = id ?: "",
                name = name ?: "",
                threadListUrl = threadListUrl ?: "",
                children = children?.map { it.mapToDomain() } ?: emptyList()
            )
        }
    }

    fun mapToDomain(): TopicsData {
        return TopicsData(
            coursewareTopics = coursewareTopics?.map { it.mapToDomain() } ?: emptyList(),
            nonCoursewareTopics = nonCoursewareTopics?.map { it.mapToDomain() } ?: emptyList()
        )
    }
}
