package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.OfflineDownload

@Serializable
data class OfflineDownload(
    @SerialName("file_url")
    var fileUrl: String? = null,
    @SerialName("last_modified")
    var lastModified: String? = null,
    @SerialName("file_size")
    var fileSize: Long? = null,
) {
    fun mapToDomain() = OfflineDownload(
        fileUrl = fileUrl ?: "",
        lastModified = lastModified,
        fileSize = fileSize ?: 0
    )
}
