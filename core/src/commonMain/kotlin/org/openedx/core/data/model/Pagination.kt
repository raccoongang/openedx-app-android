package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.Pagination as domainPagination

@Serializable
data class Pagination(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("next")
    val next: String? = null,
    @SerialName("num_pages")
    val numPages: Int? = null,
    @SerialName("previous")
    val previous: String? = null,
) {
    fun mapToDomain() = domainPagination(
        count = count ?: 0,
        next = next ?: "",
        numPages = numPages ?: 0,
        previous = previous ?: ""
    )
}
