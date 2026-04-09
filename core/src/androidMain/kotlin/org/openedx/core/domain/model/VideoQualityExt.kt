package org.openedx.core.domain.model

import org.openedx.core.R

val VideoQuality.titleResId: Int
    get() = when (this) {
        VideoQuality.AUTO -> R.string.core_video_quality_auto
        VideoQuality.OPTION_360P -> R.string.core_video_quality_p360
        VideoQuality.OPTION_540P -> R.string.core_video_quality_p540
        VideoQuality.OPTION_720P -> R.string.core_video_quality_p720
    }

val VideoQuality.desResId: Int
    get() = when (this) {
        VideoQuality.AUTO -> R.string.core_video_quality_auto_description
        VideoQuality.OPTION_360P -> R.string.core_video_quality_p360_description
        VideoQuality.OPTION_540P -> 0
        VideoQuality.OPTION_720P -> R.string.core_video_quality_p720_description
    }
