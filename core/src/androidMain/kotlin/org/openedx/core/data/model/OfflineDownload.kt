package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.room.OfflineDownloadDb
import org.openedx.core.domain.model.OfflineDownload

@Serializable
data class OfflineDownload(
    @SerialName("file_url")
    var fileUrl: String?,
    @SerialName("last_modified")
    var lastModified: String?,
    @SerialName("file_size")
    var fileSize: Long?,
) {
    fun mapToDomain() = OfflineDownload(
        fileUrl = fileUrl ?: "",
        lastModified = lastModified,
        fileSize = fileSize ?: 0
    )

    fun mapToRoomEntity() = OfflineDownloadDb(
        fileUrl = fileUrl ?: "",
        lastModified = lastModified,
        fileSize = fileSize ?: 0
    )
}
