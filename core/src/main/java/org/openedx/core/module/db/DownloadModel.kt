package org.openedx.core.module.db


data class DownloadModel(
    val id: String,
    val title: String,
    val courseId: String,
    val size: Long,
    val path: String,
    val url: String,
    val type: FileType,
    val downloadedState: DownloadedState,
    val lastModified: String? = null,
)

enum class DownloadedState {
    WAITING, DOWNLOADING, DOWNLOADED, NOT_DOWNLOADED, LOADING_COURSE_STRUCTURE;

    val isWaitingOrDownloading: Boolean
        get() {
            return this == WAITING || this == DOWNLOADING || this == LOADING_COURSE_STRUCTURE
        }

    val isDownloaded: Boolean
        get() {
            return this == DOWNLOADED
        }
}

enum class FileType {
    VIDEO, X_BLOCK
}
