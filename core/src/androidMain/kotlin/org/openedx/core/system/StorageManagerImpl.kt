package org.openedx.core.system

import android.os.Environment
import android.os.StatFs

class StorageManagerImpl : StorageManager {

    override fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return totalBlocks * blockSize
    }

    override fun getFreeStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return availableBlocks * blockSize
    }
}
