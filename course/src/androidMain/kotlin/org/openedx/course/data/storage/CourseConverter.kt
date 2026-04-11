package org.openedx.course.data.storage

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.openedx.core.data.model.room.BlockDb
import org.openedx.core.data.model.room.GradingPolicyDb
import org.openedx.core.data.model.room.SectionScoreDb
import org.openedx.core.data.model.room.discovery.CourseDateBlockDb

class CourseConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromListOfString(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toListOfString(value: String): List<String> = json.decodeFromString(value)

    @TypeConverter
    fun fromListOfBlockDbEntity(value: List<BlockDb>): String = json.encodeToString(value)

    @TypeConverter
    fun toListOfBlockDbEntity(value: String): List<BlockDb> = json.decodeFromString(value)

    @TypeConverter
    fun fromListOfCourseDateBlockDb(value: List<CourseDateBlockDb>): String = json.encodeToString(value)

    @TypeConverter
    fun toListOfCourseDateBlockDb(value: String): List<CourseDateBlockDb> = json.decodeFromString(value)

    @TypeConverter
    fun fromSectionScoreDbList(value: List<SectionScoreDb>?): String = json.encodeToString(value)

    @TypeConverter
    fun toSectionScoreDbList(value: String): List<SectionScoreDb> = json.decodeFromString(value)

    @TypeConverter
    fun fromAssignmentPolicyDbList(value: List<GradingPolicyDb.AssignmentPolicyDb>?): String = json.encodeToString(value)

    @TypeConverter
    fun toAssignmentPolicyDbList(value: String): List<GradingPolicyDb.AssignmentPolicyDb> = json.decodeFromString(value)

    @TypeConverter
    fun fromGradeRangeMap(value: Map<String, Float>?): String = json.encodeToString(value)

    @TypeConverter
    fun toGradeRangeMap(value: String): Map<String, Float> = json.decodeFromString(value)
}
