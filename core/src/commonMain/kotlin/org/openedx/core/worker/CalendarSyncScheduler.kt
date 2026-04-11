package org.openedx.core.worker

interface CalendarSyncScheduler {
    fun scheduleDailySync()
    fun requestImmediateSync()
    fun requestImmediateSync(courseId: String)
}
