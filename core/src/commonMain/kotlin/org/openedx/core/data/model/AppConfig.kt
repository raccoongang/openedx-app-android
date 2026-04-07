package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.AppConfig as DomainAppConfig

@Serializable
data class AppConfig(
    @SerialName("course_dates_calendar_sync")
    val calendarSyncConfig: CalendarSyncConfig = CalendarSyncConfig(),
) {
    fun mapToDomain(): DomainAppConfig {
        return DomainAppConfig(
            courseDatesCalendarSync = calendarSyncConfig.mapToDomain(),
        )
    }
}
