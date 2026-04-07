package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.Media

@Serializable
data class Media(
    @SerialName("banner_image")
    val bannerImage: BannerImage?,
    @SerialName("course_image")
    val courseImage: CourseImage?,
    @SerialName("course_video")
    val courseVideo: CourseVideo?,
    @SerialName("image")
    val image: Image?,
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
    val large: String?,
    @SerialName("raw")
    val raw: String?,
    @SerialName("small")
    val small: String?,
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
    val uri: String?,
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
    val uri: String?,
    @SerialName("name")
    val name: String?
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
    val uri: String?,
    @SerialName("uri_absolute")
    val uriAbsolute: String?,
) {
    fun mapToDomain(): org.openedx.core.domain.model.BannerImage {
        return org.openedx.core.domain.model.BannerImage(
            uri = uri ?: "",
            uriAbsolute = uriAbsolute ?: ""
        )
    }
}
