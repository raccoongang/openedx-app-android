@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.openedx.foundation.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual class FileUtil(
    private val dirName: String = "",
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val fileManager = NSFileManager.defaultManager

    private fun getBaseDir(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        val basePath = (paths.firstOrNull() as? String) ?: ""
        return if (dirName.isNotEmpty()) {
            "$basePath/$dirName"
        } else {
            basePath
        }
    }

    actual fun getExternalAppDirPath(): String {
        val dir = getBaseDir()
        if (!fileManager.fileExistsAtPath(dir)) {
            fileManager.createDirectoryAtPath(
                dir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
        return dir
    }

    @OptIn(BetaInteropApi::class)
    actual fun <T> getObjectFromFileWithDeserializer(
        deserializer: DeserializationStrategy<T>,
        fileName: String,
    ): T? {
        val filePath = "${getBaseDir()}/$fileName"
        if (!fileManager.fileExistsAtPath(filePath)) return null
        val content = NSString.stringWithContentsOfFile(
            filePath,
            encoding = NSUTF8StringEncoding,
            error = null
        ) ?: return null
        return try {
            json.decodeFromString(deserializer, content)
        } catch (_: Exception) {
            null
        }
    }

    @OptIn(BetaInteropApi::class)
    actual fun <T> saveObjectToFileWithSerializer(
        serializer: SerializationStrategy<T>,
        obj: T,
        fileName: String,
    ) {
        val dir = getBaseDir()
        if (!fileManager.fileExistsAtPath(dir)) {
            fileManager.createDirectoryAtPath(
                dir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
        val filePath = "$dir/$fileName"
        val content = json.encodeToString(serializer, obj)
        @Suppress("CAST_NEVER_SUCCEEDS")
        (content as NSString).writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    }

    actual fun deleteOldAppDirectory(oldDirName: String) {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        val basePath = (paths.firstOrNull() as? String) ?: return
        val oldDir = "$basePath/$oldDirName"
        if (fileManager.fileExistsAtPath(oldDir)) {
            fileManager.removeItemAtPath(oldDir, null)
        }
    }

    actual fun deleteFile(path: String): Boolean {
        return if (fileManager.fileExistsAtPath(path)) {
            fileManager.removeItemAtPath(path, null)
        } else {
            false
        }
    }
}
