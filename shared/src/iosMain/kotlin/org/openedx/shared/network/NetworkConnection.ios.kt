package org.openedx.shared.network

import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.Network.nw_interface_type_wifi
import platform.darwin.dispatch_queue_create
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class NetworkConnection {
    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("NetworkMonitor", null)
    private var isConnected = false
    private var isWifi = false

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            isConnected = nw_path_get_status(path) == nw_path_status_satisfied
            isWifi = nw_path_uses_interface_type(path, nw_interface_type_wifi)
        }
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }

    actual fun isOnline(): Boolean = isConnected

    actual fun isWifiConnected(): Boolean = isWifi

    fun cancel() {
        nw_path_monitor_cancel(monitor)
    }
}
