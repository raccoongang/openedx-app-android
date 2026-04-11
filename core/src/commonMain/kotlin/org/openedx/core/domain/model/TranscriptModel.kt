package org.openedx.core.domain.model

data class TranscriptObject(
    val captions: Map<Int, TranscriptCaption> = emptyMap()
)

data class TranscriptCaption(
    val id: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val content: String = "",
)
