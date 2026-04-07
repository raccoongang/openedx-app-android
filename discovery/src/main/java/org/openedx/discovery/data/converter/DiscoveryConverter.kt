package org.openedx.discovery.data.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.openedx.core.data.model.room.BannerImageDb
import org.openedx.core.data.model.room.CourseImageDb
import org.openedx.core.data.model.room.CourseVideoDb
import org.openedx.core.data.model.room.ImageDb

class DiscoveryConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromImageDb(imageDb: ImageDb?): String = if (imageDb == null) "" else json.encodeToString(imageDb)

    @TypeConverter
    fun toImageDb(value: String): ImageDb? = if (value.isEmpty()) null else json.decodeFromString(value)

    @TypeConverter
    fun fromBannerImage(bannerImageDb: BannerImageDb?): String = if (bannerImageDb == null) "" else json.encodeToString(bannerImageDb)

    @TypeConverter
    fun toBannerImageDb(value: String): BannerImageDb? = if (value.isEmpty()) null else json.decodeFromString(value)

    @TypeConverter
    fun fromCourseImageDb(courseImageDb: CourseImageDb?): String = if (courseImageDb == null) "" else json.encodeToString(courseImageDb)

    @TypeConverter
    fun toCourseImageDb(value: String): CourseImageDb? = if (value.isEmpty()) null else json.decodeFromString(value)

    @TypeConverter
    fun fromCourseVideoDb(courseVideoDb: CourseVideoDb?): String = if (courseVideoDb == null) "" else json.encodeToString(courseVideoDb)

    @TypeConverter
    fun toCourseVideoDb(value: String): CourseVideoDb? = if (value.isEmpty()) null else json.decodeFromString(value)
}
