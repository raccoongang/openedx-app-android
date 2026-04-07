package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardConfig(
    @SerialName("TYPE")
    private val viewType: String = DashboardType.GALLERY.name,
) {
    fun getType(): DashboardType {
        return DashboardType.valueOf(viewType.uppercase())
    }

    enum class DashboardType {
        LIST, GALLERY
    }
}
