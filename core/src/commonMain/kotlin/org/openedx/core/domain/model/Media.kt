package org.openedx.core.domain.model

import org.openedx.core.data.model.room.BannerImageDb
import org.openedx.core.data.model.room.CourseImageDb
import org.openedx.core.data.model.room.CourseVideoDb
import org.openedx.core.data.model.room.ImageDb
import org.openedx.core.data.model.room.MediaDb

data class Media(
    val bannerImage: BannerImage? = null,
    val courseImage: CourseImage? = null,
    val courseVideo: CourseVideo? = null,
    val image: Image? = null
) {

    fun mapToEntity() = MediaDb(
        bannerImage = bannerImage?.mapToEntity(),
        courseImage = courseImage?.mapToEntity(),
        courseVideo = courseVideo?.mapToEntity(),
        image = image?.mapToEntity()
    )
}

data class Image(
    val large: String,
    val raw: String,
    val small: String
) {

    fun mapToEntity() = ImageDb(large, raw, small)
}

data class CourseVideo(
    val uri: String
) {

    fun mapToEntity() = CourseVideoDb(uri)
}

data class CourseImage(
    val uri: String,
    val name: String
) {

    fun mapToEntity() = CourseImageDb(uri, name)
}

data class BannerImage(
    val uri: String,
    val uriAbsolute: String
) {

    fun mapToEntity() = BannerImageDb(uri, uriAbsolute)
}
