package org.openedx.shared.network

/**
 * Platform-specific network connectivity check.
 */
expect class NetworkConnection {
    fun isOnline(): Boolean
    fun isWifiConnected(): Boolean
}
