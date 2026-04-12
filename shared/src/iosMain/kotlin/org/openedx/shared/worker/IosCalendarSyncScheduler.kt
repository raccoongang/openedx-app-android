package org.openedx.shared.worker

import org.openedx.core.worker.CalendarSyncScheduler

/**
 * iOS stub for CalendarSyncScheduler.
 * TODO: Implement using BGTaskScheduler.
 */
class IosCalendarSyncScheduler : CalendarSyncScheduler {
    override fun scheduleDailySync() {
        // No-op: iOS calendar sync not yet implemented
    }

    override fun requestImmediateSync() {
        // No-op
    }

    override fun requestImmediateSync(courseId: String) {
        // No-op
    }
}
