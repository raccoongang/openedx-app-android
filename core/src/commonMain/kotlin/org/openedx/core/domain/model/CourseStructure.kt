package org.openedx.core.domain.model

import kotlinx.datetime.Instant

data class CourseStructure(
    val root: String,
    val blockData: List<Block>,
    val id: String,
    val name: String,
    val number: String,
    val org: String,
    val start: Instant?,
    val startDisplay: String,
    val startType: String,
    val end: Instant?,
    val coursewareAccess: CoursewareAccess?,
    val media: Media?,
    val certificate: Certificate?,
    val isSelfPaced: Boolean,
    val progress: Progress?,
)
