package org.openedx.foundation.utils

import android.content.Context
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.io.FileWriter

actual class FileUtil(
    private val context: Context,
    private val dirName: String = "",
) {
    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun getExternalAppDir(): File {
        val dir = if (dirName.isNotEmpty()) {
            File(context.getExternalFilesDir(null), dirName)
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    actual fun getExternalAppDirPath(): String = getExternalAppDir().path

    actual fun <T> getObjectFromFileWithDeserializer(deserializer: DeserializationStrategy<T>, fileName: String): T? {
        val file = File(getExternalAppDir(), fileName)
        if (!file.exists()) return null
        return try {
            FileReader(file).use { reader ->
                json.decodeFromString(deserializer, reader.readText())
            }
        } catch (_: Exception) {
            null
        }
    }

    actual fun <T> saveObjectToFileWithSerializer(serializer: SerializationStrategy<T>, obj: T, fileName: String) {
        val file = File(getExternalAppDir(), fileName)
        FileWriter(file).use { writer ->
            writer.write(json.encodeToString(serializer, obj))
        }
    }

    actual fun deleteOldAppDirectory(oldDirName: String) {
        val oldDir = File(context.getExternalFilesDir(null), oldDirName)
        if (oldDir.exists()) {
            oldDir.deleteRecursively()
        }
    }

    fun deleteRecursive(file: File, excludeNames: List<String> = emptyList()) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                if (child.name !in excludeNames) {
                    deleteRecursive(child, excludeNames)
                }
            }
        }
        if (file.name !in excludeNames) {
            file.delete()
        }
    }

    actual fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) file.delete() else false
    }
}
