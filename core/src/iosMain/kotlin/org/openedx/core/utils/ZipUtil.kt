package org.openedx.core.utils

import org.openedx.foundation.utils.FileUtil

actual fun FileUtil.unzipFile(filepath: String): String? {
    // iOS zip extraction not yet implemented
    return null
}
