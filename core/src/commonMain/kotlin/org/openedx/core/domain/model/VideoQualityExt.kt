package org.openedx.core.domain.model

import org.jetbrains.compose.resources.StringResource
import org.openedx.core.Res
import org.openedx.core.core_video_quality_auto
import org.openedx.core.core_video_quality_auto_description
import org.openedx.core.core_video_quality_p360
import org.openedx.core.core_video_quality_p360_description
import org.openedx.core.core_video_quality_p540
import org.openedx.core.core_video_quality_p720
import org.openedx.core.core_video_quality_p720_description

val VideoQuality.titleRes: StringResource
    get() = when (this) {
        VideoQuality.AUTO -> Res.string.core_video_quality_auto
        VideoQuality.OPTION_360P -> Res.string.core_video_quality_p360
        VideoQuality.OPTION_540P -> Res.string.core_video_quality_p540
        VideoQuality.OPTION_720P -> Res.string.core_video_quality_p720
    }

val VideoQuality.desRes: StringResource?
    get() = when (this) {
        VideoQuality.AUTO -> Res.string.core_video_quality_auto_description
        VideoQuality.OPTION_360P -> Res.string.core_video_quality_p360_description
        VideoQuality.OPTION_540P -> null
        VideoQuality.OPTION_720P -> Res.string.core_video_quality_p720_description
    }
