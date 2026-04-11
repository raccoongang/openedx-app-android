package org.openedx.core.system

interface StorageManager {
    fun getTotalStorage(): Long
    fun getFreeStorage(): Long
}
