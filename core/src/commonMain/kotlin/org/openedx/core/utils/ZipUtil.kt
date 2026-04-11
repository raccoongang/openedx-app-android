package org.openedx.core.utils

import org.openedx.foundation.utils.FileUtil

expect fun FileUtil.unzipFile(filepath: String): String?
