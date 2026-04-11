package org.openedx.core.domain.model

data class VideoProgress(
    val blockId: String,
    val videoUrl: String,
    val videoTime: Long?,
    val duration: Long?,
)
