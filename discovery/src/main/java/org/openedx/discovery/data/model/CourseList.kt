package org.openedx.discovery.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.Pagination

data class CourseList(
    @SerialName("pagination")
    val pagination: Pagination,
    @SerialName("results")
    val results: List<CourseDetails>?,
)
