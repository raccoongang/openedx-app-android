package org.openedx.core.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.openedx.core.domain.model.CourseEnrollments as DomainCourseEnrollments

@Serializable(with = CourseEnrollmentsSerializer::class)
data class CourseEnrollments(
    val enrollments: DashboardCourseList,
    val configs: AppConfig,
    val primary: EnrolledCourse?,
) {
    fun mapToDomain() = DomainCourseEnrollments(
        enrollments = enrollments.mapToDomain(),
        configs = configs.mapToDomain(),
        primary = primary?.mapToDomain()
    )
}

object CourseEnrollmentsSerializer : KSerializer<CourseEnrollments> {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CourseEnrollments")

    override fun serialize(encoder: Encoder, value: CourseEnrollments) {
        val jsonEncoder = encoder as JsonEncoder
        val jsonObject = JsonObject(
            mapOf(
                "enrollments" to json.encodeToJsonElement(DashboardCourseList.serializer(), value.enrollments),
                "primary" to json.encodeToJsonElement(EnrolledCourse.serializer(), value.primary ?: return),
            )
        )
        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): CourseEnrollments {
        val jsonDecoder = decoder as JsonDecoder
        val jsonElement = jsonDecoder.decodeJsonElement()
        val jsonObject = jsonElement.jsonObject

        val enrollments = deserializeEnrollments(jsonObject)
        val appConfig = deserializeAppConfig(jsonObject)
        val primaryCourse = deserializePrimaryCourse(jsonObject)

        return CourseEnrollments(enrollments, appConfig, primaryCourse)
    }

    private fun deserializePrimaryCourse(jsonObject: JsonObject): EnrolledCourse? {
        return try {
            jsonObject["primary"]?.let {
                json.decodeFromJsonElement(EnrolledCourse.serializer(), it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deserializeEnrollments(jsonObject: JsonObject): DashboardCourseList {
        return try {
            jsonObject["enrollments"]?.let {
                json.decodeFromJsonElement(DashboardCourseList.serializer(), it)
            } ?: DashboardCourseList(
                next = null, previous = null, count = 0,
                numPages = 0, currentPage = 0, results = listOf()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            DashboardCourseList(
                next = null, previous = null, count = 0,
                numPages = 0, currentPage = 0, results = listOf()
            )
        }
    }

    /**
     * To remove dependency on the backend, all the data related to Remote Config
     * will be received under the `configs` key. The `config` is the key under
     * 'configs` which defines the data that is related to the configuration of the
     * app.
     */
    private fun deserializeAppConfig(jsonObject: JsonObject): AppConfig {
        return try {
            val configString = jsonObject["configs"]
                ?.jsonObject?.get("config")
                ?.jsonPrimitive?.content
                ?: return AppConfig()

            json.decodeFromString(AppConfig.serializer(), configString)
        } catch (_: Exception) {
            AppConfig()
        }
    }
}
