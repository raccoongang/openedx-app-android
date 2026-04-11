package org.openedx.foundation.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

expect class FileUtil {
    /**
     * Returns the path to the external app directory.
     */
    fun getExternalAppDirPath(): String

    fun <T> getObjectFromFileWithDeserializer(deserializer: DeserializationStrategy<T>, fileName: String): T?
    fun <T> saveObjectToFileWithSerializer(serializer: SerializationStrategy<T>, obj: T, fileName: String)
    fun deleteOldAppDirectory(oldDirName: String)
    fun deleteFile(path: String): Boolean
}

/**
 * Reified convenience wrapper for [FileUtil.getObjectFromFileWithDeserializer].
 */
inline fun <reified T> FileUtil.getObjectFromFile(fileName: String = "data.json"): T? =
    getObjectFromFileWithDeserializer(Json.serializersModule.serializer(), fileName)

/**
 * Reified convenience wrapper for [FileUtil.saveObjectToFileWithSerializer].
 */
inline fun <reified T> FileUtil.saveObjectToFile(obj: T, fileName: String = "data.json") =
    saveObjectToFileWithSerializer(Json.serializersModule.serializer(), obj, fileName)
