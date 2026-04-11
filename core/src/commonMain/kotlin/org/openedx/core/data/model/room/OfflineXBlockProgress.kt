package org.openedx.core.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Entity(tableName = "offline_x_block_progress_table")
data class OfflineXBlockProgress(
    @PrimaryKey
    @ColumnInfo("id")
    val blockId: String,
    @ColumnInfo("courseId")
    val courseId: String,
    @Embedded
    val jsonProgress: XBlockProgressData,
)

data class XBlockProgressData(
    @PrimaryKey
    @ColumnInfo("url")
    val url: String,
    @ColumnInfo("type")
    val type: String,
    @ColumnInfo("data")
    val data: String
) {

    fun toJson(): String {
        return buildJsonObject {
            put("url", url)
            put("type", type)
            put("data", data)
        }.toString()
    }

    companion object {
        fun parseJson(jsonString: String): XBlockProgressData {
            val jsonObject = Json.decodeFromString<JsonObject>(jsonString)
            val url = jsonObject["url"]?.jsonPrimitive?.content ?: ""
            val type = jsonObject["type"]?.jsonPrimitive?.content ?: ""
            val data = jsonObject["data"]?.jsonPrimitive?.content ?: ""

            return XBlockProgressData(url, type, data)
        }
    }
}
