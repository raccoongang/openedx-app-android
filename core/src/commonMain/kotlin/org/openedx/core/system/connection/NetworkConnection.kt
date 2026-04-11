package org.openedx.core.system.connection

interface NetworkConnection {
    fun isOnline(): Boolean
    fun isWifiConnected(): Boolean
}
