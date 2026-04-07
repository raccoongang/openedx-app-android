package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.Pagination as domainPagination

@Serializable
data class Pagination(
    @SerialName("count")
    val count: Int?,
    @SerialName("next")
    val next: String?,
    @SerialName("num_pages")
    val numPages: Int?,
    @SerialName("previous")
    val previous: String?,
) {
    fun mapToDomain() = domainPagination(
        count = count ?: 0,
        next = next ?: "",
        numPages = numPages ?: 0,
        previous = previous ?: ""
    )
}
