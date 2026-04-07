package org.openedx.core.domain.model


data class CourseStatus(
    val lastVisitedModuleId: String,
    val lastVisitedModulePath: List<String>,
    val lastVisitedBlockId: String,
    val lastVisitedUnitDisplayName: String,
)
