package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.Media

@Serializable
data class Media(
    @SerialName("banner_image")
    val bannerImage: BannerImage? = null,
    @SerialName("course_image")
    val courseImage: CourseImage? = null,
    @SerialName("course_video")
    val courseVideo: CourseVideo? = null,
    @SerialName("image")
    val image: Image? = null,
) {

    fun mapToDomain(): Media {
        return Media(
            bannerImage = bannerImage?.mapToDomain(),
            courseImage = courseImage?.mapToDomain(),
            courseVideo = courseVideo?.mapToDomain(),
            image = image?.mapToDomain()
        )
    }
}

@Serializable
data class Image(
    @SerialName("large")
    val large: String? = null,
    @SerialName("raw")
    val raw: String? = null,
    @SerialName("small")
    val small: String? = null,
) {
    fun mapToDomain(): org.openedx.core.domain.model.Image {
        return org.openedx.core.domain.model.Image(
            large = large ?: "",
            raw = raw ?: "",
            small = small ?: ""
        )
    }
}

@Serializable
data class CourseVideo(
    @SerialName("uri")
    val uri: String? = null,
) {
    fun mapToDomain(): org.openedx.core.domain.model.CourseVideo {
        return org.openedx.core.domain.model.CourseVideo(
            uri = uri ?: ""
        )
    }
}

@Serializable
data class CourseImage(
    @SerialName("uri")
    val uri: String? = null,
    @SerialName("name")
    val name: String? = null,
) {
    fun mapToDomain(): org.openedx.core.domain.model.CourseImage {
        return org.openedx.core.domain.model.CourseImage(
            uri = uri ?: "",
            name = name ?: ""
        )
    }
}

@Serializable
data class BannerImage(
    @SerialName("uri")
    val uri: String? = null,
    @SerialName("uri_absolute")
    val uriAbsolute: String? = null,
) {
    fun mapToDomain(): org.openedx.core.domain.model.BannerImage {
        return org.openedx.core.domain.model.BannerImage(
            uri = uri ?: "",
            uriAbsolute = uriAbsolute ?: ""
        )
    }
}
