package org.openedx.core.domain.model

data class CoursewareAccess(
    val hasAccess: Boolean,
    val errorCode: String,
    val developerMessage: String,
    val userMessage: String,
    val additionalContextUserMessage: String,
    val userFragment: String
)
