package org.openedx.core.domain.model

data class Media(
    val bannerImage: BannerImage? = null,
    val courseImage: CourseImage? = null,
    val courseVideo: CourseVideo? = null,
    val image: Image? = null
)

data class Image(
    val large: String,
    val raw: String,
    val small: String
)

data class CourseVideo(
    val uri: String
)

data class CourseImage(
    val uri: String,
    val name: String
)

data class BannerImage(
    val uri: String,
    val uriAbsolute: String
)
