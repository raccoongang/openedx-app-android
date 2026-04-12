package org.openedx.shared.worker

import org.openedx.course.worker.OfflineProgressSyncScheduler

/**
 * iOS stub for OfflineProgressSyncScheduler.
 * TODO: Implement using BGTaskScheduler.
 */
class IosOfflineProgressSyncScheduler : OfflineProgressSyncScheduler {
    override fun scheduleSync() {
        // No-op: iOS offline progress sync not yet implemented
    }
}
