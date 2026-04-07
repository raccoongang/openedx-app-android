package org.openedx.foundation.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class FileUtil(
    private val context: Context,
    private val dirName: String = "",
) {

    fun getExternalAppDir(): File {
        val dir = if (dirName.isNotEmpty()) {
            File(context.getExternalFilesDir(null), dirName)
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun <T> saveObjectToFile(obj: T, fileName: String = "data.json") {
        val file = File(getExternalAppDir(), fileName)
        FileWriter(file).use { writer ->
            Gson().toJson(obj, writer)
        }
    }

    inline fun <reified T> getObjectFromFile(fileName: String = "data.json"): T? {
        val file = File(getExternalAppDir(), fileName)
        if (!file.exists()) return null
        return try {
            FileReader(file).use { reader ->
                Gson().fromJson(reader, object : TypeToken<T>() {}.type)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun deleteOldAppDirectory(oldDirName: String) {
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

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) file.delete() else false
    }
}
