package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExperimentalFeaturesConfig(
    @SerialName("APP_LEVEL_DOWNLOADS")
    val appLevelDownloadsConfig: AppLevelDownloadsConfig = AppLevelDownloadsConfig(),
    @SerialName("APP_LEVEL_DATES")
    val appLevelDatesConfig: AppLevelDatesConfig = AppLevelDatesConfig(),
)
