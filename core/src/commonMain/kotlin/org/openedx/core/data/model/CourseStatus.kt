package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseStatus

@Serializable
data class CourseStatus(
    @SerialName("last_visited_module_id")
    val lastVisitedModuleId: String? = null,
    @SerialName("last_visited_module_path")
    val lastVisitedModulePath: List<String>? = null,
    @SerialName("last_visited_block_id")
    val lastVisitedBlockId: String? = null,
    @SerialName("last_visited_unit_display_name")
    val lastVisitedUnitDisplayName: String? = null,
) {
    fun mapToDomain() = CourseStatus(
        lastVisitedModuleId = lastVisitedModuleId ?: "",
        lastVisitedModulePath = lastVisitedModulePath ?: emptyList(),
        lastVisitedBlockId = lastVisitedBlockId ?: "",
        lastVisitedUnitDisplayName = lastVisitedUnitDisplayName ?: ""
    )
}
