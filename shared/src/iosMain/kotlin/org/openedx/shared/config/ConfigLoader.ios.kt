package org.openedx.shared.config

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun loadConfigJson(): String {
    return try {
        val path = NSBundle.mainBundle.pathForResource("config", "json") ?: return "{}"
        NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) ?: "{}"
    } catch (e: Exception) {
        "{}"
    }
}
