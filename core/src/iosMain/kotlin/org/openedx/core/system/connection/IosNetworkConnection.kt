package org.openedx.core.system.connection

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_queue_create

/**
 * iOS implementation of the core `NetworkConnection` interface, backed by Apple
 * Network framework's path monitor (same approach as `shared.network.NetworkConnection`,
 * just typed against the core interface ViewModels actually depend on).
 */
@OptIn(ExperimentalForeignApi::class)
class IosNetworkConnection : NetworkConnection {

    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("CoreNetworkMonitor", null)

    private var connected: Boolean = false
    private var wifi: Boolean = false

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            connected = nw_path_get_status(path) == nw_path_status_satisfied
            wifi = nw_path_uses_interface_type(path, nw_interface_type_wifi)
        }
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }

    override fun isOnline(): Boolean = connected
    override fun isWifiConnected(): Boolean = wifi
}
