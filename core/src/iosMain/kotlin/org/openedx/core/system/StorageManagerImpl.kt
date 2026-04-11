@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.openedx.core.system

import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory

class StorageManagerImpl : StorageManager {

    override fun getTotalStorage(): Long {
        return getFileSystemAttribute("NSFileSystemSize")
    }

    override fun getFreeStorage(): Long {
        return getFileSystemAttribute("NSFileSystemFreeSize")
    }

    private fun getFileSystemAttribute(key: String): Long {
        return try {
            val path = NSHomeDirectory()
            val attrs = NSFileManager.defaultManager.attributesOfFileSystemForPath(path, null)
            (attrs?.get(key) as? Number)?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}
